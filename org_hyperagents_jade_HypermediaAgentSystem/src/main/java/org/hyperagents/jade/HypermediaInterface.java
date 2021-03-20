package org.hyperagents.jade;

import jade.core.ContainerID;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HypermediaInterface {
  private final PlatformState state;
  private final Server server;

  public HypermediaInterface(int port) {
    state = PlatformState.getInstance();
    server = new Server(port);

    HandlerList list = new HandlerList();

    list.addHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (requestMatches(baseRequest, "GET","/")) {
          baseRequest.setHandled(true);
          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType("text/html");
          response.getWriter().println("Containers: " + state.getNumberOfContainers());

          for (ContainerID cid : state.getContainerIDs()) {
            response.getWriter().println(cid);
          }
        }
      }
    });

    server.setHandler(list);
  }

  public void start() throws Exception {
    server.start();
  }

  public void stop() throws Exception {
    server.stop();
  }

  private boolean requestMatches(Request request, String method, String requestURI) {
    return request.getMethod().equalsIgnoreCase(method) &&
        request.getRequestURI().equalsIgnoreCase(requestURI);
  }
}
