package it.tomlolriff.hoveringinformation.activity.models;

import it.tomlolriff.hoveringinformation.activity.Utility;
import it.tomlolriff.hoveringinformation.agents.HoverInfoZoneDetect;
import it.tomlolriff.hoveringinformation.agents.types.GeographicalInfo;
import jade.core.Location;
import jade.util.leap.Serializable;

import java.util.UUID;

public class HoveringInformationPiece implements Serializable {
	private static final long serialVersionUID = 1115083188530301669L;

	public HoveringInformationPiece(UUID id, Location jadeLocation, Object payLoad, Double x, Double y) {
		this.id = id;
		this.payLoad = payLoad;
		HoverInfoZoneDetect zoneDetector = new HoverInfoZoneDetect(Utility.X_ANCHOR, Utility.Y_ANCHOR, Utility.SAFE_RADIUS, Utility.RISK_RADIUS, Utility.RELEVANT_RADIUS);
		this.geoInfo = new GeographicalInfo(jadeLocation, x, y, Utility.X_ANCHOR, Utility.Y_ANCHOR, zoneDetector.getFromCoordinate(x, y));
	}
	
	private UUID id = null;
	private Object payLoad = null;
	private GeographicalInfo geoInfo = null;
	private GeographicalInfo geoInfoFrom = null;
	
	public Object getPayLoad() {
		return payLoad;
	}
	
	public GeographicalInfo getGeoInfo() {
		return geoInfo;
	}
	
	public GeographicalInfo getGeoInfoFrom() {
		return geoInfoFrom;
	}
	
	public UUID getId() {
		return id;
	}
	
	public String getMyAgentName() {
		return id + "||" + geoInfo.getJade_location().getName();
	}
	
	public void setGeoInfo(GeographicalInfo geoInfo) {
		this.geoInfo = geoInfo;
	}
	
	public void setGeoInfoFrom(GeographicalInfo geoInfoFrom) {
		this.geoInfoFrom = geoInfoFrom;
	}
	
	@Override
	public boolean equals(Object obj) {
		return id.equals(((HoveringInformationPiece)obj).getId());
	}
}
