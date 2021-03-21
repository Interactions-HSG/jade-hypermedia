package org.hyperagents.jade;

import jade.core.ContainerID;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class ContainerGraphBuilder extends GraphBuilder {
  private final ContainerID containerID;

  public ContainerGraphBuilder(ContainerID containerID, int httpPort) {
    super(containerID.getAddress(), httpPort, "/containers/");

    this.containerID = containerID;

    if (containerID.getMain()) {
      graphBuilder.add(getSubjectIRI(), RDF.TYPE, rdf.createIRI(JADE.MainContainer));
    } else {
      graphBuilder.add(getSubjectIRI(), RDF.TYPE, rdf.createIRI(JADE.Container));
    }
  }

  @Override
  public String getSubjectIRI() {
    return constructRelativeIRI() + containerID.getName();
  }
}
