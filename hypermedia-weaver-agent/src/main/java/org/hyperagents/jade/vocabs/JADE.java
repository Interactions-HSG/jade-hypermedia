package org.hyperagents.jade.vocabs;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class JADE {
  public static final String PREFIX = "http://ns.hyperagents.org/jade#";

  private static IRI createIRI(String term) {
    ValueFactory rdf = SimpleValueFactory.getInstance();
    return rdf.createIRI(PREFIX + term);
  }

  /* Classes */
  public static final IRI MainContainer = createIRI("MainContainer");
  public static final IRI Container = createIRI("Container");

  public static final IRI AgentManagementSystem = createIRI("AgentManagementSystem");
  public static final IRI DirectoryFacilitator = createIRI("DirectoryFacilitator");
  public static final IRI RemoteMonitoringAgent = createIRI("RemoteMonitoringAgent");
  public static final IRI HypermediaWeaverAgent = createIRI("HypermediaWeaverAgent");

  /* Object properties */
  public static final IRI hostsMainContainer = createIRI("hostsMainContainer");
  public static final IRI hostsContainer = createIRI("hostsContainer");
  public static final IRI containsAgent = createIRI("containsAgent");

  /* Data properties */
  public static final IRI hasName = createIRI("hasName");
  public static final IRI localName = createIRI("hasLocalName");
}
