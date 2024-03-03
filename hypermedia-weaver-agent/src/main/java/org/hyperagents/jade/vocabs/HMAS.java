package org.hyperagents.jade.vocabs;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class HMAS {
  public static final String PREFIX = "https://purl.org/hmas/";

  private static IRI createIRI(String term) {
    ValueFactory rdf = SimpleValueFactory.getInstance();
    return rdf.createIRI(PREFIX + term);
  }

  public static final IRI Agent = createIRI("Agent");
  public static final IRI Artifact = createIRI("Artifact");
  public static final IRI ResourceProfile = createIRI("ResourceProfile");
  public static final IRI Signifier = createIRI("Signifier");
  public static final IRI Workspace = createIRI("Workspace");
  public static final IRI HypermediaMASPlatform = createIRI("HypermediaMASPlatform");

  public static final IRI AuthorizationHeader = createIRI("AuthorizationHeader");

  public static final IRI hasProfile = createIRI("hasProfile");
  public static final IRI isProfileOf = createIRI("isProfileOf");
  public static final IRI hasSignifier = createIRI("hasSignifier");
  public static final IRI exposesSignifiersFrom = createIRI("exposesSignifiersFrom");


  public static final IRI globalSpecification = createIRI("globalSpecification");
  public static final IRI isHostedOn = createIRI("isHostedOn");
  public static final IRI contains = createIRI("contains");
  public static final IRI isContainedIn = createIRI("isContainedIn");
}
