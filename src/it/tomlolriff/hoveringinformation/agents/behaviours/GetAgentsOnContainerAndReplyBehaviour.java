package it.tomlolriff.hoveringinformation.agents.behaviours;

import it.tomlolriff.jadeandroidonwifidirect.jade.extensions.SimpleCyclicMessageBehaviour;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

/**
 * Richiede all'AMS una lista di tutti gli agenti del suo stesso container ed invia a tutti loro
 * un messaggio passato al costruttore
 * @author tom - lollo - riff
 *
 */
public class GetAgentsOnContainerAndReplyBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 1991703726548869447L;
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private ACLMessage replyMessage;
	private MessageTemplate templateGetAgentsOnContainer;
	private ReplyToAllAgentsOnContainerBehaviour replyToAllAgentsOnContainerBehaviour;

	public GetAgentsOnContainerAndReplyBehaviour(Agent myAgent, ACLMessage replyMessage) {
		super(myAgent);
		this.replyMessage = replyMessage;
		this.templateGetAgentsOnContainer = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology(JADEManagementOntology.getInstance().getName()));
	}

	@Override
	public void action() {
		replyToAllAgentsOnContainerBehaviour = new ReplyToAllAgentsOnContainerBehaviour();
		myAgent.addBehaviour(replyToAllAgentsOnContainerBehaviour);

		try {
			ACLMessage request = getACLMessageForQueryAgentsOnLocation();
			myAgent.send(request);
		}
		catch (CodecException e) {
			logger.log(Logger.SEVERE, "fillContent() - CodecException " + e.getMessage());
		}
		catch (OntologyException e) {
			logger.log(Logger.SEVERE, "fillContent() - OntologyException " + e.getMessage());
		}
	}

	/**
	 * Restituisce una corretta istanza di {@link ACLMessage} per recuperare gli agenti nella stesso container
	 * @return una corretta istanza di {@link ACLMessage} per recuperare gli agenti nella stesso container
	 * @throws CodecException
	 * @throws OntologyException
	 */
	private ACLMessage getACLMessageForQueryAgentsOnLocation() throws CodecException, OntologyException {
		QueryAgentsOnLocation action = new QueryAgentsOnLocation();
		action.setLocation(myAgent.here());

		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setSender(myAgent.getAID());
		request.addReceiver(myAgent.getAMS());
		request.setOntology(JADEManagementOntology.getInstance().getName());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);

		Action action2 = new Action(myAgent.getAMS(), action);
		myAgent.getContentManager().fillContent(request, action2);
		return request;
	}

	/**
	 * Rimuove tutti i Behaviour interni dall'agente
	 */
	public void detachBehaviour() {
		if(myAgent != null) {
			if(replyToAllAgentsOnContainerBehaviour != null) {
				myAgent.removeBehaviour(replyToAllAgentsOnContainerBehaviour);
			}
			myAgent.removeBehaviour(this);
		}
	}
	
	private class ReplyToAllAgentsOnContainerBehaviour extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = -6031125729348945930L;

		public ReplyToAllAgentsOnContainerBehaviour() {
			super(templateGetAgentsOnContainer);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			// riceve la lista di agenti dello stesso container -> invia a tutti il messaggio passato al costruttore
			ContentElement ce2;
			try {
				ce2 = myAgent.getContentManager().extractContent(msg);
				Result myresult2 = (Result) ce2;
	            jade.util.leap.Iterator agentsinlocation = myresult2.getItems().iterator();
	            replyMessage.clearAllReceiver();
	            while(agentsinlocation.hasNext()) {
	            	// OntoAID estende AID, quindi è già un AID!!
	            	AID aidTmp = (AID) agentsinlocation.next();
	            	replyMessage.addReceiver(aidTmp);
	            }
				// lo invia a tutti gli agenti del container, tanto verrà ricevuto
				// solo dai piece of hovering information
	            myAgent.send(replyMessage);
			} catch (CodecException e) {
				logger.log(Logger.SEVERE, "Eccezione...");
			} catch (UngroundedException e) {
				logger.log(Logger.SEVERE, "Eccezione...");
			} catch (OntologyException e) {
				logger.log(Logger.SEVERE, "Eccezione...");
			}
		}
		
	} 
}
