# java -cp org_hyperagents_jade_HypermediaAgentSystem/src/main/resources/jade-4.5.0.jar jade.Boot -gui -jade_core_management_AgentManagementService_agentspath org_hyperagents_jade_HypermediaAgentSystem/build/libs/ has:org.hyperagents.jade.HypermediaAgentSystem

java -cp hypermedia-weaver-agent/build/libs/org_hyperagents_jade_HypermediaWeaverAgent.jar jade.Boot -conf hypermedia-weaver-agent/main.properties -container-name Market-Place "hwa:org.hyperagents.jade.HypermediaWeaverAgent;agent-ra:org.hyperagents.jade.CupConsumerAgent;agent-rb:org.hyperagents.jade.CupProducerAgent"
