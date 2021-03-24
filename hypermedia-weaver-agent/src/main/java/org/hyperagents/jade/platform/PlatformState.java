package org.hyperagents.jade.platform;

import jade.util.Logger;
import org.hyperagents.jade.HypermediaInterface;

import java.util.*;

/**
 * Singleton used by the Hypermedia Weaver Agent controlling this instance to record platform state.
 * The {@link HypermediaInterface} retrieves and exposes data from this singleton.
 */
public class PlatformState {
  private final static Logger LOGGER = Logger.getJADELogger(PlatformState.class.getName());

  private static PlatformState platform;

  private WebAPDescription platformDescription;
  private final Set<WebContainerID> containerIDs;
  private final Map<WebContainerID, Set<WebAID>> containedAgents;

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

  public void setPlatformDescription(WebAPDescription platformDescription) {
    this.platformDescription = platformDescription;
  }

  public WebAPDescription getAPDescription() {
    return platformDescription;
  }

  public int getNumberOfContainers() {
    return containerIDs.size();
  }

  public Optional<WebContainerID> getContainerIDByName(String containerName) {
    return containerIDs.stream().filter(cid -> cid.getName().equals(containerName)).findAny();
  }

  public void addContainerID(WebContainerID containerID) {
    containerIDs.add(containerID);
  }

  public boolean removeContainerID(WebContainerID containerID) {
    return containerIDs.remove(containerID);
  }

  public Set<WebContainerID> getContainerIDs() {
    return Collections.unmodifiableSet(containerIDs);
  }

  public void addAgentToContainer(WebContainerID containerID, WebAID agentID) {
    Set<WebAID> agentIDs = containedAgents.getOrDefault(containerID, new HashSet<>());
    agentIDs.add(agentID);
    containedAgents.put(containerID, agentIDs);
  }

  public void removeAgentFromContainer(WebContainerID containerID, WebAID agentID) {
    if (containedAgents.containsKey(containerID)) {
      Set<WebAID> agentIDs = containedAgents.get(containerID);
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

  public Set<WebAID> getAgentsInContainer(WebContainerID containerID) {
    if (containedAgents.containsKey(containerID)) {
      return Collections.unmodifiableSet(containedAgents.get(containerID));
    }

    return new HashSet<>();
  }

  public Optional<WebAID> getAgentIDByName(WebContainerID containerID, String agentName) {
    Set<WebAID> agentIDs = containedAgents.get(containerID);
    if (agentIDs == null) {
      return Optional.empty();
    }

    return agentIDs.stream().filter(aid -> aid.getLocalName().equals(agentName)).findAny();
  }
}
