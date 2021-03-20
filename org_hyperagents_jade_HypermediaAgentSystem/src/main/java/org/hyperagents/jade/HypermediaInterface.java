package org.hyperagents.jade;

import jade.util.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HypermediaInterface {
  private PlatformState state;
  private Server server;

  public HypermediaInterface(int port) {
    state = PlatformState.getInstance();
    server = new Server(port);

    ContextHandler mainContainerContext = new ContextHandler("/");

    mainContainerContext.setHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException, ServletException {
        if (baseRequest.getMethod().equals("GET")) {
          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType("text/html");
          response.getWriter().println("Containers: " + state.getNumberOfContainers());
        }

        baseRequest.setHandled(true);
      }
    });

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[] { mainContainerContext });
    server.setHandler(contexts);
  }

  public void start() throws Exception {
    server.start();
  }

  public void stop() throws Exception {
    server.stop();
  }
}
