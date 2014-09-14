package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.message.PongRedirectMessageTypeFilter;
import org.dclayer.net.process.template.PersistentS2SReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.message.PongRedirectMessage;

/**
 * a persistent service-to-service pong redirect message receiver process
 */
public class PongRedirectReceiveProcess extends PersistentS2SReceiverProcess {
	
	public PongRedirectReceiveProcess() {
		super(new PongRedirectMessageTypeFilter());
	}

	@Override
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		switch(revision) {
		case 0: {
			PongRedirectMessage message = (PongRedirectMessage) ((Message) revisionMessage).getMessage();
			Log.debug(Log.PART_PROCESS, this, String.format("received pongredirect from %s, destination %s", cachedServiceAddress.getServiceAddress().toString(), message.getServiceAddressComponent().getServiceAddress().toString()));
			CachedServiceAddress redirectCachedServiceAddress = getAddressCache().addServiceAddress(message.getServiceAddressComponent().getServiceAddress(), cachedServiceAddress, AddressCache.STATUS_REACHABLEFROMREACHABLE);
			return new PongSendProcess(message.getDataComponent().getData(), redirectCachedServiceAddress);
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

}
