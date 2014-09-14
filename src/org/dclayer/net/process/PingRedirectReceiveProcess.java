package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.message.PingRedirectMessageTypeFilter;
import org.dclayer.net.process.template.PersistentS2SReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.message.PingRedirectMessage;

/**
 * a persistent service-to-service ping redirect message receiver process
 */
public class PingRedirectReceiveProcess extends PersistentS2SReceiverProcess {

	public PingRedirectReceiveProcess() {
		super(new PingRedirectMessageTypeFilter());
	}

	@Override
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		switch(revision) {
		case 0: {
			PingRedirectMessage message = (PingRedirectMessage) ((Message) revisionMessage).getMessage();
			Log.debug(Log.PART_PROCESS, this, String.format("received pingredirect from %s, destination %s", cachedServiceAddress.getServiceAddress().toString(), message.getServiceAddressComponent().getServiceAddress().toString()));
			CachedServiceAddress redirectCachedServiceAddress = getAddressCache().addServiceAddress(message.getServiceAddressComponent().getServiceAddress(), cachedServiceAddress, AddressCache.STATUS_REACHABLEFROMREACHABLE);
			return new PongRedirectSendProcess(message.getDataComponent().getData(), cachedServiceAddress, redirectCachedServiceAddress);
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

}
