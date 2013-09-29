package it.tomlolriff.hoveringinformation.agents.types;

public enum HoverInfoZone {
	SAFER("SAFER"),RISK("RISK"),RELEVANT("RELEVANT"),OUT_RELEVANT("OUT_RELEVANT"),UNKNOWN("UNKNOWN");

	private String value;
	private HoverInfoZone(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static HoverInfoZone getFromValue(String value) {
		for (HoverInfoZone zone : HoverInfoZone.values()) {
			if(zone.toString().equalsIgnoreCase(value)) {
				return zone;
			}
		}
		//TODO eccezione
		return null;
	}
}
