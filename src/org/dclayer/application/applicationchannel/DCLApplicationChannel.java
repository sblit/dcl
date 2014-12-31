package org.dclayer.application.applicationchannel;

import java.io.BufferedOutputStream;
import java.io.InputStream;

import org.dclayer.application.networktypeslotmap.NetworkEndpointSlot;
import org.dclayer.crypto.Crypto;
import org.dclayer.crypto.key.Key;
import org.dclayer.net.Data;

public class DCLApplicationChannel implements ApplicationChannel {
	
	private Data remotePublicKeyFingerprint;
	
	public DCLApplicationChannel(Key remotePublicKey, String actionIdentifier, NetworkEndpointSlot networkEndpointSlot) {
		this.remotePublicKeyFingerprint = Crypto.sha1(remotePublicKey.toData());
	}

	@Override
	public Key getRemotePublicKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getActionIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedOutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("DCL application channel (remote public key fingerprint %s)", remotePublicKeyFingerprint);
	}

}
