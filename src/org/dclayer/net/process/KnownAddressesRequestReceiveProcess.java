package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.message.KnownAddressesRequestMessageTypeFilter;
import org.dclayer.net.process.template.PersistentS2SReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.message.KnownAddressesRequestMessage;

/**
 * a persistent service-to-service known addresses reply message receiver process
 */
public class KnownAddressesRequestReceiveProcess extends PersistentS2SReceiverProcess {
	
	public KnownAddressesRequestReceiveProcess() {
		super(new KnownAddressesRequestMessageTypeFilter());
	}

	@Override
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		Log.debug(Log.PART_PROCESS, this, String.format("received known addresses request from %s", cachedServiceAddress.getServiceAddress().toString()));
		switch(revision) {
		case 0: {
			KnownAddressesRequestMessage knownAddressesRequestMessage = (KnownAddressesRequestMessage) ((Message) revisionMessage).getMessage();
			return new KnownAddressesReplySendProcess(getAddressCache(), cachedServiceAddress, knownAddressesRequestMessage.getLimit(), knownAddressesRequestMessage.getOffset());
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

}
