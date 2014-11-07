package org.dclayer.net.interservice;

import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkPacket;
import org.dclayer.net.network.NetworkType;

public interface InterserviceChannelActionListener {
	public void onReadyChange(InterserviceChannel interserviceChannel, boolean ready);
	public void onInConnectionBaseChange(InterserviceChannel interserviceChannel, byte oldInConnectionBase, byte newInConnectionBase);
	public void onRemoteNetworkJoin(InterserviceChannel interserviceChannel, NetworkType networkType, Data addressData);
	public void onRemoteNetworkLeave(InterserviceChannel interserviceChannel, NetworkType networkType, Data addressData);
	public boolean onNetworkPacket(InterserviceChannel interserviceChannel, NetworkPacket networkPacket);
}
