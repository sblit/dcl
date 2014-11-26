package org.dclayer.application;

import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkType;

public interface OnReceiveListener {

	public void onJoin(NetworkType networkType, Data ownAddressData);
	public void onReceive(NetworkType networkType, Data data, Data sourceAddressData);
	
}
