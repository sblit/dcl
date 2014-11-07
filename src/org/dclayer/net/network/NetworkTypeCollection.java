package org.dclayer.net.network;

import java.util.LinkedList;


/**
 * A collection of joined network types and attributes
 * @author Martin Exner
 */
public class NetworkTypeCollection {
	
	private LinkedList<NetworkType> networkTypes = new LinkedList<>();
	private NetworkType[] curArray;
	
	public synchronized void addNetworkType(NetworkType networkType) {
		curArray = null;
		this.networkTypes.add(networkType);
	}
	
	public synchronized NetworkType findLocal(NetworkType findNetworkType) {
		for(NetworkType networkType : networkTypes) {
			if(networkType.equals(findNetworkType)) return networkType;
		}
		return null;
	}
	
	public NetworkType[] copyArray() {
		if(curArray == null) {
			synchronized(this) {
				curArray = this.networkTypes.toArray(new NetworkType[this.networkTypes.size()]);
			}
		}
		return curArray;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		boolean comma = false;
		for(NetworkType networkType : networkTypes) {
			if(comma) stringBuilder.append(", ");
			else comma = true;
			stringBuilder.append(networkType.toString());
		}
		return stringBuilder.toString();
	}
	
}
