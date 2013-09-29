package it.tomlolriff.hoveringinformation.activity.location;

import it.tomlolriff.hoveringinformation.agents.interfaces.RequestLocationInfo;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class RequestLocationInfoAndroidImpl implements RequestLocationInfo {
	
	private LocalBroadcastManager lbm = null;
	
	public RequestLocationInfoAndroidImpl(LocalBroadcastManager lbm) {
		this.lbm = lbm;
	}

	@Override
	public void getLocationInfo() {
		Intent intent = new Intent();
		intent.setAction(LocalizatorService.ACTION_REQUEST_LOCATION);
		lbm.sendBroadcast(intent);
	}

}
