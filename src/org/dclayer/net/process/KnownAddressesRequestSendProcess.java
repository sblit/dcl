package org.dclayer.net.process;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeSendProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.message.KnownAddressesRequestMessage;

/**
 * a one-time service-to-service known addresses request message send process
 */
public class KnownAddressesRequestSendProcess extends OneTimeSendProcess {
	
	private CachedServiceAddress cachedServiceAddress;
	private long limit = 32, offset = 0;
	
	public KnownAddressesRequestSendProcess(CachedServiceAddress cachedServiceAddress) {
		super(cachedServiceAddress);
		this.cachedServiceAddress = cachedServiceAddress;
	}

	@Override
	public Process onFinalize(boolean timeout) {
		getAddressCache().setStatus(getCachedServiceAddress(), AddressCache.STATUS_KNOWNADDRESSESREQUEST, AddressCache.STATUS_KNOWNADDRESSESREQUEST_SENT);
		return new KnownAddressesReplyReceiveTimeoutProcess(cachedServiceAddress, limit, offset);
	}

	@Override
	public RevisionMessage getS2SMessage(int revision) {
		switch(revision) {
		case 0:
		default: {
			return new Message(new KnownAddressesRequestMessage(limit, offset)); // TODO set proper offset & limit values
		}
		}
	}
	
}
