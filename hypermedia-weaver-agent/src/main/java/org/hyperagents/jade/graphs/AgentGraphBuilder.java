package org.hyperagents.jade.graphs;

import jade.core.AID;
import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.PlatformState;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;
import org.hyperagents.jade.vocabs.STNCore;

import java.util.Iterator;

public class AgentGraphBuilder extends EntityGraphBuilder {
  private final AID agentID;
  private final ContainerID containerID;
  private final IRI agentIRI;

  public AgentGraphBuilder(ContainerID containerID, AID agentID,
                           int httpPort) {
    super(containerID.getAddress(), httpPort);
    this.agentID = agentID;
    this.containerID = containerID;

    graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(FIPA.AgentIdentifier));
    agentIRI = createNonInformationResource(FIPA.identifierOf, "#agent");
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(FIPA.Agent));
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(STNCore.Agent));

    APDescription apDesc = PlatformState.getInstance().getAPDescription();
    String platformIRI = new PlatformGraphBuilder(apDesc, containerID.getAddress(), httpPort)
        .getEntityIRI();
    graphBuilder.add(agentIRI, rdf.createIRI(FIPA.hostedBy), rdf.createIRI(platformIRI));

    graphBuilder.setNamespace("stn-core", STNCore.PREFIX);
  }

  @Override
  public String getDocumentIRI() {
    ContainerGraphBuilder builder = new ContainerGraphBuilder(containerID, httpPort);
    return builder.getDocumentIRI() + "agents/" + agentID.getLocalName();
  }

  @Override
  public String getEntityIRI() {
    return agentIRI.stringValue();
  }

  public AgentGraphBuilder addMetadata() {
    graphBuilder.add(agentIRI, rdf.createIRI(FIPA.name), agentID.getName());
    graphBuilder.add(agentIRI, rdf.createIRI(JADE.localName), agentID.getLocalName());

    return this;
  }

  @SuppressWarnings("unchecked")
  public AgentGraphBuilder addAddresses() {
    return addOrderedList(FIPA.addresses, agentID.getAllAddresses());
  }

  @SuppressWarnings("unchecked")
  public AgentGraphBuilder addResolvers() {
    return addOrderedList(FIPA.resolvers, agentID.getAllResolvers());
  }

  private AgentGraphBuilder addOrderedList(String property, Iterator<String> list) {
    if (list.hasNext()) {
      BNode listNode = rdf.createBNode();
      graphBuilder.add(agentIRI, rdf.createIRI(property), listNode);

      String head = list.next();
      addStringArrayAsList(listNode, head, list);
    }

    return this;
  }

  private void addStringArrayAsList(Resource subject, String head, Iterator<String> tail) {
    graphBuilder.add(subject, RDF.TYPE, RDF.LIST);
    graphBuilder.add(subject, RDF.FIRST, head);

    if (tail.hasNext()) {
      BNode rest = rdf.createBNode();
      graphBuilder.add(subject, RDF.REST, rest);
      addStringArrayAsList(rest, tail.next(), tail);
    } else {
      graphBuilder.add(subject, RDF.REST, RDF.NIL);
    }
  }
}
