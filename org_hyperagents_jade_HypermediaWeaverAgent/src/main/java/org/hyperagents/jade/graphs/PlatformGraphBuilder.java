package org.hyperagents.jade.graphs;

import jade.core.ContainerID;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.PlatformState;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Set;

public class PlatformGraphBuilder extends GraphBuilder {

  public PlatformGraphBuilder(String address, int httpPort) {
    super(address, httpPort);

    graphBuilder.add(getSubjectIRI(), RDF.TYPE, rdf.createIRI(JADE.Platform));
  }

  public PlatformGraphBuilder addMetadata() {
    graphBuilder.add(getSubjectIRI(), JADE.hasName, PlatformState.getInstance().getPlatformName());
    return this;
  }

  public PlatformGraphBuilder addContainers(Set<ContainerID> containerIDs) {
    // Add containment triples from main-container to all containers
    for (ContainerID cid : containerIDs) {
      if (cid.getMain()) {
        graphBuilder.add(getSubjectIRI(), JADE.hasMainContainer, rdf.createIRI(constructContainerIRI(cid)));
      } else {
        graphBuilder.add(getSubjectIRI(), JADE.hasContainer, rdf.createIRI(constructContainerIRI(cid)));
      }
    }

    return this;
  }

  private String constructContainerIRI(ContainerID cid) {
    ContainerGraphBuilder builder = new ContainerGraphBuilder(cid, httpPort);
    return builder.getSubjectIRI();
  }
}
