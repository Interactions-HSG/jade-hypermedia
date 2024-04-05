package org.hyperagents.jade;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.util.Logger;

import java.util.logging.Level;

public class CupProducerAgent extends Agent {

  protected void setup() {
    Logger logger = Logger.getMyLogger(this.getName());

    logger.log(Level.INFO, "[" + getLocalName() + "] Waiting for CFP...");

    MessageTemplate template = MessageTemplate.and(
      MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
      MessageTemplate.MatchPerformative(ACLMessage.CFP)
    );

    addBehaviour(new ContractNetResponder(this, template) {
      @Override
      protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        logger.log(Level.INFO, "[" + getLocalName() + "] CFP received from " +
          cfp.getSender().getName() + ". Action is " + cfp.getContent());

        int proposal = evaluateAction();
//        if (proposal > 2) {
        // We provide a proposal
        logger.log(Level.INFO, "[" + getLocalName() + "] Proposing " + proposal);

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        propose.setContent(String.valueOf(proposal));

          return propose;
//        } else {
//          // We refuse to provide a proposal
//          logger.log(Level.INFO, "Agent " + getLocalName() + ": Refuse");
//          throw new RefuseException("evaluation-failed");
//        }
      }

      @Override
      protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        logger.log(Level.INFO, "[" + getLocalName() + "] Proposal accepted");

        if (performAction()) {
          logger.log(Level.INFO, "[" + getLocalName() + "] Action successfully performed");
          ACLMessage inform = accept.createReply();
          inform.setPerformative(ACLMessage.INFORM);

          return inform;
        } else {
          logger.log(Level.INFO, "[" + getLocalName() + "] Action execution failed");
          throw new FailureException("Unable to fulfill cup production request");
        }
      }

      protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        logger.log(Level.INFO, "[" + getLocalName() + "] Proposal rejected");
      }
    } );
  }

  private int evaluateAction() {
    // Simulate an evaluation by generating a random number
    return (int) (Math.random() * 10);
  }

  private boolean performAction() {
    // Simulate action execution by generating a random number
    // return (Math.random() > 0.2);
    return true;
  }
}
