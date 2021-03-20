package org.hyperagents.jade;

import jade.core.ContainerID;

import java.util.ArrayList;
import java.util.List;

public class PlatformState {
  private static PlatformState queue;
  private final List<ContainerID> containers;

  private PlatformState() {
    containers = new ArrayList<>();
  }

  public static synchronized PlatformState getInstance() {
    if (queue == null) {
      queue = new PlatformState();
    }

    return queue;
  }

  public int getNumberOfContainers() {
    return containers.size();
  }

  public void addContainerID(ContainerID containerID) {
    containers.add(containerID);
  }

  public boolean removeContainerID(ContainerID containerID) {
    return containers.remove(containerID);
  }

}
