package it.tomlolriff.hoveringinformation.agents;

import it.tomlolriff.hoveringinformation.agents.types.GeographicalInfo;
import it.tomlolriff.hoveringinformation.agents.types.HoverInfoZone;
import jade.core.ContainerID;
import jade.core.Location;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class GeographicalDataStorage 
	implements Map<HoverInfoZone, List<GeographicalInfo>> {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private int size = 0;
	private List<GeographicalInfo> safer_location;
	private List<GeographicalInfo> risk_location;
	private List<GeographicalInfo> relevant_location;
	private List<GeographicalInfo> out_relevant_location;
	private List<GeographicalInfo> unknown_location;
	
	GeographicalDataStorage() {
		safer_location = new ArrayList<GeographicalInfo>();
		risk_location = new ArrayList<GeographicalInfo>();
		relevant_location = new ArrayList<GeographicalInfo>();
		out_relevant_location = new ArrayList<GeographicalInfo>();
		unknown_location = new ArrayList<GeographicalInfo>();
	}

	/**
	 * Restituisce un elenco di {@link Location} presenti nella {@link HoverInfoZone} passata in input
	 * @param zone la {@link HoverInfoZone} richiesta
	 * @return un elenco di {@link Location} presenti nella {@link HoverInfoZone} passata in input
	 */
	public List<Location> getLocation4Zone(HoverInfoZone zone) {
		return getAllLocation(zone);
	}

	private List<GeographicalInfo> getAllGeoInfo(HoverInfoZone zone) {
		List<GeographicalInfo> res = new ArrayList<GeographicalInfo>();
		switch (zone) {
			case SAFER:
				res = safer_location;
				break;
			case RISK:
				res = risk_location;
				break;
			case RELEVANT:
				res = relevant_location;
				break;
			case OUT_RELEVANT:
				res = out_relevant_location;
				break;
			case UNKNOWN:
				res = unknown_location;
				break;
			default:
				//FIXME eccezione controllata
				throw new RuntimeException();
		}
		return res;
	}

	private List<Location> getAllLocation(HoverInfoZone zone) {
		List<GeographicalInfo> geoInfo = getAllGeoInfo(zone);
		List<Location> result = new ArrayList<Location>();
		for (GeographicalInfo geoInfoTmp : geoInfo) {
			result.add(geoInfoTmp.getJade_location());
		}
		return result;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return (size == 0);
	}

	@Override
	public boolean containsKey(Object key) {
		if(key instanceof HoverInfoZone) {
			return true;
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		if(value instanceof GeographicalInfo) {
			List<GeographicalInfo> values = null;
			for (HoverInfoZone zone : HoverInfoZone.values()) {
				values = getAllGeoInfo(zone);
				for (GeographicalInfo geographicalInfo : values) {
					if(((GeographicalInfo)value).equals(geographicalInfo)){
						return true;
					}
				}
			}
		} else if(value instanceof Location) {
			List<GeographicalInfo> values = null;
			for (HoverInfoZone zone : HoverInfoZone.values()) {
				values = getAllGeoInfo(zone);
				for (GeographicalInfo geographicalInfo : values) {
					if(((Location)value).getID().equalsIgnoreCase(geographicalInfo.getJade_location().getID())){
						return true;
					}
				}
			}
		} else {
			//TODO eccezione
		}
		return false;
	}

	@Override
	public List<GeographicalInfo> get(Object key) {
		if(containsKey(key)) {
			return getAllGeoInfo((HoverInfoZone)key);
		} else {
			//TODO Eccezione
			return null;
		}
	}

	public List<GeographicalInfo> put(
			ContainerID value) {
		GeographicalInfo geoInfo = new GeographicalInfo(value);
		put(geoInfo);
		return unknown_location;
	}

	public List<GeographicalInfo> put(
			GeographicalInfo value) {
		HoverInfoZone key = value.getZone();
		if(containsValue(value.getJade_location())) {
			// se lo contiene, ma l'aggiornamento me lo mette in UNKNOWN non lo faccio,
			// preferisco tenerlo in una zona "conosciuta"
			if(value.getZone() == HoverInfoZone.UNKNOWN) {
				return null;
			}
			List<GeographicalInfo> prevData = get(key);
			// devo fare update -> remove e poi put del nuovo
			removeLocation(value.getJade_location());
			put(value);

			return prevData;
		} else {
			List<GeographicalInfo> values = getAllGeoInfo(key);
			values.add(value);
			size++;
			logValues();
			return null;
		}
	}

	@Override
	public List<GeographicalInfo> put(
			HoverInfoZone key,
			List<GeographicalInfo> value) {
		// per ognuno va a chiamare il metodo che fa il put del singolo dato
		for (GeographicalInfo geographicalInfo : value) {
			put(geographicalInfo);
		}
		return value;
	}

	@Override
	public List<GeographicalInfo> remove(
			Object key) {
		if(key instanceof Location) {
			removeLocation((Location)key);
		} else if(key instanceof GeographicalInfo) {
			removeLocation((GeographicalInfo)key);
		} else {
			//FIXME eccezione
		}
		
		return null;
	}

	private void removeLocation(GeographicalInfo key) {
		List<GeographicalInfo> values = null;
		for (HoverInfoZone zone : HoverInfoZone.values()) {
			values = getAllGeoInfo(zone);
			for (GeographicalInfo geographicalInfo : values) {
				if(key.equals(geographicalInfo)){
					values.remove(geographicalInfo);
					size--;
					return;
				}
			}
		}
	}

	private void removeLocation(Location key) {
		List<GeographicalInfo> values = null;
		for (HoverInfoZone zone : HoverInfoZone.values()) {
			values = getAllGeoInfo(zone);
			for (GeographicalInfo geographicalInfo : values) {
				if(key.equals(geographicalInfo.getJade_location())){
					values.remove(geographicalInfo);
					size--;
					return;
				}
			}
		}
	}

	@Override
	public void putAll(
			Map<? extends HoverInfoZone, ? extends List<GeographicalInfo>> m) {
		for (HoverInfoZone zone : HoverInfoZone.values()) {
			put(zone, m.get(zone));
		}
	}

	@Override
	public void clear() {
		this.safer_location.clear();
		this.risk_location.clear();
		this.relevant_location.clear();
		this.out_relevant_location.clear();
		this.unknown_location.clear();
		this.size = 0;
	}

	@Override
	public Set<HoverInfoZone> keySet() {
		return new HashSet<HoverInfoZone>(Arrays.asList(HoverInfoZone.values()));
	}

	@Override
	public Collection<List<GeographicalInfo>> values() {
		Collection<List<GeographicalInfo>> res = new ArrayList<List<GeographicalInfo>>();
		for (HoverInfoZone zone : HoverInfoZone.values()) {
			res.add(get(zone));
		}

		return res;
	}

	@Override
	public Set<java.util.Map.Entry<HoverInfoZone, List<GeographicalInfo>>> entrySet() {
		HashSet<java.util.Map.Entry<HoverInfoZone, List<GeographicalInfo>>> res = 
				new HashSet<java.util.Map.Entry<HoverInfoZone, List<GeographicalInfo>>>();
		
		res.add(new GeographicalDataStorageEntry(HoverInfoZone.SAFER, this.safer_location));
		res.add(new GeographicalDataStorageEntry(HoverInfoZone.RISK, this.risk_location));
		res.add(new GeographicalDataStorageEntry(HoverInfoZone.RELEVANT, this.relevant_location));
		res.add(new GeographicalDataStorageEntry(HoverInfoZone.OUT_RELEVANT, this.out_relevant_location));
		res.add(new GeographicalDataStorageEntry(HoverInfoZone.UNKNOWN, this.unknown_location));

		return res;
	}

	class GeographicalDataStorageEntry implements java.util.Map.Entry<HoverInfoZone, List<GeographicalInfo>> {
		private HoverInfoZone key;
		private List<GeographicalInfo> values;
		public GeographicalDataStorageEntry(HoverInfoZone key, List<GeographicalInfo> values) {
			this.key = key;
			this.values = values;
		}

		@Override
		public HoverInfoZone getKey() {
			return key;
		}

		@Override
		public List<GeographicalInfo> getValue() {
			return values;
		}

		@Override
		public List<GeographicalInfo> setValue(List<GeographicalInfo> value) {
			this.values = value;
			return values;
		}
	}

	private void logValues() {
		logger.log(Level.INFO, " - " + this.size + " ELEMENTS - ");
		logger.log(Level.INFO, "UNKNOWN:");
		for (GeographicalInfo info : this.unknown_location) {
			logger.log(Level.INFO, info.toString());
		}
		logger.log(Level.INFO, "****************");
		logger.log(Level.INFO, "SAFER:");
		for (GeographicalInfo info : this.safer_location) {
			logger.log(Level.INFO, info.toString());
		}
		logger.log(Level.INFO, "****************");
		logger.log(Level.INFO, "RISK:");
		for (GeographicalInfo info : this.risk_location) {
			logger.log(Level.INFO, info.toString());
		}
		logger.log(Level.INFO, "****************");
		logger.log(Level.INFO, "RELEVANT:");
		for (GeographicalInfo info : this.relevant_location) {
			logger.log(Level.INFO, info.toString());
		}
		logger.log(Level.INFO, "****************");
		logger.log(Level.INFO, "OUT_RELEVANT:");
		for (GeographicalInfo info : this.out_relevant_location) {
			logger.log(Level.INFO, info.toString());
		}
		logger.log(Level.INFO, "****************");
	}
}
