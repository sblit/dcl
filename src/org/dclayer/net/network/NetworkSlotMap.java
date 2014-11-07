package org.dclayer.net.network;

import java.util.ArrayList;

import org.dclayer.net.address.Address;

public class NetworkSlotMap {
	
	private ArrayList<NetworkSlot> networkSlots = new ArrayList<>();
	
	/**
	 * assigns the given {@link NetworkType} to an unoccupied slot, returning the slot it was assigned to
	 * @param networkType the {@link NetworkType} to assign
	 * @return the slot the given {@link NetworkType} was assigned to
	 */
	public int add(NetworkType networkType) {
		int slot = 0;
		for(NetworkSlot networkSlot : networkSlots) {
			if(networkSlot == null) {
				networkSlots.set(slot, new NetworkSlot(networkType));
				return slot;
			}
			slot++;
		}
		networkSlots.add(new NetworkSlot(networkType));
		return slot;
	}
	
	/**
	 * assigns the given {@link NetworkType} and {@link Address} to the given slot, returning the newly created {@link NetworkSlot} object
	 * @param slot the slot to assign the {@link NetworkType} to
	 * @param networkType the {@link NetworkType} to assign to the given slot
	 * @param address the {@link Address} to assign to the given slot
	 * @return the newly created {@link NetworkSlot} object
	 */
	public NetworkSlot put(int slot, NetworkType networkType, Address address) {
		NetworkSlot networkSlot = new NetworkSlot(networkType, address);
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
