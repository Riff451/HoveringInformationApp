package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.activity.models.HoveringInformationPiece;
import it.tomlolriff.hoveringinformation.agents.behaviours.GetAgentsOnContainerAndReplyBehaviour;
import it.tomlolriff.hoveringinformation.agents.interfaces.PieceHICallbacks;
import it.tomlolriff.hoveringinformation.agents.types.GeographicalInfo;
import it.tomlolriff.hoveringinformation.agents.types.HoverInfoZone;
import it.tomlolriff.jadeandroidonwifidirect.jade.extensions.SimpleCyclicMessageBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class PieceHoveringInformationAgent extends Agent {
	private static final long serialVersionUID = -4612432308829220021L;

	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	
	private HoveringInformationPiece thisPiece = null;

	private transient PieceHICallbacks callbacks = null;
	private transient AID aidSubscriber = null;

	public static final String ONTOLOGY_UPDATE_SELFCONTAINER_GEOINFO = "hover-info-update-selfcontainer-geoinfo";
	public static final String ONTOLOGY_GET_ZONE = "hover-info-get-zone";

	private MessageTemplate templateUpdtSelfContainer;
	private MessageTemplate templateLocationReceiver;
	private MessageTemplate templateGetPieceHoverInformationCallbacks;
	private MessageTemplate templateGetAidSubscriber;

	private ReceivePieceHoverInformationCallbacks receivePieceHoverInformationCallbacks;
	private ReceiveAIDSubscriber receiveAIDSubscriber;
	private ReceiveCoordinatesChanged receiveCoordinatesChanged;
	private ReceiveRequestedContainers requestedContainers;
	private OneShotBehaviour oneShotEffectiveClone;
	
	private GetAgentsOnContainerAndReplyBehaviour requestPieceHoverInformationCallbacks;
	private GetAgentsOnContainerAndReplyBehaviour requestAIDSubscriber;
	
	private transient boolean haveToKillTheOriginal = false;
	

	@Override
	protected void setup() {
		super.setup();

		Object[] arguments = getArguments();
		if(arguments == null || arguments.length != 4) {
			throw new RuntimeException("Mancano dei parametri iniziali");	//FIXME la runtime è brutta...
		}
		if(!(arguments[0] instanceof UUID)) {
			throw new RuntimeException("UUID errato");
		}
		if(!(arguments[2] instanceof Double) || !(arguments[3] instanceof Double)) {
			throw new RuntimeException("Coordinate del Piece errate");
		}
		thisPiece = new HoveringInformationPiece((UUID)arguments[0], here(), arguments[1], (Double)arguments[2], (Double)arguments[3]);
		
		templateUpdtSelfContainer = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology(ONTOLOGY_UPDATE_SELFCONTAINER_GEOINFO));

		templateLocationReceiver = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
				MessageTemplate.MatchOntology(ONTOLOGY_GET_ZONE));

		templateGetPieceHoverInformationCallbacks = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
				MessageTemplate.MatchOntology(PieceHoverInformationCallbacksProviderAgent.ONTOLOGY_GET_PIECE_HI_CALLBACKS));
		
		templateGetAidSubscriber = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
				MessageTemplate.MatchOntology(ContainerSubscriberAgent.ONTOLOGY_GET_AID_SUBSCRIBER));

		// mi clono subito e mi ammazzo!!
		// da notare che non passo dal "nostro" doClone(), lui lo scarta se è la stessa location. 
		// Io qui mi devo clonare subito, senza controlli e senza notificare nulla a nessuno.
		haveToKillTheOriginal = true;
		addBehaviour(getInitSequentialBehaviour());
	}
	
	/**
	 * Restituisce un {@link SequentialBehaviour} che permette di effettuare le operazioni si initializzazione
	 * con l'ordine corretto
	 * @return un {@link SequentialBehaviour} che permette di effettuare le operazioni si initializzazione
	 * con l'ordine corretto
	 */
	private SequentialBehaviour getInitSequentialBehaviour() {
		// il sequential mi dà garanzie sull'ordine di esecuzione: PRIMA si clona e POI si uccide
		SequentialBehaviour seqBehaviour = new SequentialBehaviour();
		seqBehaviour.addSubBehaviour(new OneShotBehaviour() {
			private static final long serialVersionUID = -1644374366455967924L;

			@Override
			public void action() {
				PieceHoveringInformationAgent.super.doClone(here(), getValidClonedName(here()));
			}
		});
		seqBehaviour.addSubBehaviour(new AssassinsCreed());

		return seqBehaviour;
	}

	private class AssassinsCreed extends OneShotBehaviour {
		private static final long serialVersionUID = -4615463969550760793L;

		@Override
		public void action() {
			if(haveToKillTheOriginal) {
				PieceHoveringInformationAgent.super.doDelete();
			}
		}
		
	}

	private String getValidClonedName(Location location) {
		return thisPiece.getId() + "||" + location.getName();
	}

	private class ReceiveAIDSubscriber extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = 4297054012346375066L;

		public ReceiveAIDSubscriber() {
			super(templateGetAidSubscriber);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			try {
				aidSubscriber = (AID) msg.getContentObject();
				if(PieceHoveringInformationAgent.this.callbacks != null) {
					// aggiungi gli altri Behaviour
					addAllInternalBehaviours();
					callbacks.onPieceCreated(thisPiece);
				}
			} catch (UnreadableException e) {
				logger.log(Logger.SEVERE, e.getMessage());
			}
		}
		
	}
	
	private class ReceivePieceHoverInformationCallbacks extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = 2852812797279767590L;

		public ReceivePieceHoverInformationCallbacks() {
			super(templateGetPieceHoverInformationCallbacks);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			try {
				callbacks = (PieceHICallbacks) msg.getContentObject();
				if(PieceHoveringInformationAgent.this.aidSubscriber != null) {
					// aggiungi gli altri Behaviour
					addAllInternalBehaviours();
					callbacks.onPieceCreated(thisPiece);
				}
			} catch (UnreadableException e) {
				logger.log(Logger.SEVERE, e.getMessage());
			}
		}
	}

	private void removeAllBehaviours() {
		if(receiveCoordinatesChanged != null && requestedContainers !=  null && receivePieceHoverInformationCallbacks != null &&
				receiveAIDSubscriber != null && requestPieceHoverInformationCallbacks != null && requestAIDSubscriber != null) {
			// mi sono clonato in locale, rimuovo i behaviour e mi sposto nella destinazione finale
			removeBehaviour(this.receiveCoordinatesChanged);
			removeBehaviour(this.requestedContainers);
			removeBehaviour(this.receivePieceHoverInformationCallbacks);
			removeBehaviour(this.receiveAIDSubscriber);
			if(oneShotEffectiveClone != null){
				removeBehaviour(this.oneShotEffectiveClone);
			}
			requestPieceHoverInformationCallbacks.detachBehaviour();
			requestAIDSubscriber.detachBehaviour();
		}
	}

	private void addAllInitBehaviours() {
		receivePieceHoverInformationCallbacks = new ReceivePieceHoverInformationCallbacks();
		addBehaviour(receivePieceHoverInformationCallbacks);

		receiveAIDSubscriber = new ReceiveAIDSubscriber();
		addBehaviour(receiveAIDSubscriber);
		
		ACLMessage aclMessageGetAIDSub = new ACLMessage(ACLMessage.PROPOSE);
		aclMessageGetAIDSub.setOntology(ContainerSubscriberAgent.ONTOLOGY_GET_AID_SUBSCRIBER);
		requestAIDSubscriber = new GetAgentsOnContainerAndReplyBehaviour(this, aclMessageGetAIDSub);
		addBehaviour(requestAIDSubscriber);
		
		ACLMessage aclMessageGetCallbacks = new ACLMessage(ACLMessage.PROPOSE);
		aclMessageGetCallbacks.setOntology(PieceHoverInformationCallbacksProviderAgent.ONTOLOGY_GET_PIECE_HI_CALLBACKS);
		requestPieceHoverInformationCallbacks = new GetAgentsOnContainerAndReplyBehaviour(this, aclMessageGetCallbacks);
		addBehaviour(requestPieceHoverInformationCallbacks);
	}

	private void addAllInternalBehaviours() {
		receiveCoordinatesChanged = getZoneChangedBehaviour();
		requestedContainers = new ReceiveRequestedContainers();
		addBehaviour(receiveCoordinatesChanged);
		addBehaviour(requestedContainers);
	}

	protected abstract ReceiveCoordinatesChanged getZoneChangedBehaviour();

	private class ReceiveRequestedContainers extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = -8920446237677664466L;

		public ReceiveRequestedContainers() {
			super(templateLocationReceiver);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			List<Location> locationsList = new ArrayList<Location>();
			// ricevo le Location
			try {
				locationsList = (List<Location>) msg.getContentObject();
			} catch (UnreadableException e) {
				logger.log(Logger.SEVERE, e.getMessage());
				return;
			}

			manageMovement(locationsList);
		}
	}

	/**
	 * Gestisce le operazioni da effettuare al cambio di zona
	 * @param locationsList le {@link Location} della zona associata a quella corrente dell'agente
	 */
	protected abstract void manageMovement(List<Location> locationsList);

	protected abstract class ReceiveCoordinatesChanged extends SimpleCyclicMessageBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 728245821653148939L;
		private ACLMessage aclMessageRisk;
		private ACLMessage aclMessageRelevant;
		private ACLMessage aclMessageSafer;

		public ReceiveCoordinatesChanged() {
			super(templateUpdtSelfContainer);

			// Lo inizializzo qui, è sempre uguali
			aclMessageRisk = new ACLMessage(ACLMessage.INFORM);
			aclMessageRisk.setOntology(ONTOLOGY_GET_ZONE);
			try {
				aclMessageRisk.setContentObject(HoverInfoZone.RISK);
			} catch (IOException e) {
				logger.log(Logger.SEVERE, e.getMessage());
			}
			aclMessageRisk.addReceiver(aidSubscriber);

			// Lo inizializzo qui, è sempre uguali
			aclMessageRelevant = new ACLMessage(ACLMessage.INFORM);
			aclMessageRelevant.setOntology(ONTOLOGY_GET_ZONE);
			try {
				aclMessageRelevant.setContentObject(HoverInfoZone.RELEVANT);
			} catch (IOException e) {
				logger.log(Logger.SEVERE, e.getMessage());
			}
			aclMessageRelevant.addReceiver(aidSubscriber);
			
			// Lo inizializzo qui, è sempre uguali
			aclMessageSafer = new ACLMessage(ACLMessage.INFORM);
			aclMessageSafer.setOntology(ONTOLOGY_GET_ZONE);
			try {
				aclMessageSafer.setContentObject(HoverInfoZone.SAFER);
			} catch (IOException e) {
				logger.log(Logger.SEVERE, e.getMessage());
			}
			aclMessageSafer.addReceiver(aidSubscriber);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			GeographicalInfo geoInfo = null;
			try {
				geoInfo = (GeographicalInfo) msg.getContentObject();
			} catch (UnreadableException e) {
				logger.log(Logger.SEVERE, e.getMessage());
				return;
			}
			
			// aggiorno da dove sono arrivato
			thisPiece.setGeoInfoFrom(thisPiece.getGeoInfo());
			
			// se non ho cambiato zona
			if(thisPiece.getGeoInfo() != null && thisPiece.getGeoInfo().getZone() == geoInfo.getZone()) {
				// aggiorno dove sono
				thisPiece.setGeoInfo(geoInfo);
			}
			else { // ho cambiato zona
				// aggiorno dove sono
				thisPiece.setGeoInfo(geoInfo);
				// Sono al cambio di zona!
				onHoverInfoZoneChanged(thisPiece.getGeoInfo().getZone());
			}

		}
		
		/**
		 * Richiede i nodi nella zona SAFER
		 */
		protected void requestSaferNodes() {
			send(aclMessageSafer);
		}

		/**
		 * Richiede i nodi nella zona RISK
		 */
		protected void requestRiskNodes() {
			send(aclMessageRisk);
		}

		/**
		 * Richiede i nodi nella zona RELEVANT
		 */
		protected void requestRelevantNodes() {
			send(aclMessageRelevant);
		}

		protected abstract void onHoverInfoZoneChanged(HoverInfoZone newZone);
	}

	@Override
	public void doDelete() {
		callbacks.onPieceDeleted(thisPiece);
		super.doDelete();
	}

	@Override
	public final void doMove(Location destination) {
		if(!destination.equals(here())) {
			callbacks.onPieceMoved(thisPiece);
			super.doMove(destination);
		}
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		removeAllBehaviours();

		// qui gli arriva un piece con AID sicuramente diverso, ma devo rinominarlo, per un discorso di naming,
		// in UUID+ContainerID, in modo da scartalo automaticamente se ce l'ho già.
		// senza queste operazioni non sarei in grado di riconoscere l'eventuale presenza del piece.

		// mi clono e uccido subito l'originale; la doClone non andrà a buon fine
		// se ho già lo stesso piece
		haveToKillTheOriginal = true;
		addBehaviour(getInitSequentialBehaviour());
	}

	@Override
	protected void afterClone() {
		super.afterClone();
		removeAllBehaviours();

		// aggiorno da dove sono arrivato
		thisPiece.setGeoInfoFrom(thisPiece.getGeoInfo());

		// TODO aggiorno il container in cui sono, per le x e y al momento non posso far nulla
		thisPiece.getGeoInfo().setJadeLocation(here());

		//Register the SL content language
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
		//Register the mobility ontology
		getContentManager().registerOntology(JADEManagementOntology.getInstance());
		addAllInitBehaviours();
	}

	public final void doClone(final Location destination) {
		// mi clono solamente nelle altre location
		if(!destination.equals(here())) {
			callbacks.onPieceCloned(thisPiece);
			oneShotEffectiveClone = new OneShotBehaviour(this) {
				private static final long serialVersionUID = -8094807374703602815L;

				@Override
				public void action() {
					PieceHoveringInformationAgent.super.doClone(destination, getValidClonedName(destination));
				}
			};
			addBehaviour(oneShotEffectiveClone);
		}
	}

	@Deprecated
	@Override
	public final void doClone(Location destination, String newName) {
		// ignora il nome passato, lo sa lui che nome deve avere, non puoi dargliene un'altro
		doClone(destination);
	}

	protected HoveringInformationPiece getThisPiece() {
		return thisPiece;
	}
}
