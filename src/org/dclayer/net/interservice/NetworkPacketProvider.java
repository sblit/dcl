package org.dclayer.net.interservice;

import org.dclayer.net.network.NetworkPacket;


public interface NetworkPacketProvider {

	public NetworkPacket getNetworkPacket(int slot);
	
}
