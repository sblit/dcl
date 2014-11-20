package org.dclayer.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.crypto.key.RSAKey;
import org.dclayer.crypto.key.RSAPrivateKey;
import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.net.Data;

public class Crypto {
	
	public static final int KEY_MIN_NUMBITS = 2048;
	
	public static final int RSA_ADDRESS_KEY_NUMBITS = 2048;
	public static final int RSA_ADDRESS_KEY_CERTAINTY = 160;
	
	private static KeyPair generateRSAKeyPair(int numBits, int certainty) {
		
		RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
		RSAKeyGenerationParameters rsaKeyGenerationParameters = new RSAKeyGenerationParameters(
				BigInteger.valueOf(0x10001),
				new SecureRandom(),
				Math.max(numBits, KEY_MIN_NUMBITS),
				certainty);

		rsaKeyPairGenerator.init(rsaKeyGenerationParameters);

		AsymmetricCipherKeyPair pair = rsaKeyPairGenerator.generateKeyPair();

		RSAKeyParameters publicKeyParameters = (RSAKeyParameters) pair.getPublic();
		RSAKeyParameters privateKeyParameters = (RSAKeyParameters) pair.getPrivate();

		RSAKey publicKey, privateKey;
		try {
			publicKey = new RSAPublicKey(publicKeyParameters);
			privateKey = new RSAPrivateKey(privateKeyParameters);
		} catch(InsufficientKeySizeException e) {
			// this can not happen (see constructor arguments for new RSAKeyGenerationParameters)
			throw new RuntimeException(e);
		}

		return KeyPair.fromKeys(publicKey, privateKey);
		
	}
	
	public static KeyPair generateAPBRAddressRSAKeyPair() {
		return generateAddressRSAKeyPair();
	}
	
	public static KeyPair generateAddressRSAKeyPair() {
		
		return generateRSAKeyPair(RSA_ADDRESS_KEY_NUMBITS, RSA_ADDRESS_KEY_CERTAINTY);
		
	}
	
	public static Data sha1(Data... inputDatas) {
		
		Digest digest = new SHA1Digest();
		Data outputData = new Data(digest.getDigestSize());
		
		for(Data inputData : inputDatas) {
			digest.update(inputData.getData(), inputData.offset(), inputData.length());
		}
		
		digest.doFinal(outputData.getData(), 0);
		
		return outputData;
		
	}
	
}
