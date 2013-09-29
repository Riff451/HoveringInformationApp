package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.agents.interfaces.RequestLocationInfo;
import it.tomlolriff.hoveringinformation.agents.interfaces.SetLocationInfo;
import it.tomlolriff.hoveringinformation.agents.types.GeographicalInfo;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

import java.io.IOException;
import java.util.logging.Level;

public class LocalizatorAgent extends Agent implements SetLocationInfo {
	private static final long serialVersionUID = -2972850735933081032L;

	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private AID aidSubscriber = null;
	private ACLMessage aclMessage = null;
	private Double x = null;
	private Double y = null;
	private RequestLocationInfo requestLocationInfo = null;
	private Double x_anchor = Double.NaN;
	private Double y_anchor = Double.NaN;
	private Double safe_radius = Double.NaN;
	private Double risk_radius = Double.NaN;
	private Double relevant_radius = Double.NaN;

	private transient HoverInfoZoneDetect hoverInfoZoneDetect;
	private static final int TICKER_MS = 3000;

	@Override
	protected void setup() {
		super.setup();
		
		registerO2AInterface(SetLocationInfo.class, this);

		// devo istanziarmi le coordinate dell'anchoTICKER_MSr
		Object[] arguments = getArguments();
		if(arguments == null || arguments.length != 7) {
			throw new RuntimeException("Mancano dei parametri iniziali");	//FIXME
		}

		x_anchor = (Double) arguments[0];
		y_anchor = (Double) arguments[1];
		safe_radius = (Double) arguments[2];
		risk_radius = (Double) arguments[3];
		relevant_radius = (Double) arguments[4];

		hoverInfoZoneDetect = new HoverInfoZoneDetect(x_anchor, y_anchor, 
				safe_radius, risk_radius, relevant_radius);

		requestLocationInfo = (RequestLocationInfo)arguments[5];
		aidSubscriber = (AID)arguments[6];

		aclMessage = new ACLMessage(ACLMessage.INFORM);
		aclMessage.setOntology(ContainerSubscriberAgent.ONTOLOGY_UPDATE_SELFCONTAINER_ZONE);
		aclMessage.addReceiver(aidSubscriber);

		addBehaviour(new LocalizerBehaviour(this, TICKER_MS));
	}

	private class LocalizerBehaviour extends TickerBehaviour {
		private static final long serialVersionUID = -6364588127369292519L;

		public LocalizerBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			// deve chiedere alla piattaforma di "supporto" le indicazioni geografiche, la risposta verrà
			// invocata nel metodo setLocationInfo()
			requestLocationInfo.getLocationInfo();
		}
	}

	@Override
	public void setLocationInfo(Double new_x, Double new_y) {
		logger.log(Level.INFO, "LocalizatorAgent Location changed Receive = (" + new_x + "," + new_y + ")");

		// setta la locazione fisica, se variata, viene invocato dall'esterno
		if(x == null || y == null || x.compareTo(new_x) != 0 || y.compareTo(new_y) != 0) {	//TODO fare una differenza tra double con una soglia!
			x = new_x;
			y = new_y;
			// deve inviare una segnalazione al Subscriber
			GeographicalInfo geoInfo = new GeographicalInfo(here(), x, y, x_anchor, y_anchor,
					hoverInfoZoneDetect.getFromCoordinate(x, y));
			try {
				aclMessage.setContentObject(geoInfo);
				send(aclMessage);
			} catch (IOException e) {
				// TODO eccezione
			}
		}
		// else -> non fa nulla
	}
}
