package org.dclayer.net.network.component;

import org.dclayer.net.Data;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.network.slot.NetworkSlot;

/**
 * Base class for all routed Packets (e.g. APBRPacket)
 * @author Martin Exner
 */
public abstract class NetworkPacket extends PacketComponent {
	
	private final NetworkSlot networkSlot;
	
	public NetworkPacket(NetworkSlot networkSlot) {
		this.networkSlot = networkSlot;
	}
	
	public final NetworkSlot getNetworkSlot() {
		return networkSlot;
	}
	
	public abstract Data getDestinationAddressData();
	public abstract void setDestinationAddressData(Data destinationAddressData);
	public abstract DataComponent getDataComponent();
	
}
