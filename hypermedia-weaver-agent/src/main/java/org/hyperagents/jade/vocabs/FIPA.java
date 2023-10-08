package org.hyperagents.jade.vocabs;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class FIPA {
  public static final String PREFIX = "http://ns.hyperagents.org/fipa#";
  public static final String MTP_HTTP_STD = "fipa.mts.mtp.http.std";
  public static final String MTP_IIOP_STD = "fipa.mts.mtp.iiop.std";

  private static IRI createIRI(String term) {
    ValueFactory rdf = SimpleValueFactory.getInstance();
    return rdf.createIRI(PREFIX + term);
  }

  /* Classes */
  // The following terms are defined in: http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951010
  public static final IRI AgentIdentifierDescription = createIRI("AgentIdentifierDescription");
  public static final IRI AgentPlatformDescription = createIRI("AgentPlatformDescription");
  public static final IRI APService = createIRI("APService");
  // Non-standard terms:
  public static final IRI Agent = createIRI("Agent");
  public static final IRI AgentPlatform = createIRI("AgentPlatform");
  public static final IRI HTTPMessageTransportService = createIRI("HTTPMessageTransportService");
  public static final IRI IIOPMessageTransportService = createIRI("IIOPMessageTransportService");

  /* Object properties */
  // The following terms are defined in: http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951010
  public static final IRI addresses = createIRI("addresses");
  public static final IRI resolvers = createIRI("resolvers");
  public static final IRI apService = createIRI("apService");
  // Non-standard terms:
  public static final IRI address = createIRI("address");
  public static final IRI identifierOf = createIRI("identifierOf");
  public static final IRI descriptionOf = createIRI("descriptionOf");
  public static final IRI hostedBy = createIRI("hostedBy");
  public static final IRI homeAgentPlatform = createIRI("homeAgentPlatform");
  public static final IRI homeContainer = createIRI("homeContainer");

  /* Data properties */
  public static final IRI serviceName = createIRI("serviceName");
  public static final IRI serviceType = createIRI("serviceType");
}
