package org.dclayer.net.network.slot;

import java.util.ArrayList;

import org.dclayer.net.address.Address;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkType;

public class NetworkSlotMap {
	
	private ArrayList<NetworkSlot> networkSlots = new ArrayList<>();
	
	/**
	 * assigns the given {@link NetworkInstance} to an unoccupied slot, returning the slot it was assigned to
	 * @param networkNode the {@link NetworkNode} to assign
	 * @return the slot object the given {@link NetworkInstance} was assigned to
	 */
	public NetworkSlot add(NetworkNode networkNode) {
		int slot = 0;
		NetworkSlot newNetworkSlot;
		for(NetworkSlot networkSlot : networkSlots) {
			if(networkSlot == null) {
				networkSlots.set(slot, newNetworkSlot = new NetworkSlot(networkNode, slot));
				return newNetworkSlot;
			}
			slot++;
		}
		networkSlots.add(newNetworkSlot = new NetworkSlot(networkNode, slot));
		return newNetworkSlot;
	}
	
	/**
	 * assigns the given {@link NetworkNode} and {@link Address} to the given slot, returning the newly created {@link NetworkSlot} object
	 * @param slot the slot to assign the {@link NetworkNode} to
	 * @param networkNode the {@link NetworkNode} to assign to the given slot
	 * @param address the {@link Address} to assign to the given slot
	 * @return the newly created {@link NetworkSlot} object
	 */
	public NetworkSlot put(int slot, NetworkNode networkNode) {
		NetworkSlot networkSlot = new NetworkSlot(networkNode, slot);
		if(networkSlots.size() > slot) {
			networkSlots.set(slot, networkSlot);
		} else {
			while(networkSlots.size() < slot) {
				networkSlots.add(null);
			}
			networkSlots.add(networkSlot);
		}
		return networkSlot;
	}
	
	/**
	 * returns the {@link NetworkSlot} assigned to the given slot or null if the given slot is unoccupied
	 * @param slot the slot of which to return the assigned {@link NetworkSlot}
	 * @return the {@link NetworkSlot} assigned or null if the slot is unoccupied
	 */
	public NetworkSlot get(int slot) {
		return networkSlots.size() > slot ? networkSlots.get(slot) : null;
	}
	
	/**
	 * tries to find a {@link NetworkSlot} that has the given {@link NetworkInstance} assigned
	 * @param networkType the {@link NetworkType} to search for
	 * @return a {@link NetworkSlot} that has the given {@link NetworkType} assigned or null if none
	 */
	public NetworkSlot find(NetworkType networkType) {
		for(NetworkSlot networkSlot : networkSlots) {
			if(networkSlot.getNetworkNode().getNetworkType().equals(networkType)) return networkSlot;
		}
		return null;
	}
	
	/**
	 * clears the given slot, returning the {@link NetworkSlot} that was assigned before or null if the slot was already unoccupied
	 * @param slot the slot to clear
	 * @return the {@link NetworkSlot} that was assigned before or null if the slot was already unoccupied
	 */
	public NetworkSlot remove(int slot) {
		if(networkSlots.size() <= slot) return null;
		NetworkSlot networkSlot = networkSlots.get(slot);
		if(networkSlot != null) {
			networkSlots.set(slot, null);
			return networkSlot;
		}
		return null;
	}
	
}
