package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.agents.types.HoverInfoZone;
import jade.core.Location;

import java.util.List;

public class BroadRepPseudoDirectionHIAgent extends
		PieceHoveringInformationAgent {
	private static final long serialVersionUID = 5904020740351648574L;

	@Override
	protected void manageMovement(List<Location> locationsList) {
		for (Location location : locationsList) {
			if(getThisPiece().getGeoInfo().getZone() == HoverInfoZone.RISK) {
				//come nome del clone metto: ID||random
				doClone(location);
			} else if(getThisPiece().getGeoInfo().getZone() == HoverInfoZone.RELEVANT) {
				doMove(location);
			}
		}
	}

	@Override
	protected ReceiveCoordinatesChanged getZoneChangedBehaviour() {
		return new ReceiveCoordinatesChanged() {
			private static final long serialVersionUID = 3785813427930035633L;

			@Override
			protected void onHoverInfoZoneChanged(HoverInfoZone newZone) {
				switch (newZone) {
				case RISK:
					if(getThisPiece().getGeoInfoFrom().getZone().equals(HoverInfoZone.SAFER)){ // se provenivo dalla safe
						// mi clono nei safe e nei risk
						requestSaferNodes();
						requestRiskNodes();
					}
					else if(getThisPiece().getGeoInfoFrom().getZone().equals(HoverInfoZone.RELEVANT)) { // provenivo dalla relevant
						// mi clono nei risk
						requestRiskNodes();
					}
					break;
				case RELEVANT:
					if(getThisPiece().getGeoInfoFrom().getZone().equals(HoverInfoZone.RISK)) { // se provenivo dalla risk
						// mi sposto nei risk e nei relevant
						requestRiskNodes();
						requestRelevantNodes();
					}
					// se provenivo dalla out non ho piece; non devo fare niente
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
