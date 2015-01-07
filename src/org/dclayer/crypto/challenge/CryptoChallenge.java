package org.dclayer.crypto.challenge;

import org.dclayer.crypto.key.Key;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.net.Data;

public abstract class CryptoChallenge {
	
	protected Key key;
	
	public CryptoChallenge(Key key) {
		this.key = key;
	}
	
	public Key getKey() {
		return key;
	}

	public abstract Data makeChallengeData();
	public abstract boolean verifySolvedData(Data solvedData) throws CryptoException;
	
	public abstract Data solveChallengeData(Data challengeData) throws CryptoException;
	
}
