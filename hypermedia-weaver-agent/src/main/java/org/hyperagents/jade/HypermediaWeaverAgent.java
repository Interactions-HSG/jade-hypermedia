package org.hyperagents.jade;

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
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
  private static final String HYPERMEDIA_SERVICE_HOST = "http-host";
  private static final String DEFAULT_HYPERMEDIA_SERVICE_PROTOCOL = "http";
  private static final int DEFAULT_HYPERMEDIA_SERVICE_PORT = 3000;

  private String httpHost;
  private int httpPort;
  private String httpEndpoint;
  private HypermediaInterface hypermediaService;

  private Map<String, Set<ContainerID>> inquires;

  @Override
  protected void toolSetup() {
    logger = Logger.getMyLogger(getName());

    // Create hypermedia server with boot config
    httpEndpoint = constructHTTPEndpoint();
    hypermediaService = new HypermediaInterface(httpPort);

    try {
      hypermediaService.start();
      registerWithDF();
    } catch (Exception e) {
      logger.log(Logger.SEVERE, "Starting the HTTP server threw an exception: "
        + e.getMessage());
    }

    inquires = new Hashtable<>();

    addBehaviour(new AMSListenerBehaviour());
    addBehaviour(new HandleWeaverMessagesBehaviour());
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

  private String constructBaseEndpoint(String authority, String port) {
    return DEFAULT_HYPERMEDIA_SERVICE_PROTOCOL + "://" + authority + ":" + port + "/";
  }

  private String constructContainerIRI(ContainerID cid) {
    return constructContainerIRI(cid, String.valueOf(httpPort));
  }

  private String constructContainerIRI(ContainerID cid, String port) {
    return constructBaseEndpoint(cid.getAddress(), port) + "containers/" + cid.getName() + "/";
  }

  private String constructHTTPEndpoint() {
    Properties config = getBootProperties();

    httpHost = config.getProperty("local-host", "localhost");
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

    return constructBaseEndpoint(httpHost, String.valueOf(httpPort));
  }

  private DFAgentDescription constructHWADescription(String host) {
    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setName(HYPERMEDIA_SERVICE);
    sd.setType(HYPERMEDIA_SERVICE);
    sd.addProperties(new Property(HYPERMEDIA_SERVICE_HOST, host));
    dfd.addServices(sd);

    return dfd;
  }

  private void registerWithDF() {
    DFAgentDescription dfd = constructHWADescription(httpHost);

    try {
      DFService.register(this, dfd);
      logger.log(Logger.INFO, "Registered hypermedia service with DF for " + httpHost);
    } catch (FIPAException e) {
      logger.log(Logger.SEVERE, "Unable to register " + HYPERMEDIA_SERVICE + " with the DF: "
        + e.getMessage());
    }
  }

  private DFAgentDescription[] getRegisteredHWAs(String hostName) {
    DFAgentDescription template = constructHWADescription(hostName);

    try {
      return DFService.search(this, template);
    } catch (FIPAException e) {
      logger.log(Logger.SEVERE, "Unable to query DF for HWAs: " + e.getMessage());
    }

    return null;
  }

  private void exposeContainerID(ContainerID cid) {
    exposeContainerID(cid, String.valueOf(httpPort));
  }

  private void exposeContainerID(ContainerID cid, String port) {
    PlatformState state = PlatformState.getInstance();
    state.addContainerID(new WebContainerID(cid, constructContainerIRI(cid, port)));

    logger.log(Logger.INFO, "New container added: " + cid.getName() + " "
      + cid.getAddress());
    logger.log(Logger.INFO, "Total containers: " + state.getNumberOfContainers());
  }

  private class HandleWeaverMessagesBehaviour extends CyclicBehaviour {

    @Override
    public void action() {
      MessageTemplate template = MessageTemplate.MatchConversationId(HYPERMEDIA_SERVICE);
      ACLMessage message = myAgent.receive(template);

      if (message != null) {
        if (message.getPerformative() == ACLMessage.QUERY_REF) {
          String address = message.getContent();
          logger.info("Received HWA query for address: " + address);

          ACLMessage reply = message.createReply();
          reply.setPerformative(ACLMessage.INFORM_REF);
          reply.setContent(address + ":" + httpPort);

          myAgent.send(reply);
        } else if (message.getPerformative() == ACLMessage.INFORM_REF) {
          // TODO: checks
          String[] payload = message.getContent().split(":");
          logger.info("HWA reply: received address " + payload[0] + " and port " + payload[1]);

          processAllContainers(payload[0], payload[1]);
        }
      } else {
        block();
      }
    }

    private void processAllContainers(String address, String port) {
      Set<ContainerID> containerIDs = inquires.remove(address);

      if (containerIDs != null) {
        for (ContainerID cid : containerIDs) {
          exposeContainerID(cid, port);
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
        state.setPlatformDescription(apDesc);

        logger.log(Logger.INFO, "Platform name: " + apDesc.getName());
      });

      handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, (EventHandler) ev -> {
        AddedContainer ac = (AddedContainer) ev;
        ContainerID cid = ac.getContainer();

        logger.info("New container: " + cid);

        // Check if this is a local container
        String address = cid.getAddress();
        if (isLocalAddress(address) || address.equalsIgnoreCase(httpHost)) {
          logger.info("Exposing local container: " + cid);
          exposeContainerID(cid);
        } else {
          logger.info("Container is not local: " + cid);
          // If not, then search for an HWA
          Optional<DFAgentDescription> remoteHWA = getWeaverForHost(cid.getAddress());

          if (remoteHWA.isEmpty()) {
            logger.warning("Could not find an HWA for host " + cid.getAddress()
                + ". Containers on this machine will not be shown.");
          } else {
            // An HWA was found, send a query to retrieve the port
            queryWeaver(remoteHWA.get().getName(), cid.getAddress());
            // Communication is async, we register the query and wait for a response
            saveInquiry(cid);
            // We are done for now
            logger.info("Sent query for " + cid.getAddress() + " to " + remoteHWA.get().getName());
          }
        }
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

    private void saveInquiry(ContainerID cid) {
      Set<ContainerID> containerIDs = inquires.getOrDefault(cid.getAddress(), new HashSet<>());
      containerIDs.add(cid);
      inquires.put(cid.getAddress(), containerIDs);
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

    private Optional<DFAgentDescription> getWeaverForHost(String host) {
      DFAgentDescription[] result = getRegisteredHWAs(host);

      if (result == null || result.length == 0) {
        return Optional.empty();
      }

      if (result.length > 1) {
        logger.warning("Multiple HWAs are registered for host " + host);
      }

      return Optional.of(result[0]);
    }

    private void queryWeaver(AID weaver, String host) {
      // An HWA was found, send a query to retrieve the port
      ACLMessage request = new ACLMessage(ACLMessage.QUERY_REF);
      request.addReceiver(weaver);
      request.setConversationId(HYPERMEDIA_SERVICE);
      request.setContent(host);
      myAgent.send(request);
    }

    private String constructAgentIRI(ContainerID cid, AID aid) {
      return constructContainerIRI(cid) + "agents/" + aid.getLocalName();
    }
  }
}
