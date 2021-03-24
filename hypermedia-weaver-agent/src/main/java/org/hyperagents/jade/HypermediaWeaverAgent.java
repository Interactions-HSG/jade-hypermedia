package org.hyperagents.jade;

import jade.core.AID;
import jade.core.ContainerID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.introspection.*;
import jade.tools.ToolAgent;
import jade.util.Logger;
import jade.util.leap.Properties;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.platform.WebContainerID;

import java.util.Map;

/**
 * A Hypermedia Weaver Agent (HWA) is a JADE agent that helps to construct the hypermedia interface
 * exposed by its hosting platform. The HWA functions similarly to a Remote Monitoring Agent (RMA)
 * in JADE: the HWA subscribes to updates from the Agent Management System (AMS) and keeps track of
 * the evolution of the system. There can be multiple HWAs on the same platform, but at most 1 HWA
 * per machine (regardless of the number of containers hosted on that machine).
 */
public class HypermediaWeaverAgent extends ToolAgent {
  private static final String HYPERMEDIA_SERVICE = "hypermedia-weaving";
  private static final String HYPERMEDIA_SERVICE_ENDPOINT = "http-host";
  private static final String DEFAULT_HYPERMEDIA_SERVICE_PROTOCOL = "http";
  private static final int DEFAULT_HYPERMEDIA_SERVICE_PORT = 3000;

  private String localHost;
  private int httpPort;
  private String httpEndpoint;
  private HypermediaInterface hypermediaService;

  @Override
  protected void toolSetup() {
    logger = Logger.getMyLogger(getName());

    // Create hypermedia server with boot config
    httpEndpoint = constructHTTPEndpoint();
    hypermediaService = new HypermediaInterface(httpPort);

    try {
      hypermediaService.start();
      registerWithDF(httpEndpoint);
    } catch (Exception e) {
      logger.log(Logger.SEVERE, "Starting the HTTP server threw an exception: "
        + e.getMessage());
    }

    addBehaviour(new ListenerBehaviour());
  }

  @Override
  protected void toolTakeDown() {
    try {
      hypermediaService.stop();
      DFService.deregister(this);
    } catch (FIPAException e) {
      logger.log(Logger.SEVERE, "Unable to deregister with the DF.");
    } catch (Exception e) {
      logger.log(Logger.SEVERE, "Stopping the HTTP server threw an exception: "
        + e.getMessage());
    }

    // Deregister from notifications with the AMS
    send(getCancel());
  }

  private String constructHTTPEndpoint() {
    Properties config = getBootProperties();

    localHost = config.getProperty("local-host", "localhost");
    String port = config.getProperty("http-port");

    if (port == null) {
      httpPort = DEFAULT_HYPERMEDIA_SERVICE_PORT;
    } else {
      try {
        httpPort = Integer.parseInt(port);
      } catch (NumberFormatException e) {
        logger.log(Logger.SEVERE, "Provided port is not a valid number: " + e.getMessage());
      }
    }

    return "http://" + localHost + ":" + httpPort + "/";
  }

  private void registerWithDF(String httpEndpoint) {
    ServiceDescription sd = new ServiceDescription();
    sd.setName(HYPERMEDIA_SERVICE);
    sd.setType(HYPERMEDIA_SERVICE);
    sd.addProperties(new Property(HYPERMEDIA_SERVICE_ENDPOINT, httpEndpoint));

    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    dfd.addServices(sd);

    try {
      logger.log(Logger.INFO, "Registering HTTP endpoint with DF: " + httpEndpoint);
      DFService.register(this, dfd);
    }
    catch (FIPAException e) {
      logger.log(Logger.SEVERE, "Unable to register " + HYPERMEDIA_SERVICE + " with the DF: "
        + e.getMessage());
    }
  }

  private DFAgentDescription[] getRegisteredHWAs(String httpEndpoint) {
    // Construct the service search template
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setName(HYPERMEDIA_SERVICE);
    sd.setType(HYPERMEDIA_SERVICE);
    sd.addProperties(new Property(HYPERMEDIA_SERVICE_ENDPOINT, httpEndpoint));
    template.addServices(sd);

    try {
      return DFService.search(this, template);
    } catch (FIPAException e) {
      logger.log(Logger.SEVERE, "Unable to query DF for HWAs on my address: " + e.getMessage());
    }

    return null;
  }

  private class ListenerBehaviour extends AMSSubscriber {
    private final PlatformState state = PlatformState.getInstance();

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void installHandlers(Map handlersTable) {
      handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, (EventHandler) ev -> {
        PlatformDescription pd = (PlatformDescription) ev;
        WebAPDescription apDesc = new WebAPDescription(pd.getPlatform(), httpEndpoint);
        state.setPlatformDescription(apDesc);

        logger.log(Logger.INFO, "Platform name: " + apDesc.getName());
      });

      handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, (EventHandler) ev -> {
        AddedContainer ac = (AddedContainer) ev;
        ContainerID cid = ac.getContainer();

        String address = cid.getAddress();
        if (!address.equalsIgnoreCase(localHost)) {
          // TODO: ask for port from HWA
        }

        state.addContainerID(new WebContainerID(cid, constructContainerIRI(cid)));

        logger.log(Logger.INFO, "New container added: " + cid.getName() + " "
            + cid.getAddress());
        logger.log(Logger.INFO, "Total containers: " + state.getNumberOfContainers());
      });

      handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, (EventHandler) ev -> {
        RemovedContainer rc = (RemovedContainer)ev;
        ContainerID cid = rc.getContainer();

        if (!state.removeContainerID(new WebContainerID(cid, constructContainerIRI(cid)))) {
          logger.log(Logger.INFO, "Cannot remove container, container not found: "
              + cid.getName() + " " + cid.getAddress());
        } else {
          logger.log(Logger.INFO, "Container removed: " + cid.getName() + " " + cid.getAddress());
        }

        logger.log(Logger.INFO, "Total containers: " + state.getNumberOfContainers());
      });

      handlersTable.put(IntrospectionVocabulary.BORNAGENT, (EventHandler) ev -> {
        BornAgent ba = (BornAgent) ev;
        ContainerID cid = ba.getWhere();

        // ContainerID is null in case of foreign agents registered with the local AMS or virtual agents
        if (cid != null) {
          WebContainerID webCID = new WebContainerID(cid, constructContainerIRI(cid));
          WebAID webAID = new WebAID(ba.getAgent(), constructAgentIRI(cid, ba.getAgent()));
          state.addAgentToContainer(webCID, webAID);

          logger.log(Logger.INFO, "Agent " + webAID + " born in container " + webCID);
        }
      });

      handlersTable.put(IntrospectionVocabulary.DEADAGENT, (EventHandler) ev -> {
        DeadAgent da = (DeadAgent) ev;
        ContainerID cid = da.getWhere();

        // ContainerID is null in case of foreign agents registered with the local AMS or virtual agents
        if (cid != null) {
          WebContainerID webCID = new WebContainerID(cid, constructContainerIRI(cid));
          WebAID webAID = new WebAID(da.getAgent(), constructAgentIRI(cid, da.getAgent()));
          state.removeAgentFromContainer(webCID, webAID);

          logger.log(Logger.INFO, "Agent " + webAID + " killed in container " + webCID);
        }
      });
    }

    private String constructContainerIRI(ContainerID cid) {
      return DEFAULT_HYPERMEDIA_SERVICE_PROTOCOL + "://" + cid.getAddress() + ":" + httpPort + "/containers/"
          + cid.getName() + "/";
    }

    private String constructAgentIRI(ContainerID cid, AID aid) {
      return constructContainerIRI(cid) + "agents/" + aid.getLocalName();
    }
  }
}
