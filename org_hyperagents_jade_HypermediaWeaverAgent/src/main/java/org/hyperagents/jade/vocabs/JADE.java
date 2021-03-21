package org.hyperagents.jade.vocabs;

public final class JADE {
  public static final String PREFIX = "http://hyperagents.org/ns/jade#";

  /* Classes */
  public static final String MainContainer = PREFIX + "MainContainer";
  public static final String Container = PREFIX + "Container";

  public static final String AgentManagementSystem = PREFIX + "AgentManagementSystem";
  public static final String DirectoryFacilitator = PREFIX + "DirectoryFacilitator";
  public static final String RemoteMonitoringAgent = PREFIX + "RemoteMonitoringAgent";

  public static final String HypermediaWeaverAgent = PREFIX + "HypermediaWeaverAgent";

  /* Object properties */
  public static final String hasMainContainer = PREFIX + "hasMainContainer";
  public static final String hasContainer = PREFIX + "hasContainer";
  public static final String containsAgent = PREFIX + "containsAgent";

  /* Data properties */
  public static final String hasName = PREFIX + "hasName";
  public static final String localName = PREFIX + "hasLocalName";
}
