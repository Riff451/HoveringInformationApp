package it.tomlolriff.hoveringinformation.activity.location;

import it.tomlolriff.jadeandroidonwifidirect.location.LocationStrategy;
import it.tomlolriff.jadeandroidonwifidirect.location.LocationStrategyMode;
import it.tomlolriff.jadeandroidonwifidirect.location.LocationStrategyUIListener;
import android.location.Location;
import android.location.LocationManager;

public class DummyMockLocationStrategy extends LocationStrategy {
	
	private static DummyMockLocationStrategy instance = null;
	
	private DummyMockLocationStrategy (LocationManager locManager, LocationStrategyUIListener listener){
		super(locManager, LocationStrategyMode.MOCK, listener);
	}
	
	public static DummyMockLocationStrategy getInstance(LocationManager locManager, LocationStrategyUIListener listener) {
		DummyMockLocationStrategy res = null;;
		if(instance == null){
			res = new DummyMockLocationStrategy(locManager, listener);
		}
		return res;
	}
	
	@Override
	protected void doOnLocationChanged(Location location) {
		/*if( getBestLocation() == null || (location.getAccuracy() < getBestLocation().getAccuracy()) ){
			setBestLocation(location);
		}*/
		// la nostra Strategy al momento è proprio stupida: dato che i cambiamenti
		// di Location sono fittizi, ad ognuno questi noi settiamo la nostra best Location
		// senza fare altri tipi di ragionamenti
		setBestLocation(location);
	}
	
}
