package org.dclayer.net.routing;

import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkNode;

public abstract class RoutingTable {
	
	public abstract boolean add(NetworkNode networkNode);
	public abstract Nexthops lookup(Data destinationAddressData, Data originAddressData, int offset);
	
}
