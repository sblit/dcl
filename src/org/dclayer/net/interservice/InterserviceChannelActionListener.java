package org.dclayer.net.interservice;

import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.slot.NetworkSlot;

public interface InterserviceChannelActionListener {
	public void onReadyChange(InterserviceChannel interserviceChannel, boolean ready);
	public void onNewRemoteNetworkNode(InterserviceChannel interserviceChannel, NetworkNode networkNode, NetworkSlot localNetworkSlot);
	public void onRemoveRemoteNetworkNode(InterserviceChannel interserviceChannel, NetworkNode networkNode);
}
