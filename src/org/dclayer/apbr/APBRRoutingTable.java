package org.dclayer.apbr;

import java.util.LinkedList;

import org.dclayer.datastructure.tree.ParentTreeNode;
import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.component.NetworkPacket;
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
	
	private NetworkInstance localNetworkInstance;
	
	private ParentTreeNode<Nexthops> routes = new ParentTreeNode<>(0);
	private LinkedList<Data> neighbors = new LinkedList<>();
	
	public <T extends NetworkPacket> APBRRoutingTable(APBRNetworkType apbrNetworkType, NetworkInstance networkInstance) {
		this.apbrNetworkType = apbrNetworkType;
		this.numParts = apbrNetworkType.getNumParts();
		this.partBits = apbrNetworkType.getPartBits();
		this.localNetworkInstance = networkInstance;
	}
	
	private int distance(Data scaledFromAddress, Data scaledToAddress) {
		return distance(numParts, partBits, scaledFromAddress, scaledToAddress);
	}

	@Override
	public boolean add(NetworkNode networkNode) {
		
		Data scaledDestinationAddress = networkNode.getScaledAddress();
		
		int distance = distance(localNetworkInstance.getScaledAddress(), scaledDestinationAddress);
		if(distance > 1) return false;
		
		Nexthops nexthops = routes.get(scaledDestinationAddress);
		if(nexthops == null) {
			nexthops = new Nexthops(networkNode);
			routes.put(scaledDestinationAddress, nexthops);
			neighbors.add(scaledDestinationAddress);
		} else {
			nexthops.append(networkNode);
		}
		
		return true;
		
	}

	@Override
	public Nexthops lookup(Data scaledDestinationAddress, Data scaledOriginAddress, int offset) {
		
		Nexthops nexthops = null;
		
		// local? forward to localFordwardDestination and any member that uses the same scaled address (except if scaledOriginAddress equals scaledLocalAddress)
		if(localNetworkInstance.getScaledAddress().equals(scaledDestinationAddress)) {
			nexthops = new Nexthops(localNetworkInstance);
			// don't forward to a member with the same address if it came from one
			if(!localNetworkInstance.getScaledAddress().equals(scaledOriginAddress)) {
				// any other members with the same scaled address as us?
				Nexthops twinNexthops = routes.get(localNetworkInstance.getScaledAddress());
				if(twinNexthops != null) {
					nexthops.append(twinNexthops);
				}
			}
			return nexthops;
		}
		
		// else: remote, getting one hop closer
		
		Data scaledNexthopAddress = localNetworkInstance.getScaledAddress().copy();
		
		for(int i = offset; i < numParts; i++) {
			long localPart = scaledNexthopAddress.getBits(i*partBits, partBits);
			long destinationPart = scaledDestinationAddress.getBits(i*partBits, partBits);
			if(localPart != destinationPart) {
				
				scaledNexthopAddress.setBits(i*partBits, partBits, destinationPart);

				Nexthops remoteNexthops = routes.get(scaledNexthopAddress);
				if(remoteNexthops != null) {
					nexthops = new Nexthops(remoteNexthops.getForwardDestination());
					return nexthops;
				}
				
				scaledNexthopAddress.setBits(i*partBits, partBits, localPart);
				
			}
		}
		
		return null;
		
	}
	
}
