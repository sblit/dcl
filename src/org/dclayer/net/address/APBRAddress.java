package org.dclayer.net.address;

import org.dclayer.crypto.key.Key;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkTypeCollection;

public class APBRAddress<T extends Key> extends Address {
	
	private KeyPair<T> keyPair;
	
	public APBRAddress(KeyPair<T> keyPair, NetworkTypeCollection networkTypeCollection) {
		super(networkTypeCollection);
		this.keyPair = keyPair;
	}
	
	public APBRAddress(KeyPair<T> keyPair) {
		super(null);
		this.keyPair = keyPair;
	}
	
	public KeyPair<T> getKeyPair() {
		return keyPair;
	}

	@Override
	protected Data makeHashData() {
		return keyPair.getPublicKey().hashData();
	}
	
}
