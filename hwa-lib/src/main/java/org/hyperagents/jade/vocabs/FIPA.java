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
  public static final String apService = PREFIX + "apService";
  // Non-standard terms:
  public static final String address = PREFIX + "address";
  public static final String identifierOf = PREFIX + "identifierOf";
  public static final String descriptionOf = PREFIX + "descriptionOf";
  public static final String hostedBy = PREFIX + "hostedBy";

  /* Data properties */
  public static final String name = PREFIX + "name";
  public static final String type = PREFIX + "type";
}
