package org.dclayer.net.address;

import org.dclayer.crypto.key.Key;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.net.Data;
import org.dclayer.net.network.NetworkInstanceCollection;

public class AsymmetricKeyPairAddress<T extends Key> extends Address {
	
	private KeyPair<T> keyPair;
	
	public AsymmetricKeyPairAddress(KeyPair<T> keyPair, NetworkInstanceCollection networkTypeCollection) {
		super(networkTypeCollection);
		this.keyPair = keyPair;
	}
	
	public AsymmetricKeyPairAddress(KeyPair<T> keyPair) {
		super(null);
		this.keyPair = keyPair;
	}
	
	public KeyPair<T> getKeyPair() {
		return keyPair;
	}

	@Override
	protected Data makeData() {
		return keyPair.getPublicKey().toData();
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof AsymmetricKeyPairAddress)) return false;
		return keyPair.equals(((AsymmetricKeyPairAddress)o).keyPair);
	}
	
}
