package org.dclayer.apbr;

import java.util.LinkedList;

import org.dclayer.datastructure.tree.ParentTreeNode;
import org.dclayer.net.Data;
import org.dclayer.net.address.Address;
import org.dclayer.net.network.APBRNetworkType;
import org.dclayer.net.routing.ForwardDestination;
import org.dclayer.net.routing.Nexthops;
import org.dclayer.net.routing.RoutingTable;

public class APBRRoutingTable extends RoutingTable {
	
	public static int distance(int numParts, int partBits, Data scaledFromAddress, Data scaledToAddress) {
		int distance = 0;
		for(int i = 0; i < numParts; i++) {
			if(scaledFromAddress.getBits(i*partBits, partBits) != scaledToAddress.getBits(i*partBits, partBits)) {
				distance++;
			}
		}
		return distance;
	}
	
	//
	
	private final APBRNetworkType apbrNetworkType;
	
	private final int numParts;
	private final int partBits;
	
	private Data scaledLocalAddress;
	private ForwardDestination<?> localForwardDestination;
	
	private ParentTreeNode<Nexthops<?>> routes = new ParentTreeNode<>(0);
	private LinkedList<Data> neighbors = new LinkedList<>();
	
	public <T> APBRRoutingTable(APBRNetworkType apbrNetworkType, ForwardDestination<T> localForwardDestination) {
		this.apbrNetworkType = apbrNetworkType;
		this.numParts = apbrNetworkType.getNumParts();
		this.partBits = apbrNetworkType.getPartBits();
		this.localForwardDestination = localForwardDestination;
		this.scaledLocalAddress = apbrNetworkType.getScaledAddress();
	}
	
	private int distance(Data scaledFromAddress, Data scaledToAddress) {
		return distance(numParts, partBits, scaledFromAddress, scaledToAddress);
	}

	@Override
	public <T> boolean add(Data scaledDestinationAddress, ForwardDestination<T> forwardDestination) {
		
		int distance = distance(scaledLocalAddress, scaledDestinationAddress);
		if(distance > 1) return false;
		
		Nexthops<T> nexthops = (Nexthops<T>) routes.get(scaledDestinationAddress);
		if(nexthops == null) {
			nexthops = new Nexthops<T>(forwardDestination);
			routes.put(scaledDestinationAddress, nexthops);
			neighbors.add(scaledDestinationAddress);
		} else {
			nexthops.append(forwardDestination);
		}
		
		return true;
		
	}

	@Override
	public <T> Nexthops<T> lookup(Data scaledDestinationAddress, Data scaledOriginAddress, int offset) {
		
		Nexthops<T> nexthops = null;
		
		// local? forward to localFordwardDestination and any member that uses the same scaled address (except if scaledOriginAddress equals scaledLocalAddress)
		if(scaledLocalAddress.equals(scaledDestinationAddress)) {
			nexthops = new Nexthops<>((ForwardDestination<T>) localForwardDestination);
			// don't forward to a member with the same address if it came from one
			if(!scaledLocalAddress.equals(scaledOriginAddress)) {
				// any other members with the same scaled address as us?
				Nexthops<?> twinNexthops = routes.get(scaledLocalAddress);
				if(twinNexthops != null) {
					nexthops.append((Nexthops<T>) twinNexthops);
				}
			}
			return nexthops;
		}
		
		// else: remote, getting one hop closer
		
		Data scaledNexthopAddress = scaledLocalAddress.copy();
		
		for(int i = offset; i < numParts; i++) {
			long localPart = scaledNexthopAddress.getBits(i*partBits, partBits);
			long destinationPart = scaledDestinationAddress.getBits(i*partBits, partBits);
			if(localPart != destinationPart) {
				
				scaledNexthopAddress.setBits(i*partBits, partBits, destinationPart);

				Nexthops<?> remoteNexthops = routes.get(scaledNexthopAddress);
				if(remoteNexthops != null) {
					nexthops = new Nexthops<>((ForwardDestination<T>) remoteNexthops.getForwardDestination());
					return nexthops;
				}
				
				scaledNexthopAddress.setBits(i*partBits, partBits, localPart);
				
			}
		}
		
		return null;
		
	}
	
	
	
}
