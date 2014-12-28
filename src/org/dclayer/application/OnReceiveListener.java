package org.dclayer.application;

import org.dclayer.application.networktypeslotmap.NetworkEndpointSlot;
import org.dclayer.net.Data;

public interface OnReceiveListener {

	public void onJoin(NetworkEndpointSlot networkEndpointSlot, Data ownAddressData);
	public void onReceive(NetworkEndpointSlot networkEndpointSlot, Data data, Data sourceAddressData);
	
}
