package org.hyperagents.jade;

import jade.core.AID;
import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.introspection.*;
import jade.tools.ToolAgent;
import jade.util.Logger;

import java.util.Map;

/**
 * A Hypermedia Weaver Agent (HWA) is a JADE agent that helps to construct the hypermedia interface
 * exposed by its hosting platform. The HWA functions similarly to a Remote Monitoring Agent (RMA)
 * in JADE: the HWA subscribes to updates from the Agent Management System (AMS) and keeps track of
 * the evolution of the system. There can be multiple HWAs on the same platform, but at most 1 HWA
 * per machine (regardless of the number of containers hosted on that machine).
 *
 * TODO: implement constraint of one HWA per machine
 *
 * Contributors:
 * - Andrei Ciortea, Interactions-HSG, University of St.Gallen
 */
public class HypermediaWeaverAgent extends ToolAgent {
  private HypermediaInterface server;

  @Override
  protected void toolSetup() {
    logger = Logger.getMyLogger(getName());

    // Create hypermedia server with boot config
    server = new HypermediaInterface(getBootProperties());

    try {
      server.start();
    } catch (Exception e) {
      logger.log(Logger.SEVERE, "Starting the HTTP server threw an exception: "
        + e.getMessage());
    }

    addBehaviour(new ListenerBehaviour());
  }

  @Override
  protected void toolTakeDown() {
    try {
      server.stop();
    } catch (Exception e) {
      logger.log(Logger.SEVERE, "Stopping the HTTP server threw an exception: "
        + e.getMessage());
    }

    send(getCancel());
  }

  class ListenerBehaviour extends AMSSubscriber {
    private final PlatformState state = PlatformState.getInstance();

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void installHandlers(Map handlersTable) {
      handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, (EventHandler) ev -> {
        PlatformDescription pd = (PlatformDescription) ev;
        APDescription apDesc = pd.getPlatform();
        state.setPlatformDescription(apDesc);

        logger.log(Logger.INFO, "Platform name: " + apDesc.getName());
      });

      handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, (EventHandler) ev -> {
        AddedContainer ac = (AddedContainer) ev;
        ContainerID cid = ac.getContainer();
        state.addContainerID(cid);

        logger.log(Logger.INFO, "New container added: " + cid.getName() + " "
            + cid.getAddress());
        logger.log(Logger.INFO, "Total containers: " + state.getNumberOfContainers());
      });

      handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, (EventHandler) ev -> {
        RemovedContainer rc = (RemovedContainer)ev;
        ContainerID cid = rc.getContainer();

        if (!state.removeContainerID(cid)) {
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
          AID agentID = ba.getAgent();
          state.addAgentToContainer(cid, agentID);
          logger.log(Logger.INFO, "Agent " + agentID + " born in container " + cid);
        }
      });

      handlersTable.put(IntrospectionVocabulary.DEADAGENT, (EventHandler) ev -> {
        DeadAgent da = (DeadAgent) ev;
        ContainerID cid = da.getWhere();
        
        // ContainerID is null in case of foreign agents registered with the local AMS or virtual agents
        if (cid != null) {
          AID agentID = da.getAgent();
          state.removeAgentFromContainer(cid, agentID);
          logger.log(Logger.INFO, "Agent " + agentID + " killed in container " + cid);
        }
      });
    }
  }
}
