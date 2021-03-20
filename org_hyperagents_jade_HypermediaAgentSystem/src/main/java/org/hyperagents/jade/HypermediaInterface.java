package org.hyperagents.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.rdf4j.rio.RDFFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HypermediaInterface {
  private static String BASE_URI;

  private final PlatformState state;
  private final Server server;
  private final int httpPort;

  public HypermediaInterface(int port) {
    state = PlatformState.getInstance();
    httpPort = port;
    server = new Server(port);

    Profile jadeProfile = new ProfileImpl();
    BASE_URI = "http://" + jadeProfile.getParameter(Profile.EXPORT_HOST, "localhost")
        + ":" + port + "/";

    HandlerList list = createHandlerList();
    server.setHandler(list);
  }

  public void start() throws Exception {
    server.start();
  }

  public void stop() throws Exception {
    server.stop();
  }

  private HandlerList createHandlerList() {
    HandlerList list = new HandlerList();

    list.addHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (requestMatches(baseRequest, "GET","/")) {
          baseRequest.setHandled(true);

          PlatformGraphBuilder builder = new PlatformGraphBuilder(HypermediaInterface.BASE_URI,
              httpPort);

          String responseBody = builder.addMetadata()
              .addContainers(state.getContainerIDsImmutable())
              .write(RDFFormat.TURTLE);

          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType("text/turtle");

          response.getWriter().print(responseBody);
        }
      }
    });

    return list;
  }

  private boolean requestMatches(Request request, String method, String requestURI) {
    return request.getMethod().equalsIgnoreCase(method) &&
        request.getRequestURI().equalsIgnoreCase(requestURI);
  }
}
