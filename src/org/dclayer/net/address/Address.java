package org.dclayer.net.address;

import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkTypeCollection;

/**
 * An DCL node address
 * @author Martin Exner
 */
public abstract class Address {
	
	private NetworkTypeCollection networkTypeCollection;
	
	private Data hashData = null;
	
	public Address(NetworkTypeCollection networkTypeCollection) {
		this.networkTypeCollection = networkTypeCollection;
	}
	
	public NetworkTypeCollection getNetworkTypeCollection() {
		return networkTypeCollection;
	}
	
	public final Data hashData() {
		if(hashData == null) hashData = makeHashData();
		return hashData;
	}
	
	protected abstract Data makeHashData();
	
}
