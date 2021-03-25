package org.hyperagents.jade;

import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.introspection.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.ToolAgent;
import jade.util.Logger;
import jade.util.leap.Properties;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.platform.WebContainerID;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * A Hypermedia Weaver Agent (HWA) is a JADE agent that helps to construct the hypermedia interface
 * exposed by its hosting platform. The HWA functions similarly to a Remote Monitoring Agent (RMA)
 * in JADE: the HWA subscribes to updates from the Agent Management System (AMS) and keeps track of
 * the evolution of the system. There can be multiple HWAs on the same platform, but at most 1 HWA
 * per machine (regardless of the number of containers hosted on that machine).
 */
public class HypermediaWeaverAgent extends ToolAgent {
  private static final String HYPERMEDIA_SERVICE = "hypermedia-weaving";
  private static final String HYPERMEDIA_DEFAULT_PROTOCOL = "http";
  private static final int HYPERMEDIA_DEFAULT_PORT = 3000;

  private String httpHost;
  private int httpPort;
  private String httpEndpoint;
  private HypermediaInterface hypermediaService;

  private Map<String, Set<ContainerID>> pendingContainers;
  private Map<String, String> hTable;

  @Override
  protected void toolSetup() {
    logger = Logger.getMyLogger(getName());

    // Read init parameters
    init();
    // Register the hypermedia service with the DF
    registerWithDF();
    // Announce the service to all other HWAs
    informAllWeavers();

    // This behavior handles inform messages from other HWAs
    addBehaviour(new HandleWeaverMessagesBehaviour());
    // This behaviour listens for events of interest from the AMS
    addBehaviour(new AMSListenerBehaviour());
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

  private void init() {
    pendingContainers = new Hashtable<>();
    hTable = new Hashtable<>();

    // Read initialization parameters
    Properties config = getBootProperties();
    httpHost = config.getProperty("local-host", "localhost");
    String port = config.getProperty("http-port", String.valueOf(HYPERMEDIA_DEFAULT_PORT));

    try {
      httpPort = Integer.parseInt(port);
      // Start hypermedia service
      hypermediaService = new HypermediaInterface(httpPort);
      hypermediaService.start();
    } catch (NumberFormatException e) {
      logger.log(Logger.SEVERE, "Trying default HTTP port, provided port is not a valid number: "
          + e.getMessage());
      httpPort = HYPERMEDIA_DEFAULT_PORT;
    } catch (Exception e) {
      logger.log(Logger.SEVERE, "Starting the HTTP server failed: " + e.getMessage());
    }

    httpEndpoint = HYPERMEDIA_DEFAULT_PROTOCOL + "://" + httpHost + ":" + httpPort + "/";
  }

  private void registerWithDF() {
    DFAgentDescription dfd = constructHWADescription();

    try {
      DFService.register(this, dfd);
      logger.log(Logger.INFO, "Registered hypermedia service with DF for " + httpHost);
    } catch (FIPAException e) {
      logger.log(Logger.SEVERE, "Unable to register " + HYPERMEDIA_SERVICE + " with the DF: "
        + e.getMessage());
    }
  }

  private void informAllWeavers() {
    DFAgentDescription[] weavers = getRegisteredHWAs();

    if (weavers != null && weavers.length > 0) {
      ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
      inform.setConversationId(HYPERMEDIA_SERVICE);
      inform.setContent(HYPERMEDIA_DEFAULT_PROTOCOL + ":" + httpHost + ":" + httpPort);

      for (DFAgentDescription weaver : weavers) {
        if (weaver.getName() != getAID()) {
          inform.addReceiver(weaver.getName());
        }
      }

      send(inform);
    }
  }

  private DFAgentDescription constructHWADescription() {
    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setName(HYPERMEDIA_SERVICE);
    sd.setType(HYPERMEDIA_SERVICE);
    dfd.addServices(sd);

    return dfd;
  }

  private DFAgentDescription[] getRegisteredHWAs() {
    DFAgentDescription template = constructHWADescription();

    try {
      return DFService.search(this, template);
    } catch (FIPAException e) {
      logger.log(Logger.SEVERE, "Unable to query DF for HWAs: " + e.getMessage());
    }

    return null;
  }

  private void exposeContainerID(ContainerID cid) {
    exposeContainerID(cid, httpEndpoint);
  }

  private void exposeContainerID(ContainerID cid, String endpoint) {
    PlatformState state = PlatformState.getInstance();
    state.addContainerID(new WebContainerID(cid, endpoint));

    logger.log(Logger.INFO, "New container added: " + cid.getName() + " "
        + cid.getAddress() + " " + endpoint);
    logger.log(Logger.INFO, "Total containers: " + state.getNumberOfContainers());
  }

  private class HandleWeaverMessagesBehaviour extends CyclicBehaviour {

    @Override
    public void action() {
      MessageTemplate template = MessageTemplate.MatchConversationId(HYPERMEDIA_SERVICE);
      ACLMessage message = myAgent.receive(template);

      if (message != null) {
        if (message.getPerformative() == ACLMessage.INFORM) {
          // Content format is protocol:address:port
          String[] payload = message.getContent().split(":");
          String address = payload[1];
          String endpoint = payload[0] + "://" + address + ":" + payload[2] + "/";

          logger.info("Discovered new weaver: " + message.getSender().getName()
              + " for endpoint " + endpoint);

          // Keep track of the discovered endpoints
          hTable.put(address, endpoint);
          // Check any known containers for this endpoint
          checkPendingContainers(address, endpoint);
        }
      } else {
        block();
      }
    }

    private void checkPendingContainers(String address, String endpoint) {
      Set<ContainerID> containers = pendingContainers.get(address);

      if (containers != null) {
        for (ContainerID cid : containers) {
          logger.info("Exposing remote container: " + cid);
          exposeContainerID(cid, endpoint);
        }
      }
    }
  }

  private class AMSListenerBehaviour extends AMSSubscriber {
    private final PlatformState state = PlatformState.getInstance();

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void installHandlers(Map handlersTable) {
      handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, (EventHandler) ev -> {
        PlatformDescription pd = (PlatformDescription) ev;
        WebAPDescription apDesc = new WebAPDescription(pd.getPlatform(), httpEndpoint);
        state.setAPDescription(apDesc);

        logger.log(Logger.INFO, "Platform name: " + apDesc.getName());
      });

      handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, (EventHandler) ev -> {
        AddedContainer ac = (AddedContainer) ev;
        ContainerID cid = ac.getContainer();

        logger.info("New container: " + cid);

        // Check if this is a local container
        String containerAddress = cid.getAddress();
        if (isLocalAddress(containerAddress) || containerAddress.equalsIgnoreCase(httpHost)) {
          logger.info("Exposing local container: " + cid);
          exposeContainerID(cid);
        } else {
          String endpoint = hTable.get(containerAddress);

          if (endpoint == null) {
            logger.info("Container is not local and I don't know this address: " + cid);
            Set<ContainerID> containerIDs = pendingContainers.getOrDefault(containerAddress,
              new HashSet<>());
            containerIDs.add(cid);
            pendingContainers.put(containerAddress, containerIDs);
          } else {
            logger.info("Container is not local, but I know this address: " + cid);
            exposeContainerID(cid, endpoint);

            if (cid.getMain()) {
              state.setMainNodeEndpoint(endpoint);
            }
          }
        }
      });

      handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, (EventHandler) ev -> {
        RemovedContainer rc = (RemovedContainer)ev;
        ContainerID cid = rc.getContainer();

        if (!state.removeContainerID(new WebContainerID(cid, httpEndpoint))) {
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
          WebContainerID webCID = new WebContainerID(cid, httpEndpoint);
          WebAID webAID = new WebAID(ba.getAgent(), webCID.getIRI());
          state.addAgentToContainer(webCID, webAID);

          logger.log(Logger.INFO, "Agent " + webAID + " born in container " + webCID);
        }
      });

      handlersTable.put(IntrospectionVocabulary.DEADAGENT, (EventHandler) ev -> {
        DeadAgent da = (DeadAgent) ev;
        ContainerID cid = da.getWhere();

        // ContainerID is null in case of foreign agents registered with the local AMS or virtual agents
        if (cid != null) {
          WebContainerID webCID = new WebContainerID(cid, httpEndpoint);
          WebAID webAID = new WebAID(da.getAgent(), webCID.getIRI());
          state.removeAgentFromContainer(webCID, webAID);

          logger.log(Logger.INFO, "Agent " + webAID + " killed in container " + webCID);
        }
      });
    }

    private boolean isLocalAddress(String host) {
      try {
        InetAddress address = InetAddress.getByName(host);

        // Check if the address is a valid special local or loop back
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
          return true;
        }

        // Check if the address is defined on any interface
        try {
          return NetworkInterface.getByInetAddress(address) != null;
        } catch (SocketException e) {
          logger.severe("Unable to check container address: " + e.getMessage());
        }
      } catch (UnknownHostException e) {
        logger.severe("Unknown host: " + host);
      }

      return false;
    }
  }
}
