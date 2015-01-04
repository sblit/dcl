package org.dclayer.application.applicationchannel;

import java.io.BufferedOutputStream;
import java.io.InputStream;

import org.dclayer.application.applicationchannelslotmap.ApplicationChannelSlot;
import org.dclayer.crypto.Crypto;
import org.dclayer.crypto.key.Key;
import org.dclayer.net.Data;
import org.dclayer.net.applicationchannel.ApplicationChannelTarget;

public abstract class AbsApplicationChannel implements ApplicationChannel {
	
	private ApplicationChannelSlot applicationChannelSlot;
	private ApplicationChannelActionListener applicationChannelActionListener;
	
	private boolean locallyInitiated;
	
	private Data remotePublicKeyFingerprint;
	private ApplicationChannelTarget applicationChannelTarget;
	
	public AbsApplicationChannel(ApplicationChannelTarget applicationChannelTarget, ApplicationChannelActionListener applicationChannelActionListener, boolean locallyInitiated) {
		this.applicationChannelTarget = applicationChannelTarget;
		this.applicationChannelActionListener = applicationChannelActionListener;
		this.locallyInitiated = locallyInitiated;
		this.remotePublicKeyFingerprint = Crypto.sha1(getRemotePublicKey().toData());
	}
	
	public void setApplicationChannelSlot(ApplicationChannelSlot applicationChannelSlot) {
		this.applicationChannelSlot = applicationChannelSlot;
	}
	
	public ApplicationChannelSlot getApplicationChannelSlot() {
		return applicationChannelSlot;
	}
	
	public void setApplicationChannelActionListener(ApplicationChannelActionListener applicationChannelActionListener) {
		this.applicationChannelActionListener = applicationChannelActionListener;
	}
	
	public ApplicationChannelActionListener getApplicationChannelActionListener() {
		return applicationChannelActionListener;
	}
	
	@Override
	public boolean wasInitiatedLocally() {
		return locallyInitiated;
	}

	@Override
	public ApplicationChannelTarget getApplicationChannelTarget() {
		return applicationChannelTarget;
	}

	@Override
	public Key getRemotePublicKey() {
		return applicationChannelTarget.getRemoteAddress().getKeyPair().getPublicKey();
	}

	@Override
	public String getActionIdentifier() {
		return applicationChannelTarget.getActionIdentifier();
	}

	@Override
	public abstract InputStream getInputStream();
	@Override
	public abstract BufferedOutputStream getOutputStream();
	
	protected abstract String getName();
	
	@Override
	public final String toString() {
		return String.format("%s (actionIdentifier=%s, remote public key fingerprint %s)", getName(), getActionIdentifier(), remotePublicKeyFingerprint);
	}

}
