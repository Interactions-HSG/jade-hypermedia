package org.hyperagents.jade;

import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;

import java.util.*;

public class PlatformState {
  private static PlatformState platform;

  private APDescription platformDescription;
  private final List<ContainerID> containerIDs;

  private PlatformState() {
    platformDescription = null;
    containerIDs = new ArrayList<>();
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

  public String getPlatformName() {
    return platformDescription.getName();
  }

  public Optional<ContainerID> getContainerID(String containerName) {
    return containerIDs.stream().filter(cid -> cid.getName().equals(containerName)).findAny();
  }

  public int getNumberOfContainers() {
    return containerIDs.size();
  }

  public void addContainerID(ContainerID containerID) {
    containerIDs.add(containerID);
  }

  public boolean removeContainerID(ContainerID containerID) {
    return containerIDs.remove(containerID);
  }

  public List<ContainerID> getContainerIDsImmutable() {
    return Collections.unmodifiableList(containerIDs);
  }
}
