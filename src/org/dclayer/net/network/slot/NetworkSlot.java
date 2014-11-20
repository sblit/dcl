package org.dclayer.net.network.slot;

import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.component.NetworkPacket;


public class NetworkSlot {
	
	private NetworkNode networkNode;
	private NetworkPacket networkPacket;
	private int slot;
	
	private NetworkSlot remoteEquivalent;
	
	public NetworkSlot(NetworkNode networkNode, int slot) {
		this.networkNode = networkNode;
		this.slot = slot;
		this.networkPacket = networkNode.getNetworkType().makeNetworkPacket(this);
	}
	
	public NetworkPacket getNetworkPacket() {
		return networkPacket;
	}
	
	public NetworkNode getNetworkNode() {
		return networkNode;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public void setRemoteEquivalent(NetworkSlot remoteEquivalent) {
		this.remoteEquivalent = remoteEquivalent;
	}
	
	public NetworkSlot getRemoteEquivalent() {
		return remoteEquivalent;
	}
	
	@Override
	public String toString() {
		return String.format("slot %d (%s, %s remote equivalent)", slot, networkNode, remoteEquivalent == null ? "no" : "has");
	}
	
}
