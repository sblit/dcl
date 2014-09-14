package org.dclayer.net.process.template;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;

/**
 * base class for one-time service-to-service message send {@link Process}es
 */
public abstract class OneTimeSendProcess extends Process {
	
	/**
	 * the destination {@link CachedServiceAddress} of this {@link OneTimeSendProcess}
	 */
	private CachedServiceAddress cachedServiceAddress;
	
	public OneTimeSendProcess(CachedServiceAddress cachedServiceAddress) {
		this.cachedServiceAddress = cachedServiceAddress;
	}

	@Override
	protected int defineProperties() {
		return S2SMESSAGE | FINALIZECALLBACK;
	}
	
	@Override
	public CachedServiceAddress getCachedServiceAddress() {
		return cachedServiceAddress;
	}
	
	@Override
	public abstract RevisionMessage getS2SMessage(int revision);
	
	@Override
	public Process onFinalize(boolean timeout) {
		return null;
	}

}
