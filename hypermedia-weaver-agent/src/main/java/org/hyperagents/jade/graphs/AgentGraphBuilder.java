package org.hyperagents.jade.graphs;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.HyperAgents;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Iterator;

/**
 * Constructs an RDF description of a JADE agent.
 */
public class AgentGraphBuilder extends EntityGraphBuilder {
  private final WebAID agentID;

  /**
   * Constructs an agent graph builder. Unlike {@link PlatformGraphBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter, which identifies the agent's container. This is necessary for distributed deployments.
   * @param agentID the identifier of the JADE agent
   */
  public AgentGraphBuilder(WebAID agentID) {
    super(agentID.getIRI());

    this.agentID = agentID;

    graphBuilder.add(getDocumentIRI(), RDF.TYPE, FIPA.AgentIdentifierDescription);
    graphBuilder.add(agentID.getAgentIRI(), RDF.TYPE, HyperAgents.Agent);
    graphBuilder.add(getDocumentIRI(), HyperAgents.describes, rdf.createIRI(agentID.getAgentIRI()));

    if (agentID.getAllAddresses().hasNext() || agentID.getAllResolvers().hasNext()) {
      graphBuilder.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
    }

    graphBuilder.setNamespace("fipa", FIPA.PREFIX);
  }

  /**
   * Adds any agent metadata (e.g., local name, fully qualified name).
   * @return this instance of agent graph builder (fluid API)
   */
  public AgentGraphBuilder addMetadata() {
    graphBuilder.add(agentID.getAgentIRI(), FIPA.serviceName, agentID.getName());
    graphBuilder.add(agentID.getAgentIRI(), JADE.localName, agentID.getLocalName());

    PlatformState state = PlatformState.getInstance();
    String mainEndpoint  = state.getMainContainerEndpoint();
    WebAPDescription apDesc = state.getAPDescription();

    if (apDesc != null) {
      if (mainEndpoint != null) {
        apDesc = new WebAPDescription(apDesc.getApDescription(), mainEndpoint);
      }
      graphBuilder.add(agentID.getAgentIRI(), FIPA.hostedBy, rdf.createIRI(apDesc.getPlatformIRI()));
    }
    graphBuilder.add(agentID.getAgentIRI(), FIPA.homeAgentPlatform, agentID.getAID().getHap());
    graphBuilder.add(agentID.getAgentIRI(), FIPA.homeContainer,
      rdf.createIRI(agentID.getContainerIRI()));

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

  private AgentGraphBuilder addOrderedList(IRI property, Iterator<String> list) {
    if (list.hasNext()) {
      BNode listNode = rdf.createBNode();
      graphBuilder.add(getDocumentIRI(), property, listNode);

      String head = list.next();
      addStringArrayAsList(listNode, head, list);
    }

    return this;
  }

  private void addStringArrayAsList(Resource subject, String head, Iterator<String> tail) {
    graphBuilder.add(subject, RDF.TYPE, RDF.LIST);
    graphBuilder.add(subject, RDF.FIRST, rdf.createIRI(head));

    if (tail.hasNext()) {
      BNode rest = rdf.createBNode();
      graphBuilder.add(subject, RDF.REST, rest);
      addStringArrayAsList(rest, tail.next(), tail);
    } else {
      graphBuilder.add(subject, RDF.REST, RDF.NIL);
    }
  }
}
