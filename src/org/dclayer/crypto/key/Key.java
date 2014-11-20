package org.dclayer.crypto.key;

import org.dclayer.crypto.Crypto;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.net.Data;

public abstract class Key<T> {
	
	public static final byte RSA = 0;
	
	//
	
	protected final T key;
	private int numBits = -1;
	
	public Key(T key) throws InsufficientKeySizeException {
		this.key = key;
		
		this.numBits = computeNumBits();
		if(numBits < Crypto.KEY_MIN_NUMBITS) throw new InsufficientKeySizeException();
	}
	
	public final int getNumBits() {
		return numBits;
	}
	
	public abstract int getType();
	protected abstract int computeNumBits();
	
	public abstract int getMaxBlockNumBits();
	
	public abstract Data encrypt(Data plainData) throws CryptoException;
	public abstract Data decrypt(Data cipherData) throws CryptoException;
	
	public abstract Data toData();
	
	@Override
	public abstract String toString();
	
	public abstract boolean equals(Key<?> key);
	
}
