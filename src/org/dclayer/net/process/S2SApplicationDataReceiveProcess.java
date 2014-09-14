package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.message.S2SApplicationDataMessageFilter;
import org.dclayer.net.process.template.PersistentS2SReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.message.S2SApplicationDataMessage;

/**
 * a persistent service-to-service application data message receiver process
 */
public class S2SApplicationDataReceiveProcess extends PersistentS2SReceiverProcess {
	
	private ApplicationIdentifier applicationIdentifier;
	private ApplicationConnection applicationConnection;
	
	public S2SApplicationDataReceiveProcess(ApplicationConnection applicationConnection, ApplicationIdentifier applicationIdentifier) {
		super(new S2SApplicationDataMessageFilter());
		this.applicationIdentifier = applicationIdentifier;
		this.applicationConnection = applicationConnection;
	}

	@Override
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		switch(revision) {
		case 0: {
			S2SApplicationDataMessage applicationDataMessage = (S2SApplicationDataMessage) ((Message) revisionMessage).getMessage();
			ApplicationIdentifier destinationApplicationIdentifier = applicationDataMessage.getDestinationApplicationIdentifierComponent().getApplicationIdentifier();
			ApplicationIdentifier sourceApplicationIdentifier = applicationDataMessage.getSourceApplicationIdentifierComponent().getApplicationIdentifier();
			byte[] data = applicationDataMessage.getDataComponent().getData();
			boolean match = this.applicationIdentifier.equals(applicationDataMessage.getDestinationApplicationIdentifierComponent().getApplicationIdentifier());
			Log.debug(Log.PART_PROCESS, this,
					String.format("received application data message from %s: destapplication=%s, srcapplication=%s; match=%s",
							cachedServiceAddress.toString(),
							destinationApplicationIdentifier.toString(),
							sourceApplicationIdentifier.toString(),
							match));
			if(match) {
				return new A2SDataSendProcess(applicationConnection, destinationApplicationIdentifier, sourceApplicationIdentifier, cachedServiceAddress, data);
			} else {
				return null;
			}
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

}
