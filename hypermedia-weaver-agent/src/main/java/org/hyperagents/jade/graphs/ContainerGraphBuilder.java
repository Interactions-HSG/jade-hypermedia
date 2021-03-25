package org.hyperagents.jade.graphs;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebContainerID;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Set;

/**
 * Constructs an RDF description of a JADE container.
 */
public class ContainerGraphBuilder extends EntityGraphBuilder {
  private final WebContainerID containerID;

  /**
   * Constructs a container graph builder. Unlike {@link PlatformGraphBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter. This is necessary for distributed deployments.
   * @param containerID a identifier of the JADE container
   */
  public ContainerGraphBuilder(WebContainerID containerID) {
    super(containerID.getIRI());

    this.containerID = containerID;

    if (containerID.isMain()) {
      graphBuilder.add(getDocumentIRI(), RDF.TYPE, JADE.MainContainer);
    } else {
      graphBuilder.add(getDocumentIRI(), RDF.TYPE, JADE.Container);
    }
  }

  public ContainerGraphBuilder addMetadata() {
    graphBuilder.add(getDocumentIRI(), JADE.hasName, containerID.getName());
    return this;
  }

  /**
   * Adds containment triples to the set of agents provided as argument.
   * @param agentIDs a set of FIPA Agent Identifiers
   * @return this container graph builder instance (fluid API)
   */
  public ContainerGraphBuilder addAgents(Set<WebAID> agentIDs) {
    for (WebAID aid : agentIDs) {
      graphBuilder.add(getDocumentIRI(), JADE.containsAgent, rdf.createIRI(aid.getAgentIRI()));
    }

    return this;
  }
}
