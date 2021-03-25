package org.hyperagents.jade.platform;

import jade.core.AID;
import jade.util.leap.Iterator;

public class WebAID extends WebWrapper {
  private final AID agentID;
  private final String containerIRI;

  public WebAID(AID agentID, String containerIRI) {
    super(containerIRI + "agents/" + agentID.getLocalName());
    this.agentID = agentID;
    this.containerIRI = containerIRI;
  }

  public String getAgentIRI() {
    return getIRI() + "#agent";
  }

  public String getContainerIRI() {
    return containerIRI;
  }

  public String getName() {
    return agentID.getName();
  }

  public String getLocalName() {
    return agentID.getLocalName();
  }

  public Iterator getAllAddresses() {
    return agentID.getAllAddresses();
  }

  public Iterator getAllResolvers() {
    return agentID.getAllResolvers();
  }

  public AID getAID() {
    return agentID;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof WebAID) {
      return agentID.equals(((WebAID) obj).getAID());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return agentID.hashCode();
  }

  @Override
  public String toString() {
    return agentID.toString();
  }
}
