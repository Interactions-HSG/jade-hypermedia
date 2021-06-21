package org.hyperagents.jade.platform;

import jade.core.ContainerID;

public class WebContainerID extends WebWrapper {
  private final ContainerID containerID;

  public WebContainerID(ContainerID containerID, String endpoint) {
    super(endpoint + "containers/" + containerID.getName() + "/");
    this.containerID = containerID;
  }

  public String getContainerIRI() {
    return getIRI() + "#container";
  }

  public String getName() {
    return containerID.getName();
  }

  public boolean isMain() {
    return containerID.getMain();
  }

  public ContainerID getContainerID() {
    return containerID;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof WebContainerID) {
      return containerID.equals(((WebContainerID) obj).getContainerID());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return containerID.hashCode();
  }

  @Override
  public String toString() {
    return containerID.toString();
  }
}
