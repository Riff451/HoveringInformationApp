package it.tomlolriff.hoveringinformation.activity;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class LocalBroadcastManagerExecutor {
	
	private static LocalBroadcastManagerExecutor instance = null;
	private LocalBroadcastManager lbm = null;
	
	private LocalBroadcastManagerExecutor(LocalBroadcastManager lbm) {
		this.lbm = lbm;
	}
	
	static void initiateInstance(LocalBroadcastManager lbm) {
		if(instance == null) {
			instance = new LocalBroadcastManagerExecutor(lbm);
		}
	}
	
	public static synchronized LocalBroadcastManagerExecutor getInstance() {
		if(instance == null) {
			// FIXME eccezione
		}
		return instance;
	}
	
	public synchronized void sendBroadcast(Intent intent) {
		if(lbm == null) {
			// FIXME eccezione
		}
		lbm.sendBroadcast(intent);
	}
}
