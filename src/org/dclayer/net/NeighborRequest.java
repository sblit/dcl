package org.dclayer.net;

import org.dclayer.crypto.key.Key;

public class NeighborRequest {

	private Key publicKey;
	private String actionIdentifier;
	
	public NeighborRequest(Key publicKey, String actionIdentifier) {
		this.publicKey = publicKey;
		this.actionIdentifier = actionIdentifier;
	}
	
	public Key getPublicKey() {
		return publicKey;
	}
	
	public String getActionIdentifier() {
		return actionIdentifier;
	}
	
	@Override
	public int hashCode() {
		return publicKey.hashCode() + actionIdentifier.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof NeighborRequest)) return false;
		NeighborRequest neighborRequest = (NeighborRequest) o;
		return publicKey.equals(neighborRequest.publicKey) && actionIdentifier.equals(neighborRequest.actionIdentifier);
	}
	
}
