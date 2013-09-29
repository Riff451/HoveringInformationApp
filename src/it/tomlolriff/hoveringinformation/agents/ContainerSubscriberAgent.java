package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.agents.behaviours.GetAgentsOnContainerAndReplyBehaviour;
import it.tomlolriff.hoveringinformation.agents.types.GeographicalInfo;
import it.tomlolriff.hoveringinformation.agents.types.HoverInfoZone;
import it.tomlolriff.jadeandroidonwifidirect.jade.extensions.SimpleCyclicMessageBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.introspection.AMSSubscriber;
import jade.domain.introspection.AddedContainer;
import jade.domain.introspection.Event;
import jade.domain.introspection.IntrospectionVocabulary;
import jade.domain.introspection.RemovedContainer;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;

public class ContainerSubscriberAgent extends Agent {
	private static final long serialVersionUID = 2117268155804480999L;

	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private GeographicalDataStorage map = new GeographicalDataStorage();
	public static final String ONTOLOGY_UPDATE_CONTAINER_ZONE = "hover-info-update-container-zone";
	public static final String ONTOLOGY_UPDATE_SELFCONTAINER_ZONE = "hover-info-update-selfcontainer-zone";
	public static final String ONTOLOGY_GET_AID_SUBSCRIBER = "hover-info-get-aid-subscriber";
	public static final String DF_SERVICE_NAME = "hoverinfo-subscribe-locations";

	private MessageTemplate templateGetZone;
	private MessageTemplate templateUpdtSelfContainerZone;
	private MessageTemplate templateUpdtContainerZone;
	private MessageTemplate templateGetAidSubscriber;
	
	private GetAgentsOnContainerAndReplyBehaviour sendToAgentsCoordUpdateBehav = null;

	@Override
	protected void setup() {
		super.setup();

		//Register the SL content language
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);

		//Register the mobility ontology
		getContentManager().registerOntology(JADEManagementOntology.getInstance());
		
		templateGetZone = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology(PieceHoveringInformationAgent.ONTOLOGY_GET_ZONE));

		templateUpdtSelfContainerZone = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology(ONTOLOGY_UPDATE_SELFCONTAINER_ZONE));

		templateUpdtContainerZone = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology(ONTOLOGY_UPDATE_CONTAINER_ZONE));
		
		templateGetAidSubscriber = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchOntology(ContainerSubscriberAgent.ONTOLOGY_GET_AID_SUBSCRIBER));

		registerDF();

		addBehaviour(new SubscriberBehaviour());	//TODO forse non serve a niente!!
		addBehaviour(new GetZoneNodesBehaviour());
		addBehaviour(new UpdateSelfContainerZoneBehaviour());
		addBehaviour(new UpdateContainerZoneBehaviour());
		addBehaviour(new GetAIDBehaviour());
	}

	private void registerDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(DF_SERVICE_NAME);
		sd.setName(getAID().toString());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	private class SubscriberBehaviour extends AMSSubscriber {
		private static final long serialVersionUID = 2778048472356663087L;

		@Override
		protected void installHandlers(Map handlers) {
			EventHandler addedHandler = new EventHandler() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 8011998681193034472L;

				@Override
				public void handle(Event event) {
					AddedContainer addedContainer = (AddedContainer) event;
					logger.log(Level.INFO, "HANDLE - add!!!" + addedContainer.getName() + "\n" + 
							"Container-toString(): " + addedContainer.getContainer().toString() + "\n" +
							"Container.getName(): " + addedContainer.getContainer().getName() + "\n" +
							"Container.getId(): " + addedContainer.getContainer().getID());
					map.put(addedContainer.getContainer());
				}
			};
			handlers.put(IntrospectionVocabulary.ADDEDCONTAINER,
					addedHandler);

			EventHandler removedHandler = new EventHandler() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 7180866971238540063L;

				@Override
				public void handle(Event ev) {
					logger.log(Level.INFO, "HANDLE - remove!!!");
					RemovedContainer removedContainer = (RemovedContainer) ev;
					map.remove(removedContainer.getContainer());
				}
			};
			handlers.put(IntrospectionVocabulary.REMOVEDCONTAINER,
					removedHandler);
		}
	}

	private class UpdateSelfContainerZoneBehaviour extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = -750359011193155655L;
		private ACLMessage aclMessage = null;

		public UpdateSelfContainerZoneBehaviour() {
			super(templateUpdtSelfContainerZone);
			aclMessage = new ACLMessage(ACLMessage.INFORM);
			aclMessage.setOntology(PieceHoveringInformationAgent.ONTOLOGY_UPDATE_SELFCONTAINER_GEOINFO);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			try {
				GeographicalInfo geoInfo = (GeographicalInfo) msg.getContentObject();
				map.put(geoInfo);
				logger.log(Level.INFO, "ContainerSubscriberAgent Location changed Receive = (" + geoInfo.getX() + "," + geoInfo.getY() + ")");

				// notifica a tutti i subscriber la modifica e ai piece of hovering information del suo Container la modifica
				addBehaviour(createReceiverManagerBehaviour(geoInfo));
			} catch (UnreadableException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}

		private SequentialBehaviour createReceiverManagerBehaviour(final GeographicalInfo geoInfo) {
			SequentialBehaviour seqBehaviour = new SequentialBehaviour();
			seqBehaviour.addSubBehaviour(new OneShotBehaviour() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 5056793144164377405L;

				@Override
				public void action() {
					ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
					reply.setOntology(ONTOLOGY_UPDATE_CONTAINER_ZONE);
					try {
						reply.setContentObject(geoInfo);
						DFAgentDescription[] subscribers = getSubscribersAID();
						for (DFAgentDescription aid : subscribers) {
							reply.addReceiver(aid.getName());
						}
						logger.log(Level.INFO, "ContainerSubscriberAgent - notifySelfLocationChangedToSubscribers");
						send(reply);
					} catch (IOException e) {
						logger.log(Logger.SEVERE, "fillContent() - CodecException " + e.getMessage());
					}
				}

				private DFAgentDescription[] getSubscribersAID() {
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType(DF_SERVICE_NAME);
					template.addServices(sd);
					SearchConstraints all = new SearchConstraints();
					all.setMaxResults(Long.valueOf(-1));
					try {
						return DFService.search(myAgent, template, all);
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
					return null;
					//TODO eccezione
				}
			});

			try {
				aclMessage.setContentObject(geoInfo);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}

			if(sendToAgentsCoordUpdateBehav != null) {
				sendToAgentsCoordUpdateBehav.detachBehaviour();
			}
			sendToAgentsCoordUpdateBehav = new GetAgentsOnContainerAndReplyBehaviour(myAgent, aclMessage);
			seqBehaviour.addSubBehaviour(sendToAgentsCoordUpdateBehav);
			return seqBehaviour;
		}
	}

	private class UpdateContainerZoneBehaviour extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = -2797845047702483930L;

		public UpdateContainerZoneBehaviour() {
			super(templateUpdtContainerZone);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			try {
				GeographicalInfo geoInfo = (GeographicalInfo) msg.getContentObject();
				map.put(geoInfo);
			} catch (UnreadableException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}

	private class GetZoneNodesBehaviour extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = 7519532604453988841L;

		public GetZoneNodesBehaviour() {
			super(templateGetZone);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			// ho ricevuto una richiesta di container di una determinata zona
			HoverInfoZone zone = null;
			try {
				zone = (HoverInfoZone) msg.getContentObject();
			} catch (UnreadableException e1) {
				logger.log(Level.SEVERE, e1.getMessage());
			}
			ACLMessage msgReply = msg.createReply();
			msgReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			try {
				msgReply.setContentObject((Serializable) map.getLocation4Zone(zone));
				send(msgReply);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}
	
	private class GetAIDBehaviour extends SimpleCyclicMessageBehaviour {
		private static final long serialVersionUID = 1305344043844121997L;

		public GetAIDBehaviour() {
			super(templateGetAidSubscriber);
		}

		@Override
		public void innerAction(ACLMessage msg) {
			AID response = getAID();
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			try {
				reply.setContentObject(response);
				send(reply);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
		
	}
	
}
