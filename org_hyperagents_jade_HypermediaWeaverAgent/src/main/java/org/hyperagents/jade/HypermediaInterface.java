package org.hyperagents.jade;

import jade.core.ContainerID;
import jade.util.Logger;
import jade.util.leap.Properties;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hyperagents.jade.graphs.ContainerGraphBuilder;
import org.hyperagents.jade.graphs.PlatformGraphBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class HypermediaInterface {
  private final static Logger LOGGER = Logger.getJADELogger(HypermediaInterface.class.getName());

  private Server server;
  private final String httpHost;
  private int httpPort;

  public HypermediaInterface(Properties config) {
    httpHost = config.getProperty("http-host", "localhost");

    try {
      httpPort = Integer.parseInt(config.getProperty("http-port", "3000"));

      server = new Server(httpPort);
      HandlerList list = createHandlerList();
      server.setHandler(list);
    } catch (NumberFormatException e) {
      LOGGER.log(Logger.SEVERE, "The passed port " + httpPort + " is not a number.");
    }

    LOGGER.log(Logger.INFO, "HTTP config: host " + httpHost + ", port " + httpPort);
  }

  public void start() throws Exception {
    server.start();
  }

  public void stop() throws Exception {
    server.stop();
  }

  private HandlerList createHandlerList() {
    PlatformState state = PlatformState.getInstance();

    HandlerList list = new HandlerList();

    list.addHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (requestMatches(baseRequest, "GET","/")) {
          baseRequest.setHandled(true);

          PlatformGraphBuilder builder = new PlatformGraphBuilder(httpHost, httpPort);

          String responseBody = builder.addMetadata()
              .addContainers(state.getContainerIDsImmutable())
              .write(RDFFormat.TURTLE);

          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType("text/turtle");
          response.getWriter().print(responseBody);
        }
      }
    });

    list.addHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String path = baseRequest.getRequestURI();
        if (path.matches("/containers/[^/]*(/$)?")) {
          baseRequest.setHandled(true);

          String containerName = getLastPathParam(path, "/containers/");

          Optional<ContainerID> containerID = state.getContainerID(containerName);

          if (containerID.isPresent()) {
            ContainerGraphBuilder builder = new ContainerGraphBuilder(containerID.get(), httpPort);

            String responseBody = builder.addAgents(state.getAgentsInContainer(containerID.get()))
              .write(RDFFormat.TURTLE);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/turtle");
            response.getWriter().print(responseBody);
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        }
      }
    });

    return list;
  }

  private boolean requestMatches(Request request, String method, String requestURI) {
    return request.getMethod().equalsIgnoreCase(method) &&
        request.getRequestURI().equalsIgnoreCase(requestURI);
  }

  private String getLastPathParam(String path, String prefix) {
    String param = path.substring(prefix.length());
    return param.endsWith("/") ? param.substring(0, param.length()-1) : param;
  }
}
