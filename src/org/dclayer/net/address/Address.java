package org.dclayer.net.address;

import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkInstanceCollection;

/**
 * A DCL node address
 * @author Martin Exner
 */
public abstract class Address {
	
	private NetworkInstanceCollection networkTypeCollection;
	
	private Data data = null;
	
	public Address(NetworkInstanceCollection networkTypeCollection) {
		this.networkTypeCollection = networkTypeCollection;
	}
	
	public NetworkInstanceCollection getNetworkInstanceCollection() {
		return networkTypeCollection;
	}
	
	public final Data toData() {
		if(data == null) data = makeData();
		return data;
	}
	
	protected abstract Data makeData();
	
	@Override
	public abstract boolean equals(Object o);
	
}
