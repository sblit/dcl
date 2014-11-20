package org.dclayer.net.network;

import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.net.address.Address;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.slot.NetworkSlot;

public abstract class ApplicationNetworkInstance extends NetworkInstance {

	private NetworkSlot networkSlot;
	
	public ApplicationNetworkInstance(HierarchicalLevel parentHierarchicalLevel, NetworkType networkType, Address address) {
		super(parentHierarchicalLevel, networkType, address);
	}
	
	public void setNetworkSlot(NetworkSlot networkSlot) {
		this.networkSlot = networkSlot;
	}
	
	@Override
	public final boolean onForward(NetworkPacket networkPacket) {
		if(networkSlot == null) return false;
		return onForward(networkPacket, networkSlot);
	}
	
	public abstract boolean onForward(NetworkPacket networkPacket, NetworkSlot networkSlot);
	
}
