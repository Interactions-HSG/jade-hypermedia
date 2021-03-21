package org.hyperagents.jade.graphs;

import jade.core.AID;
import jade.core.ContainerID;

public class AgentGraphBuilder extends GraphBuilder {
  private final AID agentID;
  private final ContainerID containerID;

  public AgentGraphBuilder(ContainerID containerID, AID agentID, int httpPort) {
    super(containerID.getAddress(), httpPort);
    this.agentID = agentID;
    this.containerID = containerID;
  }

  @Override
  public String getSubjectIRI() {
    ContainerGraphBuilder builder = new ContainerGraphBuilder(containerID, httpPort);
    return builder.getSubjectIRI() + "agents/" + agentID.getLocalName();
  }
}
