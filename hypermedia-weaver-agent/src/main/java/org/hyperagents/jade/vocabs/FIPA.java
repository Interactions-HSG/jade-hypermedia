package org.hyperagents.jade.vocabs;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class FIPA {
  public static final String PREFIX = "http://hyperagents.org/ns/fipa#";

  private static IRI createIRI(String term) {
    ValueFactory rdf = SimpleValueFactory.getInstance();
    return rdf.createIRI(PREFIX + term);
  }

  /* Classes */
  // The following terms are defined in: http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951010
  public static final IRI AgentIdentifier = createIRI("AgentIdentifier");
  public static final IRI APDescription = createIRI("APDescription");
  // Non-standard terms:
  public static final IRI Agent = createIRI("Agent");

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
  public static final IRI name = createIRI("name");
  public static final IRI type = createIRI("type");
}
