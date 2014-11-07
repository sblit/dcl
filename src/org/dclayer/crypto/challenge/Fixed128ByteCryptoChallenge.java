package org.dclayer.crypto.challenge;

import java.util.Random;

import org.dclayer.crypto.key.Key;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.net.Data;

public class Fixed128ByteCryptoChallenge extends CryptoChallenge {
	
	// leave space for both random padding of plain text to encrypt
	// and for OAEP padding
	public static final int CHALLENGE_DATA_MAXNUMBYTES = 64;
	public static final int PLAINDATA_PADDING = 64;
	
	//
	
	private Data plainData;
	
	public Fixed128ByteCryptoChallenge(Key key) {
		super(key);
	}

	// crypto challenge resolution
	
	public Data solveCryptoChallenge(Data plainData) throws CryptoException {
		Data cryptData = new Data(plainData.length() + PLAINDATA_PADDING);
		byte[] padding = new byte[PLAINDATA_PADDING];
		(new Random()).nextBytes(padding); // TODO is a simple random padding sufficient?
		cryptData.setBytes(0, padding, 0, padding.length);
		cryptData.setBytes(padding.length, plainData.getData(), plainData.offset(), plainData.length());
		return key.encrypt(cryptData);
	}

	// crypto challenge verification

	public Data makeRandomPlainData() {
		byte[] bytes = new byte[CHALLENGE_DATA_MAXNUMBYTES];
		(new Random()).nextBytes(bytes);
		this.plainData = new Data(bytes);
		return plainData;
	}

	public boolean verifyCipherData(Data cipherData) throws CryptoException {
		Data plainData = key.decrypt(cipherData);
		return (plainData.length() == (CHALLENGE_DATA_MAXNUMBYTES+PLAINDATA_PADDING))
				&& this.plainData.equals(0, plainData, PLAINDATA_PADDING, CHALLENGE_DATA_MAXNUMBYTES);
	}
	
}
