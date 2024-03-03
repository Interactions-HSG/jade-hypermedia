package org.hyperagents.jade.graphs;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebContainerID;
import org.hyperagents.jade.vocabs.HMAS;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Set;

/**
 * Constructs an RDF description of a JADE container.
 */
public class ContainerGraphBuilder extends EntityGraphBuilder {
  private final WebContainerID containerID;
  private final IRI containerIRI;

  /**
   * Constructs a container graph builder. Unlike {@link PlatformGraphBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter. This is necessary for distributed deployments.
   * @param containerID a identifier of the JADE container
   */
  public ContainerGraphBuilder(WebContainerID containerID) {
    super(containerID.getIRI());

    this.containerID = containerID;
    this.containerIRI = rdf.createIRI(containerID.getContainerIRI());

    graphBuilder.add(getDocumentIRI(), RDF.TYPE, HMAS.ResourceProfile);
    graphBuilder.add(getDocumentIRI(), HMAS.isProfileOf, containerIRI);

    graphBuilder.add(containerIRI, RDF.TYPE, HMAS.Workspace);
    if (containerID.isMain()) {
      graphBuilder.add(containerIRI, RDF.TYPE, JADE.MainContainer);
    }
  }

  public ContainerGraphBuilder addMetadata() {
    graphBuilder.add(containerIRI, HMAS.hasProfile, getDocumentIRI());

    String platformIRI = PlatformState.getInstance().getAPDescription().getPlatformIRI();
    graphBuilder.add(containerIRI, HMAS.isHostedOn, rdf.createIRI(platformIRI));

    return this;
  }

  /**
   * Adds containment triples to the set of agents provided as argument.
   * @param agentIDs a set of FIPA Agent Identifiers
   * @return this container graph builder instance (fluid API)
   */
  public ContainerGraphBuilder addAgents(Set<WebAID> agentIDs) {
    for (WebAID aid : agentIDs) {
      IRI agentIRI = rdf.createIRI(aid.getAgentIRI());
      graphBuilder.add(containerIRI, HMAS.contains, agentIRI);
      graphBuilder.add(agentIRI, RDF.TYPE, HMAS.Agent);
    }

    return this;
  }
}
