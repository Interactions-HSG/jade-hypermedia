package org.hyperagents.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.util.Logger;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;

public class CupConsumerAgent extends Agent {
  private int nResponders = 1;

  protected void setup() {
    Logger logger = Logger.getMyLogger(this.getName());

    logger.log(Level.INFO, "[" + getLocalName() + "] Added custom signifier");

    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    dfd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
    dfd.addProtocols(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

    try {
        DFService.register(this, dfd);
    } catch (FIPAException e) {
        throw new RuntimeException(e);
    }

    MessageTemplate template = MessageTemplate.and(
      MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
      MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );

    addBehaviour(new AchieveREResponder(this, template) {
      private String action = "";

      @Override
      protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
        logger.log(Level.INFO, "[" + getLocalName() + "] REQUEST received from "
          + request.getSender().getName() + ". Action is " + request.getContent());

        action = request.getContent();

        if (checkAction()) {
          // We agree to perform the action. Note that in the FIPA-Request protocol the AGREE message
          // is optional. Return null if you don't want to send it.
          logger.log(Level.INFO, "[" + getLocalName() + "] Agree");

          ACLMessage agree = request.createReply();
          agree.setPerformative(ACLMessage.AGREE);

          return agree;
        } else {
          // We refuse to perform the action
          logger.log(Level.INFO, "[" + getLocalName() + "] Refuse");
          throw new RefuseException("check-failed");
        }
      }

      @Override
      protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        if (performAction(action)) {
          logger.log(Level.INFO, "[" + getLocalName() + "] CFP for cup production launched successfully");

          ACLMessage inform = request.createReply();
          inform.setPerformative(ACLMessage.INFORM);

          return inform;
        } else {
          logger.log(Level.INFO, "[" + getLocalName() + "] CFP for cup production failed");
          throw new FailureException("unexpected-error");
        }
      }
    });
  }

  private boolean checkAction() {
    // Simulate a check by generating a random number
    // return (Math.random() > 0.2);
    return true;
  }

  private boolean performAction(String action) {
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
    cfp.addReceiver(new AID("agent-rb", AID.ISLOCALNAME));
    cfp.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
    cfp.setContent(action);

    addBehaviour(new CupOrderInitiator(this, cfp));

    return true;
  }

  class CupOrderInitiator extends ContractNetInitiator {
    Logger logger = Logger.getMyLogger("");

    CupOrderInitiator(Agent agent, ACLMessage cfp) {
      super(agent, cfp);
    }

    @Override
    protected void handlePropose(ACLMessage propose, Vector v) {
      logger.log(Level.INFO, "[" + getLocalName() + "] " + propose.getSender().getName() + " proposed " + propose.getContent());
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
      logger.log(Level.INFO, "[" + getLocalName() + "] " + refuse.getSender().getName() + " refused");
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
      if (failure.getSender().equals(myAgent.getAMS())) {
        // FAILURE notification from the JADE runtime: the receiver does not exist
        logger.log(Level.INFO, "[" + getLocalName() + "] Responder does not exist");
      } else {
        logger.log(Level.INFO, "[" + getLocalName() + "] " + failure.getSender().getName() + " failed");
      }

      nResponders--;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
      if (responses.size() < nResponders) {
        // Some responder didn't reply within the specified timeout
        logger.log(Level.INFO, "[" + getLocalName() + "] Timeout expired: missing " + (nResponders - responses.size()) + " responses");
      }

      // Evaluate proposals
      int bestProposal = -1;
      AID bestProposer = null;
      ACLMessage accept = null;

      Enumeration e = responses.elements();
      while (e.hasMoreElements()) {
        ACLMessage msg = (ACLMessage) e.nextElement();

        if (msg.getPerformative() == ACLMessage.PROPOSE) {
          ACLMessage reply = msg.createReply();
          reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
          acceptances.addElement(reply);

          int proposal = Integer.parseInt(msg.getContent());

          if (proposal > bestProposal) {
            bestProposal = proposal;
            bestProposer = msg.getSender();
            accept = reply;
          }
        }
      }

      // Accept the proposal of the best proposer
      if (accept != null) {
        logger.log(Level.INFO, "[" + getLocalName() + "] Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
        accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
      }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
      logger.log(Level.INFO, "[" + getLocalName() + "] " + inform.getSender().getName() + " successfully performed the requested action");
    }
  }
}
