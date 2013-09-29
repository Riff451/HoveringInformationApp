package it.tomlolriff.hoveringinformation.activity.components;

import it.tomlolriff.hoveringinformation.R;
import it.tomlolriff.hoveringinformation.activity.HoveringInformationActivity;
import it.tomlolriff.hoveringinformation.activity.HoveringInformationActivityListener;
import it.tomlolriff.hoveringinformation.activity.models.HoveringInformationPiece;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WifiDirectInfo;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HoverInfoFragment extends ListFragment 
	implements HoveringInformationActivityListener {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	
	/**
	 * Interfaccia di callback per comunicare con l'Activity che ospita il fragment
	 */
	private HoverInfoFragmentListener myListener = null;
	/**
	 * View per il layout del fragment
	 */
    private View mContentView = null;
    /**
	 * Lista di Hovering Information Pieces
	 */
	private List<HoveringInformationPiece> hoverinfos = new ArrayList<HoveringInformationPiece>();
	/**
	 * Activity Hosting
	 */
	private Context context = null;
	
	@Override
    public void onAttach (Activity activity) {
    	super.onAttach(activity);
    	context = activity;
    	myListener = (HoverInfoFragmentListener) activity;
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.hoverinfo_list, null);
        return mContentView;
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new HoverInfoListAdapter(getActivity(), R.layout.row_hoverinfo, hoverinfos));
        ((HoveringInformationActivity)context).addHoveringInformationActivityListener(this);
        LocalBroadcastManager.getInstance(context).registerReceiver(new PieceHoveringInformationReceiver(), getIntentFilter());
    }
	
	private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PieceHoveringInformationReceiver.ACTION_CLONED);
        intentFilter.addAction(PieceHoveringInformationReceiver.ACTION_CREATED);
        intentFilter.addAction(PieceHoveringInformationReceiver.ACTION_DELETED);
        intentFilter.addAction(PieceHoveringInformationReceiver.ACTION_MOVED);

        return intentFilter;
	}

	/**
     * Array adapter for ListFragment that maintains HoveringInformationPiece list.
     */
    private class HoverInfoListAdapter extends ArrayAdapter<HoveringInformationPiece> {
    	/**
    	 * Lista di HoveringInformationPiece
    	 */
    	private List<HoveringInformationPiece> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public HoverInfoListAdapter(Context context, int textViewResourceId,
                List<HoveringInformationPiece> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_hoverinfo, null);
                if(position%2!=0) {
                	v.setBackgroundColor(getResources().getColor(R.color.BackgroundPieceOdd));
                }
            }
            HoveringInformationPiece piece = items.get(position);
            if (piece != null) {
                TextView nomeAgente = (TextView) v.findViewById(R.id.hoverinfo_payload);
                if (nomeAgente != null) {
                	nomeAgente.setText(piece.getMyAgentName());
                }

                TextView payload = (TextView) v.findViewById(R.id.hoverinfo_name);
                if (payload != null) {
                	payload.setText(piece.getPayLoad().toString());
                }
            }

            return v;
        }
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	
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
	}

	@Override
	public void onServiceConnectionConnected(WifiDirectInfo wifiDirectInfo) {
	}

	@Override
	public void onServiceConnectionDisconnected() {
	}

	private void addPiece(HoveringInformationPiece piece){
    	hoverinfos.add(piece);
        ((HoverInfoListAdapter) getListAdapter()).notifyDataSetChanged();
    }

	private void removePiece(HoveringInformationPiece piece){
    	hoverinfos.remove(piece);
        ((HoverInfoListAdapter) getListAdapter()).notifyDataSetChanged();
    }
	
	public class PieceHoveringInformationReceiver extends BroadcastReceiver {
		private static final String ACTION_NAMESPACE = "it.tomlolriff.hoveringinformation.action.HOVERINFO_";
		public static final String EXTRA_PARAM_NAME = "HoverInfoPiece";
		public static final String ACTION_CREATED = ACTION_NAMESPACE + "CREATED";
		public static final String ACTION_CLONED = ACTION_NAMESPACE + "CLONED";
		public static final String ACTION_DELETED = ACTION_NAMESPACE + "DELETED";
		public static final String ACTION_MOVED = ACTION_NAMESPACE + "MOVED";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equalsIgnoreCase(ACTION_CREATED)) {
				HoveringInformationPiece piece = (HoveringInformationPiece)intent.getSerializableExtra(EXTRA_PARAM_NAME);
				addPiece(piece);
			} else if(action.equalsIgnoreCase(ACTION_CLONED)) {
				// alla clonazione non dovrei fare niente di particolare
				logger.log(Logger.INFO, "ACTION_CLONED");
			} else if(action.equalsIgnoreCase(ACTION_MOVED)) {
				HoveringInformationPiece piece = (HoveringInformationPiece)intent.getSerializableExtra(EXTRA_PARAM_NAME);
				removePiece(piece);
			} else if(action.equalsIgnoreCase(ACTION_DELETED)) {
				HoveringInformationPiece piece = (HoveringInformationPiece)intent.getSerializableExtra(EXTRA_PARAM_NAME);
				removePiece(piece);
			}
		}
		
	}
}
