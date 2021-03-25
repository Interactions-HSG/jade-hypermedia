package org.hyperagents.jade.platform;

public abstract class WebWrapper {
  private final String endpoint;

  public WebWrapper(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getIRI() {
    return endpoint;
  }
}
