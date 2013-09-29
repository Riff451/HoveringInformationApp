package it.tomlolriff.hoveringinformation.activity;

import it.tomlolriff.hoveringinformation.R;
import it.tomlolriff.hoveringinformation.activity.components.ConnectionFragment;
import it.tomlolriff.hoveringinformation.activity.components.ConnectionFragmentListener;
import it.tomlolriff.hoveringinformation.activity.components.HoverInfoFragment;
import it.tomlolriff.hoveringinformation.activity.components.HoverInfoFragmentListener;
import it.tomlolriff.hoveringinformation.activity.components.TabsAdapter;
import it.tomlolriff.hoveringinformation.activity.location.LocalizatorService;
import it.tomlolriff.hoveringinformation.activity.location.RequestLocationInfoAndroidImpl;
import it.tomlolriff.hoveringinformation.agents.BroadRepPseudoDirectionHIAgent;
import it.tomlolriff.hoveringinformation.agents.BroadcastReplicationHIAgent;
import it.tomlolriff.hoveringinformation.agents.ContainerSubscriberAgent;
import it.tomlolriff.hoveringinformation.agents.LocalizatorAgent;
import it.tomlolriff.hoveringinformation.agents.PieceHoverInformationCallbacksProviderAgent;
import it.tomlolriff.hoveringinformation.agents.PieceHoveringInformationAgent;
import it.tomlolriff.hoveringinformation.agents.interfaces.SetLocationInfo;
import it.tomlolriff.hoveringinformation.agents.types.HoverInfoZone;
import it.tomlolriff.jadeandroidonwifidirect.activity.JadeWifiDirectActivity;
import it.tomlolriff.jadeandroidonwifidirect.jade.exceptions.AgentContainerNotStartedException;
import it.tomlolriff.jadeandroidonwifidirect.jade.types.AgentInfo;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WifiDirectInfo;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;
import jade.core.AID;
import jade.util.Logger;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HoveringInformationActivity extends JadeWifiDirectActivity 
	implements ConnectionFragmentListener, HoverInfoFragmentListener {
	
	protected Logger logger = Logger.getJADELogger(this.getClass().getName());
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	TabsAdapter mTabsAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	private List<HoveringInformationActivityListener> myListeners = null;
	
	private String subscriberName = null;
	private String localizatorName = null;
	private String callbacksHIProviderName = null;
	
	private LocalizatorService locService = null;
	
	
	private static final String BROADCAST_ALGORITHM = "BroadcastAlgorithm";
	private static final String BROADCAST_PSEUDO_DIR_ALGORITHM = "BroadcastPseudoDirAlgorithm";
	private String hoverinfoAlgorithmSelected = BROADCAST_ALGORITHM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hovering_information_app);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
        //mViewPager.setId(R.id.pager);
        //setContentView(mViewPager);
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Set up TabsAdapter
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.title_connection_fragment)),
                ConnectionFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.title_hoverinfo_fragment)),
				HoverInfoFragment.class, null);
		
		
		myListeners = new ArrayList<HoveringInformationActivityListener>();
		
		if (savedInstanceState != null) {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeAllHoveringInformationActivityListener();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_hovering_information_app, menu);
		return true;
	}

	// gestione del menù
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                // Since this is the system wireless settings activity, it's
                // not going to send us a result. We will be notified by
                // WiFiDeviceBroadcastReceiver instead.
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                return true;
            case R.id.create_piece:
            	AlertDialog.Builder builder_create = new AlertDialog.Builder(this);
            	if(isPlatformReady() && locService != null) {
            		final EditText input = new EditText(this);
            		builder_create.setView(input);
            		builder_create.setTitle("Nuova Hovering Information");
            		
            		builder_create.setSingleChoiceItems(R.array.hoverinfo_algorithms_array, 0, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch(which) 
							{
							case 0:
								hoverinfoAlgorithmSelected = BROADCAST_ALGORITHM;
								break;
							case 1:
								hoverinfoAlgorithmSelected = BROADCAST_PSEUDO_DIR_ALGORITHM;
								break;
							default:
								break;
							}
						}
					});
            		
            		builder_create.setPositiveButton(R.string.crea, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							createHoverInfoAgent(input.getText().toString());
						}
					});
            	} else {
            		builder_create = createWarningDialog();
            	}
            	builder_create.create().show();
            	return true;
            case R.id.move:
            	AlertDialog.Builder builder_move = new AlertDialog.Builder(this);
            	if(locService == null || !isPlatformReady()) {
         		   builder_move = createWarningDialog();
         	   	} else {
         		   builder_move.setTitle("Spostati in:")
            		.setItems(R.array.zones_array, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int which) {
	                	   // The 'which' argument contains the index position
	                	   // of the selected item
	                	   switch(which)
	                	   {
	                	   case 0: // to safe zone
	                		   locService.moveToSafeZone();
	                		   setZone(HoverInfoZone.SAFER);
	                		   break;
	                	   case 1: // to risk zone
	                		   locService.moveToRiskZone();
	                		   setZone(HoverInfoZone.RISK);
	                		   break;
	                	   case 2: // to relevant zone
	                		   locService.moveToRelevantZone();
	                		   setZone(HoverInfoZone.RELEVANT);
	                		   break;
	                	   case 3: // to out-of-relevant zone
	                		   locService.moveToOutOfRelevantZone();
	                		   setZone(HoverInfoZone.OUT_RELEVANT);
	                		   break;
	                	   }
	                   }
            		});
         	   	}
            	builder_move.create().show();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Setta un TextView con la zona attuale
     * @param zone
     */
    private void setZone(HoverInfoZone zone) {
		TextView text = (TextView) findViewById(R.id.textView_zone);
        if (text != null) {
        	text.setText("Zona: " + zone);
        }
	}

    /**
     * Crea e restituisce una {@link AlertDialog} di warning per notificare la mancanza di una platform pronta
     * per eseguire una operazione
     * @return una {@link AlertDialog} di warning per notificare la mancanza di una platform pronta
     * per eseguire una operazione
     */
    private Builder createWarningDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Hovering Information non pronto!");
   		builder.setMessage("Connessione mancante o Agenti Jade non ancora attivi!");
    	return builder;
    }

    /**
     * Crea un agente di tipo {@link PieceHoveringInformationAgent}
     * @param payload il payload contenuto nell'agente
     * @param hoverinfoAlgorithm algoritmo da usare per le policies del piece
     */
    private void createHoverInfoAgent(String payload) {
    	locService.moveToSafeZone(); // mi sposto in automatico sulla nostra unica anchor
    	setZone(HoverInfoZone.SAFER);
    	UUID uuid = UUID.randomUUID();
    	if(hoverinfoAlgorithmSelected.equalsIgnoreCase(BROADCAST_ALGORITHM)) {
    		createAndStartAgent(uuid.toString(), BroadcastReplicationHIAgent.class.getName(),
	    			new Object[] {
	    				uuid,
	    				payload,
	    				// suppongo che sia posizionato esattamente sull'anchor
	    				Utility.X_ANCHOR,
	    				Utility.Y_ANCHOR
	    			});
    	} else if(hoverinfoAlgorithmSelected.equalsIgnoreCase(BROADCAST_PSEUDO_DIR_ALGORITHM)) {
	    	createAndStartAgent(uuid.toString(), BroadRepPseudoDirectionHIAgent.class.getName(),
	    			new Object[] {
	    				uuid,
	    				payload,
	    				// suppongo che sia posizionato esattamente sull'anchor
	    				Utility.X_ANCHOR,
	    				Utility.Y_ANCHOR
	    			});
    	}
    }
    
    @Override
    protected void doOnContainerSuccessfullyStarted() {
    	for(HoveringInformationActivityListener l : myListeners){
			l.doOnContainerSuccessfullyStarted();
		}
    	startVeryFirstAgents();
    }

    @Override
    protected void doOnMainContainerSuccessfullyStarted() {
    	for(HoveringInformationActivityListener l : myListeners){
			l.doOnMainContainerSuccessfullyStarted();
		}
    	startVeryFirstAgents();
    }

	private void startVeryFirstAgents() {
		// Creo ed avvio il PieceHoverInformationCallbacksProviderAgent
    	callbacksHIProviderName = Utility.generateMACizeName("PieceHoverInformationCallbacksProviderAgent", (WifiManager) this.getSystemService(
                WIFI_SERVICE), "");
    	LocalBroadcastManagerExecutor.initiateInstance(LocalBroadcastManager.getInstance(this));
    	createAndStartAgent(callbacksHIProviderName, PieceHoverInformationCallbacksProviderAgent.class.getName(),
    			new Object[] {
    				new PieceHICallbacksFactory() 
    			});
    	// Creo ed avvio il ContainerSubscriberAgent
    	subscriberName = Utility.generateMACizeName("ContainerSubscriberAgent", (WifiManager) this.getSystemService(
                WIFI_SERVICE), "");
    	createAndStartAgent(subscriberName, ContainerSubscriberAgent.class.getName(), null);
	}
	
	@Override
	protected void doOnAgentSuccessfullyStarted(AgentInfo agentInfo) {
		
		logger.log(Logger.WARNING, "Agente Creato: " + agentInfo.getAgentName());
		
		// Vengo dalla creazione del subscriber
		if(subscriberName != null && agentInfo.getAgentName().contains(subscriberName)) {
			// Credo ed avvio il LocalizatorAgent
	    	localizatorName = Utility.generateMACizeName("LocalizatorAgent", (WifiManager) this.getSystemService(
	                WIFI_SERVICE), "");
	    	createAndStartAgent(localizatorName, LocalizatorAgent.class.getName(),
	    			new Object[]{
			    		Utility.X_ANCHOR, 
						Utility.Y_ANCHOR,
						Utility.SAFE_RADIUS,
						Utility.RISK_RADIUS,
						Utility.RELEVANT_RADIUS,
						new RequestLocationInfoAndroidImpl(LocalBroadcastManager.getInstance(this)),
			    		new AID(subscriberName, AID.ISLOCALNAME)});
		}

		// Vengo dalla creazione del localizer
		if(localizatorName != null && 
				agentInfo.getAgentName().contains(localizatorName)) { // All'avvio del LocalizatorAgent inizio il rilevamento della posizione
			try {
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(LocalizatorService.ACTION_REQUEST_LOCATION);
				locService = new LocalizatorService(
						getO2AInterface(localizatorName, SetLocationInfo.class),
						(LocationManager)getSystemService(LOCATION_SERVICE));
				LocalBroadcastManager.getInstance(this).registerReceiver(
						locService, 
						intentFilter);
				
				// FIXME devo decidere quando stopparla
				locService.startLocStrategy();
				locService.sendMockLocation(
						Utility.LATITUDE_START,
						Utility.LONGITUDE_START,
						Utility.BEARING_START);
			} catch (StaleProxyException e) {
				logger.log(Logger.SEVERE, "doOnAgentSuccessfullyStarted ERROR: " + e);
			} catch (AgentContainerNotStartedException e) {
				logger.log(Logger.SEVERE, "doOnAgentSuccessfullyStarted ERROR: " + e);
			}
		}
		
		// mostro il campo di testo della zona attuale se la piattaforma jade è pronta
		if(isPlatformReady() && locService != null) {
			((RelativeLayout)findViewById(R.id.layout_current_zone)).setVisibility(View.VISIBLE);
			((View) findViewById(R.id.line_zone)).setVisibility(View.VISIBLE);
		}

		for(HoveringInformationActivityListener l : myListeners){
			l.doOnAgentSuccessfullyStarted();
		}
	}

    // Richiamato a seguito di un evento WiFi Direct. Devo tenere conto della
    // priorità di certi eventi rispetto ad altri per l'eventuale aggiornamento della UI.
    @Override
    protected void doOnWifiDInfoChange(WifiDirectEvent event, WifiDirectInfo info, Object extraInfo){
    	for(HoveringInformationActivityListener l : myListeners){
			l.doOnWifiDInfoChange(event, info, extraInfo);
		}
    }

    // quando mi connetto al Service aggiorno la UI con le nuove info WiFi Direct
    @Override
    protected void onServiceConnectionConnected(WifiDirectInfo wifiDirectInfo) {
    	for(HoveringInformationActivityListener l : myListeners){
			l.onServiceConnectionConnected(wifiDirectInfo);
		}
    }
    
    @Override
    protected void onServiceConnectionDisconnected() {
    	for(HoveringInformationActivityListener l : myListeners){
			l.onServiceConnectionDisconnected();
		}
    }

    @Override
    public void cancelDisconnect(){
    	// TODO
    }
    
    @Override
    public void disconnect(){
    	// TODO
    }
    
    // Mi connetto al peer selezionato dall'utente
    @Override
    public void connect(WifiP2pDevice device){
    	connectToPeer(device);
    }

	@Override
	public String getDirectName() {
		return getMyDirectName();
	}
	
	@Override
	public WifiP2pDeviceList getWifiP2pDeviceList() {
		return getCurrentWifiP2pDeviceList();
	}
	
	public void addHoveringInformationActivityListener(HoveringInformationActivityListener l) {
		myListeners.add(l);
	}
	
	public void removeHoveringInformationActivityListener(HoveringInformationActivityListener l) {
		myListeners.remove(l);
	}
	
	private void removeAllHoveringInformationActivityListener() {
		myListeners.clear();
	}
	
}
