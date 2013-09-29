package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.agents.types.HoverInfoZone;
import jade.core.Location;

import java.util.List;

public class BroadcastReplicationHIAgent extends PieceHoveringInformationAgent {
	private static final long serialVersionUID = 1401204009904474296L;

	@Override
	protected void manageMovement(List<Location> locationsList) {
		for (Location location : locationsList) {
			if(getThisPiece().getGeoInfo().getZone() == HoverInfoZone.RISK) {
				doClone(location);
			} else if(getThisPiece().getGeoInfo().getZone() == HoverInfoZone.RELEVANT) {
				doMove(location);
			}
		}
	}

	@Override
	protected ReceiveCoordinatesChanged getZoneChangedBehaviour() {
		return new ReceiveCoordinatesChanged() {
			private static final long serialVersionUID = 6327650541924000311L;

			@Override
			protected void onHoverInfoZoneChanged(HoverInfoZone newZone) {
				switch (newZone) {
				case RISK:
					//TODO potrebbe richiedere anche gli UNKNOWN!
					// si vuole clonare, quindi richiede quelli in Risk ZONE
					requestRiskNodes();
					break;
				case RELEVANT:
					//TODO potrebbe richiedere anche gli UNKNOWN!
					// si vuole muovere, quindi richiede quelli in Relevant ZONE
					requestRelevantNodes();
					break;
				case OUT_RELEVANT:
					// harakiri!
					doDelete();
					break;
				default:
					// non fa nulla
					break;
				}
			}
		};
	}

}
