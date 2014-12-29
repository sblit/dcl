package org.dclayer.net.network;

import java.util.Set;

import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.net.NeighborRequest;
import org.dclayer.net.address.Address;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.slot.GenericNetworkSlot;

public abstract class ApplicationNetworkInstance extends NetworkInstance {

	private GenericNetworkSlot<? extends NetworkNode> networkSlot;
	
	private Set<NeighborRequest> requestedNeighbors;
	
	public ApplicationNetworkInstance(HierarchicalLevel parentHierarchicalLevel, NetworkType networkType, Address address) {
		super(parentHierarchicalLevel, networkType, address, true);
	}
	
	public void setNetworkSlot(GenericNetworkSlot<? extends NetworkNode> networkSlot) {
		this.networkSlot = networkSlot;
	}
	
	public GenericNetworkSlot<? extends NetworkNode> getNetworkSlot() {
		return networkSlot;
	}
	
	public void setRequestedNeighbors(Set<NeighborRequest> requestedNeighbors) {
		this.requestedNeighbors = requestedNeighbors;
	}
	
	public Set<NeighborRequest> getRequestedNeighbors() {
		return requestedNeighbors;
	}
	
	@Override
	public final boolean onForward(NetworkPacket networkPacket) {
		if(networkSlot == null) return false;
		return onForward(networkPacket, networkSlot);
	}
	
	public abstract boolean onForward(NetworkPacket networkPacket, GenericNetworkSlot<? extends NetworkNode> networkSlot);
	
}
