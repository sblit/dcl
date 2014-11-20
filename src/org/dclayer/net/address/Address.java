package org.dclayer.net.address;

import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkInstanceCollection;

/**
 * An DCL node address
 * @author Martin Exner
 */
public abstract class Address {
	
	private NetworkInstanceCollection networkTypeCollection;
	
	private Data hashData = null;
	
	public Address(NetworkInstanceCollection networkTypeCollection) {
		this.networkTypeCollection = networkTypeCollection;
	}
	
	public NetworkInstanceCollection getNetworkInstanceCollection() {
		return networkTypeCollection;
	}
	
	public final Data hashData() {
		if(hashData == null) hashData = makeHashData();
		return hashData;
	}
	
	protected abstract Data makeHashData();
	
}
