package it.tomlolriff.hoveringinformation.agents.interfaces;

import it.tomlolriff.hoveringinformation.activity.models.HoveringInformationPiece;

import java.io.Serializable;

public interface PieceHICallbacks extends Serializable {
	void onPieceCreated(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece);
	void onPieceCloned(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece);
	void onPieceMoved(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece);
	void onPieceDeleted(/*GeographicalInfo geoInfo, Object payload, AID aid*/ HoveringInformationPiece piece);
}
