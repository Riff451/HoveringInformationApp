package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.activity.PieceHICallbacksFactory;
import it.tomlolriff.hoveringinformation.agents.interfaces.PieceHICallbacks;
import it.tomlolriff.jadeandroidonwifidirect.jade.extensions.SimpleCyclicMessageBehaviour;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

import java.io.IOException;

public class PieceHoverInformationCallbacksProviderAgent extends Agent {
	private static final long serialVersionUID = 115558516909799925L;

	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	
	private PieceHICallbacksFactory pieceHICallbacksFactory = null;
	private MessageTemplate templateGetPieceHICallbacks;
	public static final String ONTOLOGY_GET_PIECE_HI_CALLBACKS = "hover-info-get-piece-callbacks";

	@Override
	protected void setup() {
		super.setup();

		Object[] arguments = getArguments();
		if(arguments == null || arguments.length != 1) {
			throw new RuntimeException("Mancano dei parametri iniziali");	//FIXME
		}

		pieceHICallbacksFactory = (PieceHICallbacksFactory)arguments[0];

		templateGetPieceHICallbacks  = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchOntology(ONTOLOGY_GET_PIECE_HI_CALLBACKS));

		addBehaviour(new GetPieceHICallbacksBehaviour());
	}

	private class GetPieceHICallbacksBehaviour extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = -6105895916407245837L;

		public GetPieceHICallbacksBehaviour() {
			super(templateGetPieceHICallbacks);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			// restituisce una sua nuova istanza
			PieceHICallbacks response = pieceHICallbacksFactory.createCallbacks();
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			try {
				reply.setContentObject(response);
				send(reply);
			} catch (IOException e) {
				logger.log(Logger.SEVERE, "PieceHoverInformationCallbacksProviderAgent " + e.getMessage());
			}
		}
	}
}
