package org.hyperagents.jade.graphs;

import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.FIPAAgentManagement.APService;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Iterator;
import java.util.Set;

public class PlatformGraphBuilder extends GraphBuilder {
  private final IRI platformIRI;
  private final APDescription platformDescription;

  public PlatformGraphBuilder(APDescription platformDescription, String address, int httpPort) {
    super(address, httpPort);

    this.platformDescription = platformDescription;

    graphBuilder.add(getSubjectIRI(), RDF.TYPE, rdf.createIRI(FIPA.APDescription));
    platformIRI = addNonInformationResource(FIPA.descriptionOf, "#platform");
  }

  public PlatformGraphBuilder addMetadata() {
    graphBuilder.add(platformIRI, rdf.createIRI(FIPA.name), platformDescription.getName());
    return this;
  }

  public PlatformGraphBuilder addAPServices() {
    @SuppressWarnings("unchecked")
    Iterator<APService> services = platformDescription.getAllAPServices();

    while (services.hasNext()) {
      APService service = services.next();
      BNode serviceNode = rdf.createBNode();
      graphBuilder.add(platformIRI, rdf.createIRI(FIPA.apService), serviceNode);
      addAPService(serviceNode, service);
    }

    return this;
  }

  public PlatformGraphBuilder addContainers(Set<ContainerID> containerIDs) {
    // Add containment triples from main-container to all containers
    for (ContainerID cid : containerIDs) {
      if (cid.getMain()) {
        graphBuilder.add(platformIRI, rdf.createIRI(JADE.hasMainContainer),
            rdf.createIRI(constructContainerIRI(cid)));
      } else {
        graphBuilder.add(platformIRI, rdf.createIRI(JADE.hasContainer),
            rdf.createIRI(constructContainerIRI(cid)));
      }
    }

    return this;
  }

  private void addAPService(Resource serviceNode, APService service) {
    graphBuilder.add(serviceNode, rdf.createIRI(FIPA.name), service.getName());
    graphBuilder.add(serviceNode, rdf.createIRI(FIPA.type), service.getType());

    @SuppressWarnings("unchecked")
    Iterator<String> iterator = service.getAllAddresses();

    while (iterator.hasNext()) {
      String address = iterator.next();
      graphBuilder.add(serviceNode, rdf.createIRI(FIPA.address), address);
    }
  }

  private String constructContainerIRI(ContainerID cid) {
    ContainerGraphBuilder builder = new ContainerGraphBuilder(cid, httpPort);
    return builder.getSubjectIRI();
  }
}
