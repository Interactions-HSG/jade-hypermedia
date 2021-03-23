package org.hyperagents.jade.graphs;

import jade.core.AID;
import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.util.leap.Properties;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.PlatformState;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.JADE;
import org.hyperagents.jade.vocabs.STNCore;

import java.util.Iterator;

/**
 * Constructs an RDF description of a JADE agent.
 */
public class AgentGraphBuilder extends EntityGraphBuilder {
  private final AID agentID;
  private final ContainerID containerID;
  private final IRI agentIRI;

  /**
   * Constructs an agent graph builder. Unlike {@link PlatformGraphBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter, which identies the agent's container. This is necessary for distributed deployments.
   * @param config the set of properties provided as arguments to this container
   * @param containerID the JADE container identifier for this agent's container
   * @param agentID a JADE agent identifier
   */
  public AgentGraphBuilder(Properties config, ContainerID containerID, AID agentID) {
    super(config);

    // TODO: configure HTTP port as well, but this has to be handled by HWAs
    this.config.setProperty("http-host", containerID.getAddress());

    this.agentID = agentID;
    this.containerID = containerID;

    graphBuilder.add(getDocumentIRI(), RDF.TYPE, rdf.createIRI(FIPA.AgentIdentifier));
    agentIRI = createNonInformationResource(FIPA.identifierOf, "#agent");
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(FIPA.Agent));
    graphBuilder.add(agentIRI, RDF.TYPE, rdf.createIRI(STNCore.Agent));

    APDescription apDesc = PlatformState.getInstance().getAPDescription();
    String platformIRI = new PlatformGraphBuilder(config, apDesc)
        .getEntityIRI();
    graphBuilder.add(agentIRI, rdf.createIRI(FIPA.hostedBy), rdf.createIRI(platformIRI));

    graphBuilder.setNamespace("stn-core", STNCore.PREFIX);
  }

  /**
   * Adds any agent metadata (e.g., local name, fully qualified name).
   * @return this instance of agent graph builder (fluid API)
   */
  public AgentGraphBuilder addMetadata() {
    graphBuilder.add(agentIRI, rdf.createIRI(FIPA.name), agentID.getName());
    graphBuilder.add(agentIRI, rdf.createIRI(JADE.localName), agentID.getLocalName());

    return this;
  }

  /**
   * Adds all message transport addresses defined in this agent's the FIPA Agent Identifier. The
   * addresses are exposed as an ordered RDF list, where the order implies the agent's preference
   * (see <a href="http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951012">FIPA Agent Management
   * Ontology</a>).
   * @return this instance of agent graph builder (fluid API)
   */
  @SuppressWarnings("unchecked")
  public AgentGraphBuilder addAddresses() {
    return addOrderedList(FIPA.addresses, agentID.getAllAddresses());
  }

  /**
   * Adds all resolvers defined in this agent's the FIPA Agent Identifier. The resolvers are exposed
   * as an ordered RDF list, where the order implies the agent's preference
   * (see <a href="http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951012">FIPA Agent Management
   * Ontology</a>).
   * @return this instance of agent graph builder (fluid API)
   */
  @SuppressWarnings("unchecked")
  public AgentGraphBuilder addResolvers() {
    return addOrderedList(FIPA.resolvers, agentID.getAllResolvers());
  }

  @Override
  protected String getDocumentIRI() {
    ContainerGraphBuilder builder = new ContainerGraphBuilder(config, containerID);
    return builder.getDocumentIRI() + "agents/" + agentID.getLocalName();
  }

  @Override
  protected String getEntityIRI() {
    return agentIRI.stringValue();
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
