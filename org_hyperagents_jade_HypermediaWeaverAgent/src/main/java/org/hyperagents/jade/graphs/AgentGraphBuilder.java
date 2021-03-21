package org.hyperagents.jade.graphs;

import jade.core.AID;
import jade.core.ContainerID;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;
import org.hyperagents.jade.vocabs.STNCore;

public class AgentGraphBuilder extends GraphBuilder {
  private final AID agentID;
  private final ContainerID containerID;
  private final IRI agentIRI;

  public AgentGraphBuilder(ContainerID containerID, AID agentID, int httpPort) {
    super(containerID.getAddress(), httpPort);
    this.agentID = agentID;
    this.containerID = containerID;

    agentIRI = rdf.createIRI(getAgentIRI());

    graphBuilder.add(getSubjectIRI(), RDF.TYPE, rdf.createIRI(FIPA.AgentIdentifier));
    graphBuilder.add(FIPA.AgentIdentifier, FIPA.identifierOf, agentIRI);
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(FIPA.Agent));
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(STNCore.Agent));

    // Agent-Identifier has name (word, mandatory), addresses, resolvers (optional)
//    "fipa.mts.mtp.http.std"
  }

  @Override
  public String getSubjectIRI() {
    ContainerGraphBuilder builder = new ContainerGraphBuilder(containerID, httpPort);
    return builder.getSubjectIRI() + "agents/" + agentID.getLocalName();
  }

  public AgentGraphBuilder addMetadata() {
    graphBuilder.add(agentIRI, rdf.createIRI(FIPA.name), agentID.getName());
    graphBuilder.add(agentIRI, rdf.createIRI(JADE.localName), agentID.getLocalName());

    return this;
  }

  public AgentGraphBuilder addAddresses() {
    if (agentID.getAddressesArray().length == 0) {
      return this;
    }

    BNode addressList = rdf.createBNode();
    graphBuilder.add(agentIRI, rdf.createIRI(FIPA.addresses), addressList);
    graphBuilder.add(addressList, RDF.TYPE, RDF.LIST);

    // TODO this should be recursive
    for (String address : agentID.getAddressesArray()) {
      graphBuilder.add(addressList, RDF.FIRST, address);
    }

    graphBuilder.add(addressList, RDF.REST, RDF.NIL);

    return this;
  }

  public AgentGraphBuilder addResolvers() {
    // TODO
    return this;
  }

  private String getAgentIRI() {
    return getSubjectIRI() + "#agent";
  }
}
