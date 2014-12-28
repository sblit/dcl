package org.dclayer.crypto.key;

import org.dclayer.net.Data;

public interface RemoteRSAKeyInterface {

	public int getNumBits();
	public Data encrypt(Data plainData);
	public Data decrypt(Data cipherData);
	
}
