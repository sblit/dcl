package org.dclayer.net.address;

import org.dclayer.crypto.key.KeyPair;
import org.dclayer.crypto.key.RSAKey;
import org.dclayer.net.network.NetworkInstanceCollection;

public class APBRAddress extends AsymmetricKeyPairAddress<RSAKey> {
	
	public APBRAddress(KeyPair keyPair, NetworkInstanceCollection networkTypeCollection) {
		super(keyPair, networkTypeCollection);
	}
	
	public APBRAddress(KeyPair keyPair) {
		this(keyPair, null);
	}
	
}
