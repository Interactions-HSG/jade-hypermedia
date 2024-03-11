package org.hyperagents.jade.graphs;

import ch.unisg.ics.interactions.hmas.core.hostables.Agent;
import ch.unisg.ics.interactions.hmas.core.hostables.HypermediaMASPlatform;
import ch.unisg.ics.interactions.hmas.core.hostables.ProfiledResource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebAPDescription;
import org.hyperagents.jade.vocabs.FIPA;
import org.hyperagents.jade.vocabs.HMAS;
import org.hyperagents.jade.vocabs.JADE;

/**
 * Constructs an <a href="https://purl.org/hmas/ResourceProfile">hmas:ResourceProfile</a> of a JADE agent.
 */
public class AgentProfileBuilder extends ResourceProfileBuilder {
  private final WebAID agentID;

  private final Agent.Builder builder;

  /**
   * Constructs an agent profile builder. Unlike {@link PlatformProfileBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter, which identifies the agent's container. This is necessary for distributed deployments.
   * @param agentID the identifier of the JADE agent
   */
  public AgentProfileBuilder(WebAID agentID) {
    super(agentID.getIRI());

    this.agentID = agentID;

    builder = new Agent.Builder().setIRIAsString(agentID.getAgentIRI());
  }

  @Override
  protected ProfiledResource build() {
    return builder.build();
  }

  /**
   * Adds any agent metadata (e.g., local name, fully qualified name).
   * @return this instance of agent graph builder (fluid API)
   */
  public AgentProfileBuilder addMetadata() {
    ValueFactory rdf = SimpleValueFactory.getInstance();

    builder.addTriple(FIPA.agentName, rdf.createLiteral(agentID.getName()));
    builder.addTriple(JADE.localName, rdf.createLiteral(agentID.getLocalName()));

    PlatformState state = PlatformState.getInstance();
    String mainEndpoint  = state.getMainContainerEndpoint();
    WebAPDescription apDesc = state.getAPDescription();

    if (apDesc != null) {
      if (mainEndpoint != null) {
        apDesc = new WebAPDescription(apDesc.getApDescription(), mainEndpoint);
      }

      builder.addHMASPlatform(new HypermediaMASPlatform.Builder().setIRIAsString(apDesc.getPlatformIRI())
        .build());
    }

    builder.addTriple(FIPA.homeAgentPlatform, rdf.createLiteral(agentID.getAID().getHap()));
    builder.addTriple(HMAS.isContainedIn, rdf.createIRI(agentID.getContainerIRI()));

    return this;
  }

  /**
   * Adds all message transport addresses defined in this agent's FIPA Agent Identifier. The
   * addresses are exposed via signifiers.
   * @return this instance of agent profile builder (fluid API)
   */
  @SuppressWarnings("unchecked")
  public AgentProfileBuilder addAddresses() {
    addSignifiersForTransportAddresses(agentID.getAllAddresses());
    return this;
  }
}
