package org.hyperagents.jade;

import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.introspection.*;
import jade.tools.ToolAgent;
import jade.util.Logger;

import java.util.Map;

public class HypermediaAgentSystem extends ToolAgent {
  private HypermediaInterface server;

  @Override
  protected void toolSetup() {
    logger = Logger.getMyLogger(getName());

    server = new HypermediaInterface(3000);

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

        state.removeContainerID(cid);

        logger.log(Logger.INFO, "Container removed: " + cid.getName() + " " + cid.getAddress());
        logger.log(Logger.INFO, "Total containers: " + state.getNumberOfContainers());
      });

      handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, (EventHandler) ev -> {
        PlatformDescription pd = (PlatformDescription) ev;
        APDescription apDesc = pd.getPlatform();
        state.setPlatformDescription(apDesc);

        logger.log(Logger.INFO, "Platform name: " + apDesc.getName());
      });
    }
  }
}
