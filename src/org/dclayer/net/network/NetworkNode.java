package org.dclayer.net.network;

import org.dclayer.net.Data;
import org.dclayer.net.address.Address;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.component.NetworkPayload;
import org.dclayer.net.network.properties.CommonNetworkPayloadProperties;
import org.dclayer.net.network.routing.ForwardDestination;
import org.dclayer.net.network.routing.RoutingTable;

public abstract class NetworkNode implements ForwardDestination {
	
	private NetworkType networkType;
	private Address address;
	
	private Data scaledAddress;
	
	private NetworkPayload outNetworkPayload;
	
	public NetworkNode(NetworkType networkType, Address address) {
		this.networkType = networkType;
		this.address = address;
		this.scaledAddress = networkType.scaleAddress(address);
	}
	
	public NetworkType getNetworkType() {
		return networkType;
	}
	
	@Override
	public Address getAddress() {
		return address;
	}
	
	@Override
	public Data getScaledAddress() {
		return scaledAddress;
	}
	
	public void setOutNetworkPayload(CommonNetworkPayloadProperties commonNetworkPayloadProperties) {
		this.outNetworkPayload = networkType.makeOutNetworkPayload(scaledAddress, commonNetworkPayloadProperties);
	}
	
	public NetworkPayload getOutNetworkPayload() {
		return outNetworkPayload;
	}
	
	public RoutingTable getRoutingTable() {
		return null;
	}
	
	public boolean forward(NetworkPacket networkPacket) {
		return this.onForward(networkPacket);
	}
	
	@Override
	public String toString() {
		return String.format("%s address %s", networkType, scaledAddress);
	}
	
}
