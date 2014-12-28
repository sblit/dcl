package org.dclayer.net.a2s;

import org.dclayer.net.Data;
import org.dclayer.net.component.AbsKeyComponent;
import org.dclayer.net.componentinterface.AbsKeyComponentI;
import org.dclayer.net.network.NetworkType;

public interface A2SMessageReceiver {

	public void onReceiveRevisionMessage(int revision);
	public void onReceiveDataMessage(int slot, Data addressData, Data data);
	public void onReceiveGenerateKeyMessage();
	public void onReceiveJoinNetworkMessage(NetworkType networkType);
	public void onReceiveSlotAssignMessage(int slot, NetworkType networkType, Data addressData);
	public void onReceiveAddressPublicKeyMessage(AbsKeyComponentI absKeyComponentI);
	public void onReceiveJoinDefaultNetworksMessage();
	public void onReceiveKeyEncryptDataMessage(Data plainData);
	public void onReceiveKeyDecryptDataMessage(Data cipherData);
	public void onReceiveKeyCryptoResponseDataMessage(Data responseData);
	public void onReceiveApplicationChannelRequestMessage(int networkSlotId, int channelSlotId, AbsKeyComponent keyComponent);
	
}
