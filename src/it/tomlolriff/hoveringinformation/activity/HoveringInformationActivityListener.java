package it.tomlolriff.hoveringinformation.activity;

import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WifiDirectInfo;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;

public interface HoveringInformationActivityListener {
	
	void doOnAgentSuccessfullyStarted();
	void doOnContainerSuccessfullyStarted();
	void doOnMainContainerSuccessfullyStarted();
	void doOnWifiDInfoChange(WifiDirectEvent event, WifiDirectInfo info, Object extraInfo);
	void onServiceConnectionConnected(WifiDirectInfo wifiDirectInfo);
	void onServiceConnectionDisconnected();
	
	
}
