package org.hyperagents.jade.graphs;

import ch.unisg.ics.interactions.hmas.core.hostables.Agent;
import ch.unisg.ics.interactions.hmas.core.hostables.HypermediaMASPlatform;
import ch.unisg.ics.interactions.hmas.core.hostables.ProfiledResource;
import ch.unisg.ics.interactions.hmas.core.hostables.Workspace;
import org.hyperagents.jade.platform.PlatformState;
import org.hyperagents.jade.platform.WebAID;
import org.hyperagents.jade.platform.WebContainerID;
import org.hyperagents.jade.vocabs.JADE;

import java.util.Set;

/**
 * Constructs an <a href="https://purl.org/hmas/ResourceProfile">hmas:ResourceProfile</a> of a JADE container.
 */
public class ContainerProfileBuilder extends ResourceProfileBuilder {
  private final Workspace.Builder builder;

  /**
   * Constructs a container profile builder. Unlike {@link PlatformProfileBuilder}, this builder uses as
   * the HTTP authority for exposed IRIs the address specified by the JADE container identifier passed
   * as a parameter. This is necessary for distributed deployments.
   * @param containerID an identifier of the JADE container
   */
  public ContainerProfileBuilder(WebContainerID containerID) {
    super(containerID.getIRI());

    builder = new Workspace.Builder()
      .setIRIAsString(containerID.getContainerIRI());

    if (containerID.isMain()) {
      builder.addSemanticType(JADE.MainContainer.toString());
    }
  }

  @Override
  protected ProfiledResource build() {
    return builder.build();
  }

  public ContainerProfileBuilder addMetadata() {
    String platformIRI = PlatformState.getInstance().getAPDescription().getPlatformIRI();
    builder.addHMASPlatform(new HypermediaMASPlatform.Builder().setIRIAsString(platformIRI).build());

    return this;
  }

  /**
   * Adds containment triples to the set of agents provided as argument.
   * @param agentIDs a set of FIPA Agent Identifiers
   * @return this container graph builder instance (fluid API)
   */
  public ContainerProfileBuilder addAgents(Set<WebAID> agentIDs) {
    for (WebAID aid : agentIDs) {
      Agent agent = new Agent.Builder()
        .setIRIAsString(aid.getAgentIRI())
        .build();

      builder.addContainedResource(agent);
    }

    return this;
  }
}
