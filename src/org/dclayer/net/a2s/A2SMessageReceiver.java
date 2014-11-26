package org.dclayer.net.a2s;

import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.componentinterface.AbsKeyComponentI;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.slot.NetworkSlot;

public interface A2SMessageReceiver {

	public void onReceiveRevisionMessage(int revision);
	public void onReceiveDataMessage(int slot, Data addressData, Data data);
	public void onReceiveGenerateKeyMessage();
	public void onReceiveJoinNetworkMessage(NetworkType networkType);
	public void onReceiveSlotAssignMessage(int slot, NetworkType networkType, Data addressData);
	public void onReceiveAddressPublicKeyMessage(AbsKeyComponentI absKeyComponentI);
	public void onReceiveJoinDefaultNetworksMessage();
	
}
