package it.tomlolriff.hoveringinformation.activity.components;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;

public interface ConnectionFragmentListener {
	/**
	 * Richiesta di annullamento della connessione che si sta cercando di
	 * instaurare
	 */
    void cancelDisconnect();
    /**
     * Richiesta di connessione ad un device
     * @param device WifiP2pDevice - device a cui inviare la richiesta
     */
    void connect(WifiP2pDevice device);
    /**
     * Richiesta di disconnessione
     */
    void disconnect();
    /**
     * Richiesta del nome del dispositivo WiFi Direct
     */
    String getDirectName();
    /**
     * Richiesta dell'attuale lista di P2P Peers
     */
    WifiP2pDeviceList getWifiP2pDeviceList();
}
