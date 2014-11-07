package org.dclayer.net.network;

import org.dclayer.net.Data;
import org.dclayer.net.address.Address;

public class NetworkSlot {
	
	private NetworkPacket networkPacket;
	private NetworkType networkType;
	private Data addressData;
	
	private NetworkSlot(NetworkType networkType, Data addressData) {
		this.networkType = networkType;
		this.addressData = addressData;
		this.networkPacket = networkType.makeNetworkPacket(this);
	}
	
	public NetworkSlot(NetworkType networkType) {
		this(networkType, (Data) null);
	}
	
	public NetworkSlot(NetworkType networkType, Address address) {
		this(networkType, networkType.scaleAddress(address));
	}
	
	public NetworkPacket getNetworkPacket() {
		return networkPacket;
	}
	
	public NetworkType getNetworkType() {
		return networkType;
	}
	
	public Data getAddressData() {
		return addressData;
	}
	
}
