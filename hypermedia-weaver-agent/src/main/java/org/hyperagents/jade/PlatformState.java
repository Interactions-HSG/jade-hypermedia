package org.hyperagents.jade;

import jade.core.AID;
import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.util.Logger;

import java.util.*;

/**
 * Singleton used by the Hypermedia Weaver Agent controlling this instance to record platform state.
 * The {@link HypermediaInterface} retrieves and exposes data from this singleton.
 */
public class PlatformState {
  private final static Logger LOGGER = Logger.getJADELogger(PlatformState.class.getName());

  private static PlatformState platform;

  private APDescription platformDescription;
  private final Set<ContainerID> containerIDs;
  private final Map<ContainerID, Set<AID>> containedAgents;

  private PlatformState() {
    platformDescription = null;
    containerIDs = new HashSet<>();
    containedAgents = new Hashtable<>();
  }

  public static synchronized PlatformState getInstance() {
    if (platform == null) {
      platform = new PlatformState();
    }

    return platform;
  }

  public void setPlatformDescription(APDescription platformDescription) {
    this.platformDescription = platformDescription;
  }

  public APDescription getAPDescription() {
    return platformDescription;
  }

  public int getNumberOfContainers() {
    return containerIDs.size();
  }

  public Optional<ContainerID> getContainerIDByName(String containerName) {
    return containerIDs.stream().filter(cid -> cid.getName().equals(containerName)).findAny();
  }

  public void addContainerID(ContainerID containerID) {
    containerIDs.add(containerID);
  }

  public boolean removeContainerID(ContainerID containerID) {
    return containerIDs.remove(containerID);
  }

  public Set<ContainerID> getContainerIDs() {
    return Collections.unmodifiableSet(containerIDs);
  }

  public void addAgentToContainer(ContainerID containerID, AID agentID) {
    Set<AID> agentIDs = containedAgents.getOrDefault(containerID, new HashSet<>());
    agentIDs.add(agentID);
    containedAgents.put(containerID, agentIDs);
  }

  public void removeAgentFromContainer(ContainerID containerID, AID agentID) {
    if (containedAgents.containsKey(containerID)) {
      Set<AID> agentIDs = containedAgents.get(containerID);
      agentIDs.remove(agentID);

      if (agentIDs.isEmpty()) {
        containedAgents.remove(containerID);
      } else {
        containedAgents.put(containerID, agentIDs);
      }
    } else {
      LOGGER.log(Logger.WARNING, "Tyring to remove agent " + agentID
          + " from non-existing container " + containerID);
    }
  }

  public Set<AID> getAgentsInContainer(ContainerID containerID) {
    if (containedAgents.containsKey(containerID)) {
      return Collections.unmodifiableSet(containedAgents.get(containerID));
    }
    return new HashSet<>();
  }

  public Optional<AID> getAgentIDByName(ContainerID containerID, String agentName) {
    Set<AID> agentIDs = containedAgents.get(containerID);
    if (agentIDs == null) {
      return Optional.empty();
    }

    return agentIDs.stream().filter(aid -> aid.getLocalName().equals(agentName)).findAny();
  }
}
