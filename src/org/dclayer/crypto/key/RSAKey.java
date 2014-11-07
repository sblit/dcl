package org.dclayer.crypto.key;

import java.math.BigInteger;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.dclayer.crypto.Crypto;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.exception.crypto.InvalidCipherCryptoException;
import org.dclayer.net.Data;

public abstract class RSAKey extends Key<RSAKeyParameters> {
	
	public RSAKey(boolean isPrivate, final BigInteger modulus, final BigInteger exponent) throws InsufficientKeySizeException {
		super(new RSAKeyParameters(isPrivate, modulus, exponent));
	}
	
	public RSAKey(RSAKeyParameters rsaKeyParameters) throws InsufficientKeySizeException {
		super(rsaKeyParameters);
	}

	@Override
	public int getType() {
		return Key.RSA;
	}

	@Override
	protected int computeNumBits() {
		return key.getModulus().bitLength();
	}

	@Override
	public int getMaxBlockNumBits() {
		return getNumBits();
	}
	
	public BigInteger getModulus() {
		return key.getModulus();
	}
	
	public BigInteger getExponent() {
		return key.getExponent();
	}
	
	@Override
	public Data encrypt(Data plainData) throws InvalidCipherCryptoException {
		
		OAEPEncoding oaepEncoding = new OAEPEncoding(new RSAEngine());
		oaepEncoding.init(true, key);
        
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
		oaepEncoding.init(false, key);
        
		byte[] cipherBytes;
        try {
            cipherBytes = oaepEncoding.processBlock(cipherData.getData(), cipherData.offset(), cipherData.length());
        } catch (InvalidCipherTextException e) {
        	throw new InvalidCipherCryptoException(e);
        }
        
        return new Data(cipherBytes);
        
	}

	@Override
	public Data hashData() {
		
		BigInteger modulus = getModulus();
		BigInteger exponent = getExponent();
		
		Data modulusData = new Data(modulus.toByteArray());
		Data exponentData = new Data(exponent.toByteArray());
		
		return Crypto.sha1(modulusData, exponentData);
		
	}
	
}
