package org.dclayer.net.interservice;

import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkNode;

public interface InterserviceChannelActionListener {
	public void onReadyChange(InterserviceChannel interserviceChannel, boolean ready);
	public void onInConnectionBaseChange(InterserviceChannel interserviceChannel, byte oldInConnectionBase, byte newInConnectionBase);
	public NetworkInstance onRemoteNetworkJoin(InterserviceChannel interserviceChannel, NetworkNode networkNode);
	public void onRemoteNetworkLeave(InterserviceChannel interserviceChannel, NetworkNode networkNode);
}
