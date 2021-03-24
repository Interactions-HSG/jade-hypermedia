package org.hyperagents.jade.platform;

import jade.domain.FIPAAgentManagement.APDescription;
import jade.util.leap.Iterator;

public class WebAPDescription extends WebWrapper {
  private final APDescription apDescription;

  public WebAPDescription(APDescription apDescription, String endpoint) {
    super(endpoint);
    this.apDescription = apDescription;
  }

  public String getPlatformIRI() {
    return getEndpoint() + "#platform";
  }

  public String getName() {
    return apDescription.getName();
  }

  public Iterator getAllAPServices() {
    return apDescription.getAllAPServices();
  }

  @Override
  public String toString() {
    return apDescription.toString();
  }
}
