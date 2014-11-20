package org.dclayer.net.circle;

import java.util.LinkedList;

import org.dclayer.datastructure.tree.ParentTreeNode;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.routing.ForwardDestination;
import org.dclayer.net.network.routing.Nexthops;
import org.dclayer.net.network.routing.RoutingTable;

public class CircleRoutingTable extends RoutingTable implements HierarchicalLevel {
	
	private CircleNetworkType circleNetworkType;
	
	private NetworkInstance localNetworkInstance;

	private ParentTreeNode<Nexthops> ownRoutes = new ParentTreeNode<>(0);
	private LinkedList<NetworkNode> neighborNetworkNodes = new LinkedList<>();
	
	private ParentTreeNode<Nexthops> routes = ownRoutes;
	private LinkedList<NetworkNode> connectedTableNeighbors = null;
	private CircleRoutingTable connectedCircleRoutingTable = null;
	
	public <T extends NetworkPacket> CircleRoutingTable(CircleNetworkType circleNetworkType, NetworkInstance networkInstance) {
		this.circleNetworkType = circleNetworkType;
		this.localNetworkInstance = networkInstance;
		this.add(networkInstance);
	}

	@Override
	public boolean add(NetworkNode networkNode) {
		
		Data scaledDestinationAddress = networkNode.getScaledAddress();
		
		Nexthops nexthops = routes.get(scaledDestinationAddress);
		if(nexthops == null) {
			nexthops = new Nexthops(networkNode);
			routes.put(scaledDestinationAddress, nexthops);
		} else {
			for(ForwardDestination forwardDestination : nexthops) {
				if(networkNode.getAddress().equals(forwardDestination.getAddress())) {
					return false;
				}
			}
			nexthops.append(networkNode);
		}
		neighborNetworkNodes.add(networkNode);
		
		return true;
		
	}

	@Override
	public boolean remove(NetworkNode networkNode) {
		
		Data scaledDestinationAddress = networkNode.getScaledAddress();
		
		Nexthops nexthops = routes.get(scaledDestinationAddress);
		if(nexthops == null) {
			return false;
		}

		boolean success = false;
		Nexthops lastNexthops = null;
		
		do {
			
			ForwardDestination forwardDestination = nexthops.getForwardDestination();
			if(networkNode.getAddress().equals(forwardDestination.getAddress())) {
				
				if(lastNexthops == null) {
					// the first Nexthops element is to be removed
					
					if(nexthops.getNext() != null) {
						// there are others with the same scaled address, update the reference in the tree
						routes.put(scaledDestinationAddress, nexthops.getNext());
						
					} else {
						// this is the only one, remove the reference from the tree
						routes.remove(scaledDestinationAddress);
						
					}
					
				} else {
					
					// the Nexthops element to be removed is not the first, no need to update the reference in the tree
					lastNexthops.setNext(nexthops.getNext());
					
				}
				
				nexthops = lastNexthops; // don't let lastNexthops be nexthops the next iteration
				success = true;
				
			}
			
			lastNexthops = nexthops;
			nexthops = nexthops.getNext();
			
		} while(nexthops != null);
		
		return success;
		
	}

	@Override
	public Nexthops lookup(Data scaledDestinationAddress, Data scaledOriginAddress, int offset) {
		
		Nexthops nexthops = routes.getClosest(scaledDestinationAddress);
		
		if(nexthops == null) {
			return null;
		}
		
		if(scaledOriginAddress.equals(nexthops.getForwardDestination().getScaledAddress())) {
			// do not forward to the scaled address we just got this from
			return null;
		}
		
		if(localNetworkInstance.getScaledAddress().equals(nexthops.getForwardDestination().getScaledAddress())) {
			// we do not forward to ourselves, however, we do forward to other nodes who use the same scaled address.
			// we now need to forward to everybody in nexthops except ourselves, so we just pop off ourselves by
			// simply removing the first element (we're always the first because the local network instance is
			// the first to be added to the tree)
			nexthops = nexthops.getNext();
		}
		
		if(nexthops == null) {
			return null;
		}
		
		return nexthops;
		
	}

	@Override
	public NetworkType getNetworkType() {
		return circleNetworkType;
	}

	@Override
	public void connect(RoutingTable routingTable) {
		
		Log.debug(this, "connecting with %s", routingTable);
		
		if(connectedTableNeighbors != null) {
			Log.debug(this, "disconnecting first");
			disconnect();
		}
		
		connectedCircleRoutingTable = (CircleRoutingTable) routingTable;
		
		connectedTableNeighbors = new LinkedList<>();
		this.routes = connectedCircleRoutingTable.routes;
		
		for(NetworkNode networkNode : neighborNetworkNodes) {
			if(connectedCircleRoutingTable.add(networkNode)) {
				Log.debug(this, "copied NetworkNode: %s", networkNode);
				connectedTableNeighbors.add(networkNode);
			} else {
				Log.debug(this, "NetworkNode node copied: %s", networkNode);
			}
		}
		
	}

	@Override
	public void disconnect() {
		
		for(NetworkNode networkNode : connectedTableNeighbors) {
			Log.debug(this, "removing NetworkNode from connected table: %s", networkNode);
			connectedCircleRoutingTable.remove(networkNode);
		}
		
		connectedTableNeighbors = null;
		connectedCircleRoutingTable = null;
		
	}

	@Override
	public String toString() {
		return "CircleRoutingTable";
	}

	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		return localNetworkInstance;
	}
	
}
