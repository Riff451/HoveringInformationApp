package it.tomlolriff.hoveringinformation.activity;

import it.tomlolriff.hoveringinformation.activity.components.HoverInfoFragment.PieceHoveringInformationReceiver;
import it.tomlolriff.hoveringinformation.activity.models.HoveringInformationPiece;
import it.tomlolriff.hoveringinformation.agents.interfaces.PieceHICallbacks;
import android.content.Intent;

public class PieceHICallbacksImpl implements PieceHICallbacks {
	private static final long serialVersionUID = -728845661752293741L;

	public PieceHICallbacksImpl() {
	}

	@Override
	public void onPieceCreated(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece) {
		//callBroadcastReceiver(new HoveringInformationPiece(aid, payload, geoInfo), PieceHoveringInformationReceiver.ACTION_CREATED);
		callBroadcastReceiver(piece, PieceHoveringInformationReceiver.ACTION_CREATED);
	}

	@Override
	public void onPieceCloned(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece) {
		//callBroadcastReceiver(new HoveringInformationPiece(aid, payload, geoInfo), PieceHoveringInformationReceiver.ACTION_CLONED);
		callBroadcastReceiver(piece, PieceHoveringInformationReceiver.ACTION_CLONED);
	}

	@Override
	public void onPieceMoved(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece) {
		//callBroadcastReceiver(new HoveringInformationPiece(aid, payload, geoInfo), PieceHoveringInformationReceiver.ACTION_MOVED);
		callBroadcastReceiver(piece, PieceHoveringInformationReceiver.ACTION_MOVED);
	}

	@Override
	public void onPieceDeleted(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece) {
		//callBroadcastReceiver(new HoveringInformationPiece(aid, payload, geoInfo), PieceHoveringInformationReceiver.ACTION_DELETED);
		callBroadcastReceiver(piece, PieceHoveringInformationReceiver.ACTION_DELETED);
	}
	
	private void callBroadcastReceiver(HoveringInformationPiece piece, String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(PieceHoveringInformationReceiver.EXTRA_PARAM_NAME, piece);
		LocalBroadcastManagerExecutor.getInstance().sendBroadcast(intent);
	}

}
