package org.hyperagents.jade.vocabs;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class STNCore {
  public static final String PREFIX = "http://w3id.org/stn/core#";

  private static IRI createIRI(String term) {
    ValueFactory rdf = SimpleValueFactory.getInstance();
    return rdf.createIRI(PREFIX + term);
  }

  public static final IRI Agent = createIRI("Agent");
}
