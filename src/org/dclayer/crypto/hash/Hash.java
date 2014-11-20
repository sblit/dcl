package org.dclayer.crypto.hash;

import org.dclayer.net.Data;

public abstract class Hash {
	
	public static final String SHA1 = "sha1";
	
	//
	
	HashAlgorithm hashAlgorithm;
	
	public Hash(HashAlgorithm hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	
	public HashAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}
	
	public void update(Data... datas) {
		for(Data data : datas) {
			update(data);
		}
	}
	
	public Data finish() {
		Data outputData = new Data(getDigestSize());
		finish(outputData);
		return outputData;
	}
	
	public void finish(Data outputData) {
		finish(outputData, 0);
	}
	
	public abstract int getDigestSize();
	public abstract void update(Data data);
	public abstract void finish(Data outputData, int offset);
	
}
