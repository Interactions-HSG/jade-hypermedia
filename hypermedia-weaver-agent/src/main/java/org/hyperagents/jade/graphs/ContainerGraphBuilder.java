package org.hyperagents.jade.graphs;

import jade.core.AID;
import jade.core.ContainerID;
import jade.util.leap.Properties;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Set;

/**
 * Constructs an RDF description of a JADE container.
 */
public class ContainerGraphBuilder extends EntityGraphBuilder {
  private final ContainerID containerID;

  /**
   * Constructs a container graph builder. Unlike {@link PlatformGraphBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter. This is necessary for distributed deployments.
   * @param config the set of properties provided as arguments to this container
   * @param containerID a JADE container identifier
   */
  public ContainerGraphBuilder(Properties config, ContainerID containerID) {
    super(config);

    // TODO: configure HTTP port as well, but this has to be handled by HWAs
    this.config.setProperty("http-host", containerID.getAddress());
    setBaseIRI();

    this.containerID = containerID;

    if (containerID.getMain()) {
      graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(JADE.MainContainer));
    } else {
      graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(JADE.Container));
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
  public ContainerGraphBuilder addAgents(Set<AID> agentIDs) {
    for (AID aid : agentIDs) {
      AgentGraphBuilder agentGraph = new AgentGraphBuilder(config, containerID, aid);
      graphBuilder.add(getDocumentIRI(), JADE.containsAgent, rdf.createIRI(agentGraph.getEntityIRI()));
    }

    return this;
  }

  @Override
  protected String getDocumentIRI() {
    return baseIRI + "containers/" + containerID.getName() + "/";
  }
}
