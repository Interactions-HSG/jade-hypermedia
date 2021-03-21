package org.hyperagents.jade;

import jade.core.AID;
import jade.core.ContainerID;
import jade.util.Logger;
import jade.util.leap.Properties;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hyperagents.jade.graphs.AgentGraphBuilder;
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

    // TODO: refactor list handlers

    list.addHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (baseRequest.getMethod().matches("GET")
            && baseRequest.getRequestURI().matches("/")) {
          baseRequest.setHandled(true);

          PlatformGraphBuilder builder = new PlatformGraphBuilder(state.getAPDescription(),
              httpHost, httpPort);

          String responseBody = builder.addMetadata()
              .addAPServices()
              .addContainers(state.getContainerIDs())
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
        if (baseRequest.getMethod().matches("GET")
            && path.matches("/containers/[^/]*(/$)?")) {
          baseRequest.setHandled(true);

          String[] elements = path.split("/");
          String containerName = elements[2];
          Optional<ContainerID> containerID = state.getContainerIDByName(containerName);

          if (containerID.isPresent()) {
            ContainerGraphBuilder builder = new ContainerGraphBuilder(containerID.get(), httpPort);

            String responseBody = builder.addMetadata()
              .addAgents(state.getAgentsInContainer(containerID.get()))
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

    list.addHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String path = baseRequest.getRequestURI();
        if (baseRequest.getMethod().matches("GET")
            && path.matches("/containers/[^/]*/agents/[^/]*(/$)?")) {
          baseRequest.setHandled(true);

          String[] elements = path.split("/");
          String containerName = elements[2];
          String agentName = elements[4];

          LOGGER.log(Logger.INFO, "Request for agent " + agentName + " in container "
              + containerName);

          Optional<ContainerID> containerID = state.getContainerIDByName(containerName);

          if (containerID.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LOGGER.log(Logger.INFO, "Not able to retrieve description of " + agentName + " in "
              + containerName + ": container does not exist");
          } else {
            Optional<AID> agentID = state.getAgentIDByName(containerID.get(), agentName);

            if (agentID.isEmpty()) {
              response.setStatus(HttpServletResponse.SC_NOT_FOUND);
              LOGGER.log(Logger.INFO, "Not able to retrieve description of " + agentName + " in "
                + containerName + ": agent does not exist");
            } else {
              AgentGraphBuilder builder = new AgentGraphBuilder(containerID.get(), agentID.get(),
                  httpPort);

              String responseBody = builder.addMetadata()
                  .addAddresses()
                  .addResolvers()
                  .write(RDFFormat.TURTLE);

              response.setStatus(HttpServletResponse.SC_OK);
              response.setContentType("text/turtle");
              response.getWriter().print(responseBody);
            }
          }
        }
      }
    });

    return list;
  }
}
