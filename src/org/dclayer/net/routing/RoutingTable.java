package org.dclayer.net.routing;

import org.dclayer.net.Data;

public abstract class RoutingTable {
	
	public abstract <T> boolean add(Data destinationAddressData, ForwardDestination<T> forwardDestination);
	public abstract <T> Nexthops<T> lookup(Data destinationAddressData, Data originAddressData, int offset);
	
}
