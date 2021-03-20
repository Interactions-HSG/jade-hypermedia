package org.hyperagents.jade;

import jade.core.ContainerID;
import jade.domain.introspection.*;
import jade.tools.ToolAgent;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HypermediaAgentSystem extends ToolAgent {

  private List<ContainerID> containers;

  @Override
  protected void toolSetup() {
    logger = Logger.getMyLogger(getName());
    containers = new ArrayList<ContainerID>();

    addBehaviour(new ListenerBehaviour());
  }

  @Override
  protected void toolTakeDown() {
    send(getCancel());
  }

  class ListenerBehaviour extends AMSSubscriber {

    @Override
    protected void installHandlers(Map handlersTable) {
      handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, new EventHandler() {
        private static final long serialVersionUID = 1L;

        public void handle(Event ev) {
          AddedContainer ac = (AddedContainer) ev;
          ContainerID cid = ac.getContainer();

          containers.add(cid);

          logger.log(Logger.INFO, "New container added: " + cid.getName() + " " + cid.getAddress());
          logger.log(Logger.INFO, "Total containers: " + containers.size());
        }
      });

      handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, new EventHandler() {
        private static final long serialVersionUID = 1L;

        public void handle(Event ev) {
          RemovedContainer rc = (RemovedContainer)ev;
          ContainerID cid = rc.getContainer();

          containers.remove(cid);

          logger.log(Logger.INFO, "Container removed: " + cid.getName() + " " + cid.getAddress());
          logger.log(Logger.INFO, "Total containers: " + containers.size());
        }
      });
    }
  }
}
