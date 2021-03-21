package org.hyperagents.jade.graphs;

import jade.core.AID;
import jade.core.ContainerID;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;
import org.hyperagents.jade.vocabs.STNCore;

import java.util.Iterator;

public class AgentGraphBuilder extends GraphBuilder {
  private final AID agentID;
  private final ContainerID containerID;
  private final IRI agentIRI;

  public AgentGraphBuilder(ContainerID containerID, AID agentID, int httpPort) {
    super(containerID.getAddress(), httpPort);
    this.agentID = agentID;
    this.containerID = containerID;

    graphBuilder.add(getSubjectIRI(), RDF.TYPE, rdf.createIRI(FIPA.AgentIdentifier));
    agentIRI = addNonInformationResource(FIPA.identifierOf, "#agent");
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(FIPA.Agent));

    graphBuilder.setNamespace("stn-core", STNCore.PREFIX);
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(STNCore.Agent));
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
