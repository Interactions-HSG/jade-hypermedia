package org.hyperagents.jade.graphs;

import jade.domain.FIPAAgentManagement.APService;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.platform.WebContainerID;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.HyperAgents;
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
    super(platformDescription.getIRI());

    apDesc = platformDescription;

    graphBuilder.add(getDocumentIRI(), RDF.TYPE, FIPA.AgentPlatformDescription);
    graphBuilder.add(getDocumentIRI(), HyperAgents.describes, rdf.createIRI(apDesc.getPlatformIRI()));
  }

  /**
   * Adds any platform metadata (e.g., name).
   * @return this instance of platform graph builder (fluid API)
   */
  public PlatformGraphBuilder addMetadata() {
    // The string encoding the platform name may come with leading and trailing double quotes
    graphBuilder.add(apDesc.getPlatformIRI(), RDF.TYPE, FIPA.AgentPlatform);
    graphBuilder.add(apDesc.getPlatformIRI(), FIPA.name, apDesc.getName().replaceAll("^\"|\"$", ""));

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

      BNode signifierNode = rdf.createBNode();
      graphBuilder.add(getDocumentIRI(), HyperAgents.hasSignifier, signifierNode);
      graphBuilder.add(signifierNode, RDF.TYPE, HyperAgents.Signifier);
      graphBuilder.add(signifierNode, HyperAgents.describes, serviceNode);

      graphBuilder.setNamespace("fipa", FIPA.PREFIX);

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
        LOGGER.info("Adding triple for container: " + cid.getIRI());
        graphBuilder.add(apDesc.getPlatformIRI(), JADE.hostsMainContainer, rdf.createIRI(cid.getIRI()));
      } else {
        LOGGER.info("Adding triple for container: " + cid.getIRI());
        graphBuilder.add(apDesc.getPlatformIRI(), JADE.hostsContainer, rdf.createIRI(cid.getIRI()));
      }
    }

    return this;
  }

  private void addAPService(Resource serviceNode, APService service) {
    graphBuilder.add(serviceNode, RDF.TYPE, FIPA.APService);
    graphBuilder.add(serviceNode, FIPA.name, service.getName());

    // Check the service type for the two standard values defined by the <a
    // href="http://fipa.org/specs/fipa00067/SC00067F.html">FIPA Agent Message Transport Service
    // Specfication</a>
    if (service.getType().equalsIgnoreCase(FIPA.MTP_HTTP_STD)) {
      graphBuilder.add(serviceNode, FIPA.type, FIPA.HTTPMessageTransportProtocol);
    } else if (service.getType().equalsIgnoreCase(FIPA.MTP_IIOP_STD)) {
      graphBuilder.add(serviceNode, FIPA.type, FIPA.IIOPMessageTransportProtocol);
    } else {
      graphBuilder.add(serviceNode, FIPA.type, service.getType());
    }

    @SuppressWarnings("unchecked")
    Iterator<String> iterator = service.getAllAddresses();

    while (iterator.hasNext()) {
      String address = iterator.next();
      graphBuilder.add(serviceNode, FIPA.address, rdf.createIRI(address));
    }
  }
}
