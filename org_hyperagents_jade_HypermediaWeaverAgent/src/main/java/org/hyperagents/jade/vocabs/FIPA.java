package org.hyperagents.jade.vocabs;

public class FIPA {
  public static final String PREFIX = "http://hyperagents.org/ns/fipa#";

  /* Classes */
  // The following terms are defined in: http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951010
  public static final String AgentIdentifier = PREFIX + "AgentIdentifier";
  public static final String APDescription = PREFIX + "APDescription";
  // Non-standard terms:
  public static final String Agent = PREFIX + "Agent";

  /* Object properties */
  // The following terms are defined in: http://fipa.org/specs/fipa00023/SC00023K.html#_Toc75951010
  public static final String addresses = PREFIX + "addresses";
  public static final String resolvers = PREFIX + "resolvers";
  // Non-standard terms:
  public static final String identifierOf = PREFIX + "identifierOf";

//  public static final String usesMessageTransport = PREFIX + "usesMessageTransport";
//  public static final String hasType = PREFIX + "hasTransportType";
//  public static final String hasAddress = PREFIX + "hasTransportAddress";

  /* Data properties */
  public static final String name = PREFIX + "name";
}
