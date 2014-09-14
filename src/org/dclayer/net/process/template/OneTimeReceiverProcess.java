package org.dclayer.net.process.template;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.CachedServiceAddressFilter;
import org.dclayer.net.filter.MessageFilter;

/**
 * base class for one-time service-to-service message receiver {@link Process}es
 */
public abstract class OneTimeReceiverProcess extends Process {

	private CachedServiceAddressFilter cachedServiceAddressFilter;
	private MessageFilter messageFilter;
	private long timeout;

	public OneTimeReceiverProcess(CachedServiceAddress cachedServiceAddress, MessageFilter messageFilter, long timeout) {
		this.cachedServiceAddressFilter = new CachedServiceAddressFilter(cachedServiceAddress);
		this.messageFilter = messageFilter;
		this.timeout = timeout;
	}

	@Override
	public int defineProperties() {
		return S2SRECEIVER | CACHEDSERVICEADDRESSFILTER | TIMEOUT | FINALIZECALLBACK;
	}
	
	@Override
	public MessageFilter getMessageFilter() {
		return messageFilter;
	}
	
	@Override
	public abstract Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId);
	
	@Override
	public CachedServiceAddressFilter getCachedServiceAddressFilter() {
		return cachedServiceAddressFilter;
	}
	
	@Override
	public long getTimeout() {
		return timeout;
	}
	
	@Override
	public abstract Process onFinalize(boolean timeout);
	
	
}
