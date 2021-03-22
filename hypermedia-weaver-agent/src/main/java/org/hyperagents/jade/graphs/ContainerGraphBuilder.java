package org.hyperagents.jade.graphs;

import jade.core.AID;
import jade.core.ContainerID;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Set;

public class ContainerGraphBuilder extends EntityGraphBuilder {
  private final ContainerID containerID;

  public ContainerGraphBuilder(ContainerID containerID, int httpPort) {
    super(containerID.getAddress(), httpPort);

    this.containerID = containerID;

    if (containerID.getMain()) {
      graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(JADE.MainContainer));
    } else {
      graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(JADE.Container));
    }
  }

  @Override
  public String getDocumentIRI() {
    return baseIRI + "containers/" + containerID.getName() + "/";
  }

  public ContainerGraphBuilder addMetadata() {
    graphBuilder.add(getDocumentIRI(), JADE.hasName, containerID.getName());
    return this;
  }

  public ContainerGraphBuilder addAgents(Set<AID> agentIDs) {
    for (AID aid : agentIDs) {
      AgentGraphBuilder agentGraph = new AgentGraphBuilder(containerID, aid, httpPort);
      graphBuilder.add(getDocumentIRI(), JADE.containsAgent, rdf.createIRI(agentGraph.getEntityIRI()));
    }

    return this;
  }
}
