package it.tomlolriff.hoveringinformation.activity.components;


import it.tomlolriff.hoveringinformation.R;
import it.tomlolriff.hoveringinformation.activity.HoveringInformationActivity;
import it.tomlolriff.hoveringinformation.activity.HoveringInformationActivityListener;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WifiDirectInfo;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WiFiDirectDeviceState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectConnectionState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectDiscoveringState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectFrameworkState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectPeerListState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectState;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * ListFragment per la lista di Wifi Direct Peers e per la visualizzazione
 * delle informazioni WiFi Direct relative a questo dispositivo.
 * 
 * @author riff451 - TomZ85 - Lollo
 */
public class ConnectionFragment extends ListFragment 
	implements HoveringInformationActivityListener {
	
	/**
	 * Activity Hosting
	 */
	private Context context = null;
	/**
	 * Lista di Peers Wifi Direct
	 */
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	/**
	 * View per il layout del fragment
	 */
    private View mContentView = null;
    /**
     * Stato attuale di questo dispositivo direct
     */
    private WiFiDirectDeviceState myDirectState;
    /**
     * Mio nome attuale WiFi Direct
     */
    private String myName;
    /**
	 * ProgressDialog visualizzato alla ricerca dei Peers
	 */
	private ProgressDialog progressDialog = null;
	/**
	 * Interfaccia di callback per comunicare con l'Activity che ospita il fragment
	 */
	private ConnectionFragmentListener myListener = null;
    
    @Override
    public void onAttach (Activity activity) {
    	super.onAttach(activity);
    	context = activity;
    	myListener = (ConnectionFragmentListener) activity;
    	myDirectState = WiFiDirectDeviceState.UNKNOWN;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        ((HoveringInformationActivity)context).addHoveringInformationActivityListener(this);
    }
    
    // Richiedo di connettermi al peer che ho cliccato
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        // Siamo entrambi disponibili ad avviare una nuova piattaforma jade
        if(myDirectState.equals(WiFiDirectDeviceState.AVAILABLE) &&
        		WiFiDirectDeviceState.fromInt(device.status).equals(WiFiDirectDeviceState.AVAILABLE)) {
        	myListener.connect(device);
        }
    }
    
    /**
     * Visualizza una lista di peers nel fragment 
     * @param peerList WifiP2pDeviceList - Lista di peers da visualizzare
     */
    public void setPeerList(WifiP2pDeviceList peerList){
    	peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            return;
        }
    }
    
    /**
     * Svuota la lista di Peers
     */
    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }
    
    /**
     * Update UI for this device
     * @param deviceState WiFiDirectDeviceState object
     */
    public void updateThisDevice(String myName, WiFiDirectDeviceState deviceState) {
        this.myDirectState = deviceState;
        this.myName = myName;
        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText(this.myName);
        view = (TextView) mContentView.findViewById(R.id.my_status);
        view.setText(this.myDirectState.toString());
    }
    
    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
    	/**
    	 * Lista di WifiP2pDevice
    	 */
    	private List<WifiP2pDevice> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(WiFiDirectDeviceState.fromInt(device.status) + " " + (device.isGroupOwner() ? "Main Container (Group Owner)" : ""));
                }
            }

            return v;
        }
    }

	@Override
	public void doOnAgentSuccessfullyStarted() {
	}

	@Override
	public void doOnContainerSuccessfullyStarted() {
	}

	@Override
	public void doOnMainContainerSuccessfullyStarted() {
	}

	@Override
	public void doOnWifiDInfoChange(WifiDirectEvent event, WifiDirectInfo info,
			Object extraInfo) {
		
		if(event.equals(WifiDirectEvent.FRAMEWORK_EVENT)){
    		if(info.getWifiDFrameworkState().equals(WifiDirectFrameworkState.INIT_ERROR)){
    			setInfoMessage("Info: Grave errore di inizializzazione del WiFi Direct Framework");
    		}
    	} else if(event.equals(WifiDirectEvent.ONOFF_EVENT)){
    		if(info.getWifiDState().equals(WifiDirectState.OFF)){
    	    	setInfoMessage("Info: WiFi Direct Spento");
    	    	clearPeers();
    		}
    	} else if(event.equals(WifiDirectEvent.DISCOVERING_EVENT)){
    		if(info.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.START) &&
    				(info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_EMPTY) ||
    				info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_UNAVAILABLE))){
    	    	setInfoMessage("Info: WiFi Direct Discovering");
    	    	onInitiateDiscovery();
    		} else if(info.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.STOP) ||
    				info.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.FAILURE)){
    			onFinishDiscovery();
    		}
    	} else if(event.equals(WifiDirectEvent.THIS_DEVICE_EVENT)){
    		updateThisDevice(myListener.getDirectName(), info.getWifiDDeviceState());
    	} else if(event.equals(WifiDirectEvent.PEER_LIST_EVENT)){
    		if(info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_EMPTY) ||
    				info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_UNAVAILABLE)){
    			clearPeers();
    			if(info.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.START)){
    				onInitiateDiscovery();
    			}
    		} else if(info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_AVAILABLE)){
    			onFinishDiscovery();
    			setPeerList((WifiP2pDeviceList)extraInfo);
    		}
    	} else if(event.equals(WifiDirectEvent.CONNECTION_EVENT)){
    		if(info.getWifiDConnState().equals(WifiDirectConnectionState.CONNECTED)){
    			setInfoMessage("Info: WiFi Direct Connesso");
    		}
    	}
	}
	
	@Override
	public void onServiceConnectionConnected(WifiDirectInfo wifiDirectInfo) {
		refreshUI(wifiDirectInfo);
	}

	@Override
	public void onServiceConnectionDisconnected() {
		// TODO Boh!
	}
	
	/**
     * Imposta un messaggio da visualizzare in un campo di testo per le info generali
     * @param msg {@link String} - messaggio da visualizzare
     */
    private void setInfoMessage(String msg){
    	TextView t = (TextView) getView().findViewById(R.id.info_text);
    	if(t != null){
    		t.setText(msg);
    	}
    }
    
    /**
     *  Mostra un dialog per indicare il progesso mentre si stanno cercando i peers
     */
    private void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(context, "Premere indietro per chiudere la finestra", "Ricerca di P2P Peers in corso", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        
                    }
                });
    }
    
    /**
     * Elimina il ProgressDialog a ricerca finita
     */
    private void onFinishDiscovery(){
    	if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    /**
     * Aggiorna la UI con nuove informazioni circa lo stato del WiFi Direct
     * @param info {@link WifiDirectInfo} - info sullo stato attuale del WiFi Direct
     */
     private void refreshUI(WifiDirectInfo info){
     	// refresh delle info generali (vado in ordine di priorità)
     	if(info.getWifiDConnState().equals(WifiDirectConnectionState.CONNECTED)){
     		setInfoMessage("Info: WiFi Direct Connesso");
     	} else if(info.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.START)){
     		setInfoMessage("Info: WiFi Direct Discovering");
     	} else if(info.getWifiDState().equals(WifiDirectState.ON)){
     		setInfoMessage("Info: WiFi Direct Acceso");
     	} else if(info.getWifiDState().equals(WifiDirectState.OFF)){
     		setInfoMessage("Info: WiFi Direct Spento");
     	}
     	
     	//refresh delle mie info
     	updateThisDevice(myListener.getDirectName(), info.getWifiDDeviceState());
     	
     	//refresh della lista di peers
     	if(info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_AVAILABLE)){
     		setPeerList(myListener.getWifiP2pDeviceList());
     	} else if(info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_UNAVAILABLE) ||
     			info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_EMPTY)){
     		clearPeers();
     	}
     	
     	//refresh del progressDialog
     	if(info.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.START) &&
     			info.getWifiDPeerListState().equals(WifiDirectPeerListState.LIST_UNAVAILABLE)){
     		onInitiateDiscovery();
     	}
     }
}
