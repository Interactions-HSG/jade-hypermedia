package org.hyperagents.jade;

import ch.unisg.ics.interactions.hmas.core.hostables.HypermediaMASPlatform;
import ch.unisg.ics.interactions.hmas.core.hostables.Workspace;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.ResourceProfile;
import jade.util.Logger;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hyperagents.jade.graphs.AgentGraphBuilder;
import org.hyperagents.jade.graphs.ContainerGraphBuilder;
import org.hyperagents.jade.graphs.PlatformGraphBuilder;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.platform.WebContainerID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class HypermediaInterface {
  private final static Logger LOGGER = Logger.getJADELogger(HypermediaInterface.class.getName());
  private final static String TURTLE_MEDIA_TYPE = "text/turtle;charset=UTF-8";

  private final Server server;

  public HypermediaInterface(int httpPort) {
    server = new Server(httpPort);
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
          response.setStatus(HttpServletResponse.SC_SEE_OTHER);
          response.setHeader(HttpHeader.LOCATION.asString(), "/ap-description");
        }
      }
    });

    list.addHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (baseRequest.getMethod().matches("GET")
            && baseRequest.getRequestURI().matches("/ap-description")) {
          baseRequest.setHandled(true);

          PlatformGraphBuilder builder = new PlatformGraphBuilder(state.getAPDescription());

          String responseBody = builder.addMetadata()
//              .addAPServices()
              .addContainers(state.getContainerIDs())
              .write(RDFFormat.TURTLE);

//          WebAPDescription apDesc = state.getAPDescription();
//
//          HypermediaMASPlatform.Builder builder = new HypermediaMASPlatform.Builder()
//            .setIRIAsString(apDesc.getPlatformIRI());
//
//          // add containers
//          Set<WebContainerID> containerIDs = state.getContainerIDs();
//
//          for (WebContainerID cid : containerIDs) {
//            LOGGER.info("Adding container: " + cid.getIRI());
//
//            builder.addHostedResource(new Workspace.Builder()
//              .setIRIAsString(cid.getContainerIRI())
//              .build());
//          }

          response.setStatus(HttpServletResponse.SC_OK);
          response.setContentType(TURTLE_MEDIA_TYPE);
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
            && path.matches("/containers/[^/]+(/$)?")) {
          baseRequest.setHandled(true);

          String[] elements = path.split("/");
          String containerName = elements[2];
          Optional<WebContainerID> containerID = state.getContainerIDByName(containerName);

          if (containerID.isPresent()) {
            ContainerGraphBuilder builder = new ContainerGraphBuilder(containerID.get());

            String responseBody = builder.addMetadata()
              .addAgents(state.getAgentsInContainer(containerID.get()))
              .write(RDFFormat.TURTLE);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(TURTLE_MEDIA_TYPE);
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
            && path.matches("/containers/[^/]+/agents/[^/]+(/$)?")) {
          baseRequest.setHandled(true);

          String[] elements = path.split("/");
          String containerName = elements[2];
          String agentName = elements[4];

          LOGGER.log(Logger.INFO, "Request for agent " + agentName + " in container "
              + containerName);

          Optional<WebContainerID> containerID = state.getContainerIDByName(containerName);

          if (containerID.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LOGGER.log(Logger.INFO, "Not able to retrieve description of " + agentName + " in "
              + containerName + ": container does not exist");
          } else {
            Optional<WebAID> agentID = state.getAgentIDByName(containerID.get(), agentName);

            if (agentID.isEmpty()) {
              response.setStatus(HttpServletResponse.SC_NOT_FOUND);
              LOGGER.log(Logger.INFO, "Not able to retrieve description of " + agentName + " in "
                + containerName + ": agent does not exist");
            } else {
              AgentGraphBuilder builder = new AgentGraphBuilder(agentID.get());

              String responseBody = builder.addMetadata()
                  .addAddresses()
                  .addResolvers()
                  .write(RDFFormat.TURTLE);

              response.setStatus(HttpServletResponse.SC_OK);
              response.setContentType(TURTLE_MEDIA_TYPE);
              response.getWriter().print(responseBody);
            }
          }
        }
      }
    });

    return list;
  }
}
