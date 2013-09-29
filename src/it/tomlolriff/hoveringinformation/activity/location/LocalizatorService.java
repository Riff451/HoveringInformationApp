package it.tomlolriff.hoveringinformation.activity.location;

import it.tomlolriff.hoveringinformation.activity.Utility;
import it.tomlolriff.hoveringinformation.agents.interfaces.SetLocationInfo;
import it.tomlolriff.jadeandroidonwifidirect.location.LocationStrategyUIListener;
import it.tomlolriff.jadeandroidonwifidirect.location.MockProvider;
import it.tomlolriff.jadeandroidonwifidirect.location.exceptions.LocationStrategyException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;

public class LocalizatorService extends BroadcastReceiver {
	
	public static final String ACTION_REQUEST_LOCATION = "it.tomlolriff.hoveringinformation.action.REQUEST_LOCATION";
	
	private SetLocationInfo localizatorInterface = null;
	private DummyMockLocationStrategy locStrategy = null;
	private String mockProviderName = null;
	private Location myLocation = null;
	private SenderToAgent sendToAgent = null;
	
	public LocalizatorService(SetLocationInfo localizatorInterface, LocationManager locManager) {
		
		try {
			this.localizatorInterface = localizatorInterface;
			locStrategy = DummyMockLocationStrategy.getInstance(
					locManager, new LocationStrategyListener());
			mockProviderName = "MockGPSProvider" + Math.random();
			locStrategy.addMockProvider(new MockProvider(mockProviderName, false, true, false, 
					false, false, false, true, Criteria.POWER_HIGH, Criteria.ACCURACY_HIGH));
			sendToAgent = new SenderToAgent("Sender To LocalizatorAgent Thread");
		} catch (LocationStrategyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if(action.equalsIgnoreCase(ACTION_REQUEST_LOCATION)) {
			sendToAgent = new SenderToAgent("SenderToLocalizatorAgentThread");
			sendToAgent.start();
			Handler handler = new Handler(sendToAgent.getLooper());
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					if(myLocation != null) {
						localizatorInterface.setLocationInfo(myLocation.getLatitude(), myLocation.getLongitude());
					}
					sendToAgent.quit();
				}
			});
		}
	}
	
	public void startLocStrategy() {
		locStrategy.start();
	}
	
	public void stopLocStrategy() {
		locStrategy.stop();
	}
	
	public void sendMockLocation(double latitudine, double longitudine, float direzione) {
		Location mockLocation = new Location(mockProviderName);
		mockLocation.setLatitude(latitudine);
		mockLocation.setLongitude(longitudine);
		mockLocation.setBearing(direzione);
		mockLocation.setTime(System.currentTimeMillis());
		mockLocation.setAccuracy(Criteria.ACCURACY_HIGH);
		try {
			Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");
			if (locationJellyBeanFixMethod != null) {
			   locationJellyBeanFixMethod.invoke(mockLocation);
			}
			locStrategy.notifyMockLocation(mockLocation, mockProviderName);
		} catch (LocationStrategyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			try {
				locStrategy.notifyMockLocation(mockLocation, mockProviderName);
			} catch (LocationStrategyException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void moveToSafeZone() {
		// mi sposto esattamente sull'anchor location
		sendMockLocation(Utility.X_ANCHOR, Utility.Y_ANCHOR, (float)0.0);
	}
	
	public void moveToRiskZone() {
		sendMockLocation(Utility.X_ANCHOR + Utility.RISK_RADIUS - ((Utility.RISK_RADIUS - Utility.SAFE_RADIUS) / 2), Utility.Y_ANCHOR, (float)0.0);
	}
	
	public void moveToRelevantZone() {
		sendMockLocation(Utility.X_ANCHOR + Utility.RELEVANT_RADIUS - ((Utility.RELEVANT_RADIUS - Utility.RISK_RADIUS) / 2), Utility.Y_ANCHOR, (float)0.0);
	}
	
	public void moveToOutOfRelevantZone() {
		// ho aggiunto 0.001, ma qualsiasi valore aggiungo sono fuori dalla relevant
		sendMockLocation(Utility.X_ANCHOR + Utility.RELEVANT_RADIUS + 0.001, Utility.Y_ANCHOR, (float)0.0);
	}
	
	public Location getMyLocation() {
		return myLocation;
	}
	
	private class LocationStrategyListener extends LocationStrategyUIListener {

		@Override
		public void doOnCurrentBestLocationChange(Location currentBestLocation) {
			// Devo salvarmi il valore e quando me lo chiede passarlo al LocalizatorAgent
			myLocation = currentBestLocation;
		}
	}
	
	private class SenderToAgent extends HandlerThread {

		private SenderToAgent(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}
		
		
	}

}
