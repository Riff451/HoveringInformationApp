package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.agents.types.GeographicalInfo;
import it.tomlolriff.hoveringinformation.agents.types.HoverInfoZone;

public class HoverInfoZoneDetect {
	private Double x_anchor;
	private Double y_anchor;
	private Double safe_radius_2;
	private Double risk_radius_2;
	private Double relevant_radius_2;

	public HoverInfoZoneDetect(Double x_anchor, Double y_anchor,
			Double safe_radius, Double risk_radius, Double relevant_radius) {
		this.x_anchor = x_anchor;
		this.y_anchor = y_anchor;

		// i raggi li salvo direttamente al quadrato, mi servono così per i calcoli
		this.safe_radius_2 = (Double) Math.pow(safe_radius, 2);
		this.risk_radius_2 = (Double) Math.pow(risk_radius, 2);
		this.relevant_radius_2 = (Double) Math.pow(relevant_radius, 2);
	}

	/**
	 * Calcola l'area in cui si trova il punto, di cui vengono passate le coordinate
	 * @param geoInfo l'istanza di {@link GeographicalInfo}, contenente le coordinate correnti
	 * @return l'{@link HoverInfoZone} relativo all'area in cui si trova il punto
	 */
	public HoverInfoZone getFromCoordinate(GeographicalInfo geoInfo) {
		if(geoInfo == null) {
			// TODO oppure eccezione
			return HoverInfoZone.UNKNOWN;
		}

		return getFromCoordinate(geoInfo.getX(), geoInfo.getY());
	}

	/**
	 * Calcola l'area in cui si trova il punto, di cui vengono passate le coordinate
	 * @param x la x della coordinata
	 * @param y la y della coordinata
	 * @return l'{@link HoverInfoZone} relativo all'area in cui si trova il punto
	 */
	public HoverInfoZone getFromCoordinate(Double x, Double y) {
		if(x == null || y == null) {
			// TODO oppure eccezione
			return HoverInfoZone.UNKNOWN;
		}
		//Da pitagora: (x-center_x)^2 + (y - center_y)^2 < radius^2

		//Calcolo (x-center_x)^2 + (y - center_y)^2
		//TODO con i negativi come si comporta??
		Double temp = Math.pow(x - x_anchor, 2) + 
				Math.pow(y - y_anchor, 2);

		if(temp <= safe_radius_2) {
			return HoverInfoZone.SAFER;
		} else if(temp <= risk_radius_2) {
			return HoverInfoZone.RISK;
		} else if(temp <= relevant_radius_2) {
			return HoverInfoZone.RELEVANT;
		} else {
			return HoverInfoZone.OUT_RELEVANT;
		}
	}

}
