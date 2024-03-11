package org.hyperagents.jade.graphs;

import ch.unisg.ics.interactions.hmas.core.hostables.ProfiledResource;
import ch.unisg.ics.interactions.hmas.interaction.io.ResourceProfileGraphWriter;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.ActionSpecification;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.Form;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.ResourceProfile;
import ch.unisg.ics.interactions.hmas.interaction.signifiers.Signifier;
import jade.util.Logger;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The resource profile builder is used to construct an
 * <a href="https://purl.org/hmas/ResourceProfile">hmas:ResourceProfile</a> of a resource in JADE.
 * The constructed profile is represented in RDF and is an information resource that may describe a
 * non-information resource (e.g., an agent). The current implementation is based on hmas-java.
 */
public abstract class ResourceProfileBuilder {
  protected final static Logger LOGGER = Logger.getJADELogger(ResourceProfileBuilder.class.getName());

  private final String baseIRI;
  private final Set<Signifier> signifiers;

  /**
   * Constructs an entity graph builder.
   * @param iri the IRI that identifies the main subject of this graph
   */
  public ResourceProfileBuilder(String iri) {
    baseIRI = iri;
    signifiers = new HashSet<>();
  }

  /**
   * Serializes the constructed RDF graph as a string.
   * @return a string serialization of the constructed graph
   */
  public String write() {
    ResourceProfile profile = new ResourceProfile.Builder(build())
      .setIRIAsString(getDocumentIRI())
      .addSemanticType(FIPA.AgentIdentifierDescription.toString())
      .exposeSignifiers(signifiers)
      .build();

    return new ResourceProfileGraphWriter(profile)
        .setNamespace("fipa", FIPA.PREFIX)
        .setNamespace("jade", JADE.PREFIX)
        .write();
  }

  /**
   * Builds the profiled resource. In the current implementation of hmas-java, the resource builder
   * is specific to the resource type.
   * @return the profiled resource
   */
  protected abstract ProfiledResource build();

  /**
   * Retrieves the IRI of the main subject of this graph. The main subject typically identifies an
   * information resources.
   * @return the document's IRI
   */
  protected String getDocumentIRI() {
    return baseIRI;
  }

  /**
   * Adds a signifier to the resource profile.
   * @param signifier the signifier to be exposed
   * @return this instance of resource profile builder (fluid API)
   */
  protected ResourceProfileBuilder addSignifier(Signifier signifier) {
    signifiers.add(signifier);
    return this;
  }

  /**
   * Creates signifiers for all message transport addresses passed via the iterator.
   * @return this instance of resource profile builder (fluid API)
   */
  protected ResourceProfileBuilder addSignifiersForTransportAddresses(Iterator<String> addresses) {
    while (addresses.hasNext()) {
      String address = addresses.next();
      int count = 0;

      Form form = new Form.Builder(address)
        .setMethodName("POST")
        .setContentType("multipart-mixed")
        .setIRIAsString(getDocumentIRI() + "#form" + ++count)
        .build();

      // TODO: Update after semantic type issue is solved in hmas-java
      // Check the service type for the two standard values defined by the <a
      // href="http://fipa.org/specs/fipa00067/SC00067F.html">FIPA Agent Message Transport Service
      // Specfication</a>
//    if (service.getType().equalsIgnoreCase(FIPA.MTP_HTTP_STD)) {
//      graphBuilder.add(serviceNode, FIPA.serviceType, FIPA.HTTPMessageTransportService);
//    } else if (service.getType().equalsIgnoreCase(FIPA.MTP_IIOP_STD)) {
//      graphBuilder.add(serviceNode, FIPA.serviceType, FIPA.IIOPMessageTransportService);
//    } else {
//      graphBuilder.add(serviceNode, FIPA.serviceType, service.getType());
//    }

      addSignifier(new Signifier.Builder(new ActionSpecification.Builder(form)
          .addSemanticType(FIPA.HTTPMessageTransportService.toString())
          .build())
        .build()
      );
    }

    return this;
  }
}
