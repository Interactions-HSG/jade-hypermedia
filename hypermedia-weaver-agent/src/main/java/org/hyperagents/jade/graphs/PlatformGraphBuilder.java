package org.hyperagents.jade.graphs;

import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.FIPAAgentManagement.APService;
import jade.util.leap.Properties;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Iterator;
import java.util.Set;

/**
 * Constructs an RDF description of a JADE platform.
 */
public class PlatformGraphBuilder extends EntityGraphBuilder {
  private final IRI platformIRI;
  private final APDescription platformDescription;

  /**
   * Constructs a platform graph builder.
   * @param config the set of properties provided as arguments to this container
   * @param platformDescription an agent platform description as defined by the JADE platform
   */
  public PlatformGraphBuilder(Properties config, APDescription platformDescription) {
    super(config);

    this.platformDescription = platformDescription;

    graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(FIPA.APDescription));
    platformIRI = createNonInformationResource(FIPA.descriptionOf, "#platform");
  }

  /**
   * Adds any platform metadata (e.g., name).
   * @return this instance of platform graph builder (fluid API)
   */
  public PlatformGraphBuilder addMetadata() {
    graphBuilder.add(platformIRI, rdf.createIRI(FIPA.name), platformDescription.getName());
    return this;
  }

  /**
   * Adds descriptions of all services provided by the platform
   * (see <a href="http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951017">FIPA Agent Management
   * Ontology</a>).
   * @return this instance of platform graph builder (fluid API)
   */
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

  /**
   * Adds triples for the set of containers provided as argument.
   * @param containerIDs a set of container identifiers
   * @return this instance of platform graph builder (fluid API)
   */
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
    ContainerGraphBuilder builder = new ContainerGraphBuilder(config, cid);
    return builder.getDocumentIRI();
  }
}
