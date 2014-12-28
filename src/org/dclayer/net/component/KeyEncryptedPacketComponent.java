package org.dclayer.net.component;

import org.dclayer.crypto.key.Key;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.CryptoParseException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.misc.Toolbox;
import org.dclayer.net.Data;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;

public abstract class KeyEncryptedPacketComponent extends PacketComponent {

	private KeyComponent keyComponent = new KeyComponent();
	
	private Key publicKey;
	private Key privateKey;
	
	private Data ownCipherData = new Data();
	private Data cipherData = ownCipherData;
	
	private Data ownPlainData = new Data();
	private Data plainData = ownPlainData;
	
	private DataByteBuf dataByteBuf = new DataByteBuf();
	
	@Override
	public final void read(ByteBuf byteBuf) throws ParseException, BufException {
		
		keyComponent.read(byteBuf);

		int numBytes;
		try {
			publicKey = keyComponent.getKeyComponent().getKey();
			numBytes = publicKey.getBlockNumBytes();
		} catch (CryptoException e) {
			throw new CryptoParseException(e);
		}
		
		privateKey = null;
		
		(cipherData = ownCipherData).prepare(numBytes);
		byteBuf.read(cipherData);
		
		try {
			plainData = publicKey.decrypt(cipherData);
		} catch (CryptoException e) {
			throw new CryptoParseException(e);
		}
		
		dataByteBuf.setData(plainData);
		
		readPlain(dataByteBuf);
		
	}

	@Override
	public final void write(ByteBuf byteBuf) throws BufException {
		
		plainData.prepare(plainLength());
		dataByteBuf.setData(plainData);
		writePlain(dataByteBuf);
		
		keyComponent.write(byteBuf);
		
		try {
			cipherData = privateKey.encrypt(plainData);
		} catch (CryptoException e) {
			throw new BufException(e);
		}
		
		byteBuf.write(cipherData);
		
	}

	@Override
	public final int length() {
		return keyComponent.length() + privateKey.getBlockNumBytes();
	}

	@Override
	public final PacketComponent[] getChildren() {
		return Toolbox.append(new PacketComponent[] { keyComponent }, getPlainChildren());
	}

	@Override
	public final String toString() {
		return String.format("KeyEncryptedPacketComponent(len=%d)/%s", length(), plainToString());
	}
	
	public final void setKeyPair(KeyPair keyPair) {
		setKeyPair(keyPair.getPublicKey(), keyPair.getPrivateKey());
	}
	
	public final void setKeyPair(Key publicKey, Key privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.keyComponent.setKey(publicKey);
	}
	
	public final Key getPrivateKey() {
		return privateKey;
	}
	
	public final Key getPublicKey() {
		return publicKey;
	}
	
	public abstract void readPlain(ByteBuf byteBuf) throws ParseException, BufException;
	public abstract void writePlain(ByteBuf byteBuf) throws BufException;
	public abstract int plainLength();
	public abstract PacketComponent[] getPlainChildren();
	public abstract String plainToString();
	
}
