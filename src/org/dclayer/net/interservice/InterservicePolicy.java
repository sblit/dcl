package org.dclayer.net.interservice;

import java.util.LinkedList;
import java.util.List;

import org.dclayer.crypto.key.Key;
import org.dclayer.net.network.NetworkType;

public class InterservicePolicy {
	
	private List<Key> remotePublicKeys = new LinkedList<>();
	private List<NetworkType> networkTypes = new LinkedList<>();
	
	public InterservicePolicy restrictRemotePublicKey(Key... remotePublicKeys) {
		for(Key remotePublicKey : remotePublicKeys) {
			this.remotePublicKeys.add(remotePublicKey);
		}
		return this;
	}
	
	public InterservicePolicy restrictNetworkType(NetworkType... networkTypes) {
		for(NetworkType networkType : networkTypes) {
			this.networkTypes.add(networkType);
		}
		return this;
	}
	
	public List<Key> getRemotePublicKeys() {
		return remotePublicKeys;
	}
	
	public List<NetworkType> getNetworkTypes() {
		return networkTypes;
	}

}
