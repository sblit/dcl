package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.message.PingMessageTypeFilter;
import org.dclayer.net.process.template.PersistentS2SReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.message.PingMessage;

/**
 * a persistent service-to-service ping message receiver process
 */
public class PingReceiveProcess extends PersistentS2SReceiverProcess {
	
	public PingReceiveProcess() {
		super(new PingMessageTypeFilter());
	}

	@Override
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		Log.debug(Log.PART_PROCESS, this, String.format("received ping from %s", cachedServiceAddress.getServiceAddress().toString()));
		switch(revision) {
		case 0: {
			return new PongSendProcess(((PingMessage) ((Message) revisionMessage).getMessage()).getDataComponent().getData(), cachedServiceAddress);
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

}
