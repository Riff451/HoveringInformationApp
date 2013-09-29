package it.tomlolriff.hoveringinformation.activity;

import it.tomlolriff.hoveringinformation.agents.interfaces.PieceHICallbacks;

public class PieceHICallbacksFactory {

	public PieceHICallbacksFactory() {
	}

	public PieceHICallbacks createCallbacks() {
		return new PieceHICallbacksImpl();
	}
}
