package org.dclayer.crypto.key;

import java.math.BigInteger;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.exception.crypto.InvalidCipherCryptoException;
import org.dclayer.net.Data;

public abstract class RSAKey extends Key {
	
	private RSAKeyParameters rsaKeyParameters;
	
	public RSAKey(boolean isPrivate, final BigInteger modulus, final BigInteger exponent) throws InsufficientKeySizeException {
		this(new RSAKeyParameters(isPrivate, modulus, exponent));
	}
	
	public RSAKey(RSAKeyParameters rsaKeyParameters) throws InsufficientKeySizeException {
		super(rsaKeyParameters.getModulus().bitLength());
		this.rsaKeyParameters = rsaKeyParameters;
	}

	@Override
	public int getType() {
		return Key.RSA;
	}

	@Override
	public int getMaxBlockNumBits() {
		return getNumBits();
	}
	
	@Override
	public int getBlockNumBytes() {
		return getNumBits()/8;
	}
	
	public BigInteger getModulus() {
		return rsaKeyParameters.getModulus();
	}
	
	public BigInteger getExponent() {
		return rsaKeyParameters.getExponent();
	}
	
	@Override
	public boolean equals(Key key) {
		if(!(key instanceof RSAKey)) return false;
		RSAKey rsaKey = (RSAKey) key;
		return this.getExponent().equals(rsaKey.getExponent()) && this.getModulus().equals(rsaKey.getModulus());
	}
	
	@Override
	public int hashCode() {
		return this.getExponent().hashCode() + this.getModulus().hashCode();
	}
	
	@Override
	public Data encrypt(Data plainData) throws InvalidCipherCryptoException {
		
		OAEPEncoding oaepEncoding = new OAEPEncoding(new RSAEngine());
		oaepEncoding.init(true, rsaKeyParameters);
        
		byte[] cipherBytes;
		try {
            cipherBytes = oaepEncoding.processBlock(plainData.getData(), plainData.offset(), plainData.length());
        } catch (InvalidCipherTextException e) {
        	throw new InvalidCipherCryptoException(e);
        }
        
        return new Data(cipherBytes);
        
	}
	
	@Override
	public Data decrypt(Data cipherData) throws InvalidCipherCryptoException {
		
		OAEPEncoding oaepEncoding = new OAEPEncoding(new RSAEngine());
		oaepEncoding.init(false, rsaKeyParameters);
        
		byte[] cipherBytes;
        try {
            cipherBytes = oaepEncoding.processBlock(cipherData.getData(), cipherData.offset(), cipherData.length());
        } catch (InvalidCipherTextException e) {
        	throw new InvalidCipherCryptoException(e);
        }
        
        return new Data(cipherBytes);
        
	}

	@Override
	public Data toData() {
		
		BigInteger modulus = getModulus();
		BigInteger exponent = getExponent();
		
		byte[] modulusBytes = modulus.toByteArray();
		byte[] exponentBytes = exponent.toByteArray();
		
		Data data = new Data(modulusBytes.length + exponentBytes.length);
		
		data.setBytes(0, modulusBytes, 0, modulusBytes.length);
		data.setBytes(modulusBytes.length, exponentBytes, 0, exponentBytes.length);
		
		return data;
		
	}
	
}
