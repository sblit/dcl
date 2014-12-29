package org.dclayer.application;

import org.dclayer.net.network.NetworkType;

public class NetworkEndpoint {
	
	private NetworkType networkType;
	private NetworkEndpointSlotActionListener onReceiveListener;
	
	public NetworkEndpoint(NetworkType networkType, NetworkEndpointSlotActionListener onReceiveListener) {
		this.networkType = networkType;
		this.onReceiveListener = onReceiveListener;
	}
	
	public NetworkType getNetworkType() {
		return networkType;
	}
	
	public NetworkEndpointSlotActionListener getOnReceiveListener() {
		return onReceiveListener;
	}
	
}
