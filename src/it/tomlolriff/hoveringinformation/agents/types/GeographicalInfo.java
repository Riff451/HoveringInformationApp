package it.tomlolriff.hoveringinformation.agents.types;

import jade.core.Location;

import java.io.Serializable;

public class GeographicalInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public GeographicalInfo(Location value) {
		this.jade_location = value;
	}

	public GeographicalInfo(Location value, Double x, Double y, Double x_anchor, Double y_anchor, HoverInfoZone zone) {
		this.x = x;
		this.y = y;
		this.x_anchor = x_anchor;
		this.y_anchor = y_anchor;
		this.zone = zone;
		this.jade_location = value;
	}
	
	private HoverInfoZone zone = HoverInfoZone.UNKNOWN;
	private Location jade_location = null;
	private Double x = null;
	private Double y = null;
	private Double x_anchor = null;
	private Double y_anchor = null;
	
	@Override
	public String toString() {
		String res = "";
		res += "Location: " + jade_location.getID() + " ";
		res += "Zone: " + zone.toString();
		return res;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeographicalInfo other = (GeographicalInfo) obj;
		if (jade_location == null) {
			if (other.jade_location != null)
				return false;
		} else if (!jade_location.equals(other.jade_location))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (x_anchor == null) {
			if (other.x_anchor != null)
				return false;
		} else if (!x_anchor.equals(other.x_anchor))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (y_anchor == null) {
			if (other.y_anchor != null)
				return false;
		} else if (!y_anchor.equals(other.y_anchor))
			return false;
		if (zone != other.zone)
			return false;
		return true;
	}

	/*@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeographicalInfo other = (GeographicalInfo) obj;
		if (jade_location == null) {
			if (other.jade_location != null)
				return false;
		} else if (!jade_location.equals(other.jade_location))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (zone != other.zone)
			return false;
		return true;
	}*/

	public Location getJade_location() {
		return jade_location;
	}

	public Double getX() {
		return x;
	}

	public Double getY() {
		return y;
	}

	public HoverInfoZone getZone() {
		return zone;
	}
	
	public void setJadeLocation(Location jade_location) {
		this.jade_location = jade_location;
	}

	public Double getX_anchor() {
		return x_anchor;
	}

	public void setX_anchor(Double x_anchor) {
		this.x_anchor = x_anchor;
	}

	public Double getY_anchor() {
		return y_anchor;
	}

	public void setY_anchor(Double y_anchor) {
		this.y_anchor = y_anchor;
	}

}

