package org.hyperagents.jade.graphs;

import jade.domain.FIPAAgentManagement.APService;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.platform.WebContainerID;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Iterator;
import java.util.Set;

/**
 * Constructs an RDF description of a JADE platform.
 */
public class PlatformGraphBuilder extends EntityGraphBuilder {
  private final WebAPDescription apDesc;

  /**
   * Constructs a platform graph builder.
   * @param platformDescription an agent platform description as defined by the JADE platform
   */
  public PlatformGraphBuilder(WebAPDescription platformDescription) {
    super(platformDescription.getEndpoint());

    this.apDesc = platformDescription;

    graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(FIPA.APDescription));
    graphBuilder.add(getDocumentIRI(), FIPA.descriptionOf, platformDescription.getPlatformIRI());
  }

  /**
   * Adds any platform metadata (e.g., name).
   * @return this instance of platform graph builder (fluid API)
   */
  public PlatformGraphBuilder addMetadata() {
    graphBuilder.add(apDesc.getPlatformIRI(), rdf.createIRI(FIPA.name), apDesc.getName());
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
    Iterator<APService> services = apDesc.getAllAPServices();

    while (services.hasNext()) {
      APService service = services.next();
      BNode serviceNode = rdf.createBNode();
      graphBuilder.add(apDesc.getPlatformIRI(), rdf.createIRI(FIPA.apService), serviceNode);
      addAPService(serviceNode, service);
    }

    return this;
  }

  /**
   * Adds triples for the set of containers provided as argument.
   * @param containerIDs a set of container identifiers
   * @return this instance of platform graph builder (fluid API)
   */
  public PlatformGraphBuilder addContainers(Set<WebContainerID> containerIDs) {
    // Add containment triples from main-container to all containers
    for (WebContainerID cid : containerIDs) {
      if (cid.isMain()) {
        graphBuilder.add(apDesc.getPlatformIRI(), rdf.createIRI(JADE.hasMainContainer),
            rdf.createIRI(cid.getEndpoint()));
      } else {
        graphBuilder.add(apDesc.getPlatformIRI(), rdf.createIRI(JADE.hasContainer),
            rdf.createIRI(cid.getEndpoint()));
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
}
