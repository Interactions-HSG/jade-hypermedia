package org.hyperagents.jade.graphs;

import ch.unisg.ics.interactions.hmas.core.hostables.HypermediaMASPlatform;
import ch.unisg.ics.interactions.hmas.core.hostables.ProfiledResource;
import ch.unisg.ics.interactions.hmas.core.hostables.Workspace;
import jade.domain.FIPAAgentManagement.APService;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.platform.WebContainerID;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Iterator;
import java.util.Set;

/**
 * Constructs an <a href="https://purl.org/hmas/ResourceProfile">hmas:ResourceProfile</a> of a JADE platform.
 */
public class PlatformProfileBuilder extends ResourceProfileBuilder {
  private final WebAPDescription apDesc;

  private final HypermediaMASPlatform.Builder builder;

  /**
   * Constructs a platform graph builder.
   * @param platformDescription an agent platform description as defined by the JADE platform
   */
  public PlatformProfileBuilder(WebAPDescription platformDescription) {
    super(platformDescription.getIRI());

    apDesc = platformDescription;

    // TODO: hmas-java support needed!
    //graphBuilder.add(getDocumentIRI(), HMAS.exposesSignifiersFrom, JADE.JadeMessagingCatalog);
    //graphBuilder.add(getDocumentIRI(), HMAS.globalSpecification, HMAS.AuthorizationHeader);

    builder = new HypermediaMASPlatform.Builder()
      .setIRIAsString(apDesc.getPlatformIRI());
  }

  @Override
  protected ProfiledResource build() {
    return builder.build();
  }

  /**
   * Adds any platform metadata (e.g., name).
   * @return this instance of platform graph builder (fluid API)
   */
  public PlatformProfileBuilder addMetadata() {
    // The string encoding the platform name may come with leading and trailing double quotes
    // TODO: add once we update hmmas-java to support additional triples
    //graphBuilder.add(apDesc.getPlatformIRI(), FIPA.serviceName, apDesc.getName().replaceAll("^\"|\"$", ""));

    return this;
  }

  /**
   * Adds descriptions of all services provided by the platform
   * (see <a href="http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951017">FIPA Agent Management
   * Ontology</a>).
   * @return this instance of platform graph builder (fluid API)
   */
  @SuppressWarnings("unchecked")
  public PlatformProfileBuilder addAPServices() {
    Iterator<APService> services = apDesc.getAllAPServices();

    while (services.hasNext()) {
      APService service = services.next();
      addSignifiersForTransportAddresses(service.getAllAddresses());
    }

    return this;
  }

  /**
   * Adds triples for the set of containers provided as argument.
   * @param containerIDs a set of container identifiers
   * @return this instance of platform graph builder (fluid API)
   */
  public PlatformProfileBuilder addContainers(Set<WebContainerID> containerIDs) {
    for (WebContainerID cid : containerIDs) {
      Workspace.Builder wkspBuilder = new Workspace.Builder()
        .setIRIAsString(cid.getContainerIRI());

      if (cid.isMain()) {
        wkspBuilder.addSemanticType(JADE.MainContainer.stringValue());
      }

      builder.addHostedResource(wkspBuilder.build());
    }

    return this;
  }
}
