package org.dclayer.net.network;

import org.dclayer.net.Data;
import org.dclayer.net.address.Address;
import org.dclayer.net.crisp.CrispPacket;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.component.NetworkPayload;
import org.dclayer.net.network.properties.CommonNetworkPayloadProperties;
import org.dclayer.net.network.routing.ForwardDestination;
import org.dclayer.net.network.routing.RoutingTable;

public abstract class NetworkNode<T> implements ForwardDestination<T> {
	
	private NetworkType networkType;
	private Address address;
	
	private Data scaledAddress;
	
	private T identifierObject;
	
	private NetworkPayload outNetworkPayload;
	
	private CrispPacket outCrispPacket;
	
	private boolean endpoint = false;
	
	public NetworkNode(NetworkType networkType, Address address, T identifierObject, boolean endpoint) {
		this.networkType = networkType;
		this.address = address;
		this.identifierObject = identifierObject;
		this.endpoint = endpoint;
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
	
	@Override
	public T getIdentifierObject() {
		return identifierObject;
	}
	
	public void setOutNetworkPayload(CommonNetworkPayloadProperties commonNetworkPayloadProperties) {
		this.outNetworkPayload = networkType.makeOutNetworkPayload(scaledAddress, commonNetworkPayloadProperties);
	}
	
	public NetworkPayload getOutNetworkPayload() {
		return outNetworkPayload;
	}
	
	public void setOutCrispPacket() {
		this.outCrispPacket = new CrispPacket();
	}
	
	public CrispPacket getOutCrispPacket() {
		return outCrispPacket;
	}
	
	public RoutingTable getRoutingTable() {
		return null;
	}
	
	public boolean forward(NetworkPacket networkPacket) {
		return this.onForward(networkPacket);
	}
	
	public final boolean isEndpoint() {
		return endpoint;
	}
	
	@Override
	public String toString() {
		return String.format("%s address %s", networkType, scaledAddress);
	}
	
}
