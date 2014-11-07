package org.dclayer.crypto.key;

public class KeyPair<T extends Key> {
	
	public static <T extends Key> KeyPair<T> fromPublicKey(T publicKey) {
		return new KeyPair<T>(publicKey, null);
	}
	
	public static <T extends Key> KeyPair<T> fromPrivateKey(T privateKey) {
		return new KeyPair<T>(null, privateKey);
	}
	
	public static <T extends Key> KeyPair<T> fromKeys(T publicKey, T privateKey) {
		return new KeyPair<T>(publicKey, privateKey);
	}
	
	//

	private T publicKey;
	private T privateKey;
	
	private KeyPair(T publicKey, T privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public T getPublicKey() {
		return publicKey;
	}

	public T getPrivateKey() {
		return privateKey;
	}
	
}
