package org.hyperagents.jade.vocabs;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class HyperAgents {
  public static final String PREFIX = "http://ns.hyperagents.org/core#";

  private static IRI createIRI(String term) {
    ValueFactory rdf = SimpleValueFactory.getInstance();
    return rdf.createIRI(PREFIX + term);
  }

  public static final IRI Agent = createIRI("Agent");
  public static final IRI Artifact = createIRI("Artifact");
  public static final IRI ResourceProfile = createIRI("ResourceProfile");
  public static final IRI Signifier = createIRI("Signifier");
  public static final IRI Workspace = createIRI("Workspace");
  public static final IRI Platform = createIRI("Platform");

  public static final IRI hasSignifier = createIRI("hasSignifier");
  public static final IRI describes = createIRI("describes");
  public static final IRI hostsWorkspace = createIRI("hostsWorkspace");

  public static final IRI contains = createIRI("contains");
  public static final IRI containsAgent = createIRI("containsAgent");
}
