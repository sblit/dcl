package org.dclayer.application;

import org.dclayer.net.network.NetworkType;

public class NetworkEndpoint {
	
	private NetworkType networkType;
	private OnReceiveListener onReceiveListener;
	
	public NetworkEndpoint(NetworkType networkType, OnReceiveListener onReceiveListener) {
		this.networkType = networkType;
		this.onReceiveListener = onReceiveListener;
	}
	
	public NetworkType getNetworkType() {
		return networkType;
	}
	
	public OnReceiveListener getOnReceiveListener() {
		return onReceiveListener;
	}
	
}
