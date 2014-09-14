package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.KnownAddressesRequestMessage;
import org.dclayer.net.filter.message.A2SKnownAddressesRequestMessageTypeFilter;
import org.dclayer.net.process.template.PersistentA2SReceiverProcess;
import org.dclayer.net.process.template.Process;

/**
 * a persistent application-to-service known addresses request message receiver process
 */
public class A2SKnownAddressesRequestReceiveProcess extends PersistentA2SReceiverProcess {
	
	public A2SKnownAddressesRequestReceiveProcess() {
		super(new A2SKnownAddressesRequestMessageTypeFilter());
	}

	@Override
	public Process receiveA2S(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		Log.debug(Log.PART_PROCESS, this, String.format("received known addresses request from %s", applicationConnection.toString()));
		switch(revision) {
		case 0: {
			KnownAddressesRequestMessage knownAddressesRequestMessage = (KnownAddressesRequestMessage) ((Message) revisionMessage).getMessage();
			return new A2SKnownAddressesReplySendProcess(getAddressCache(), applicationConnection, knownAddressesRequestMessage.getLimit(), knownAddressesRequestMessage.getOffset());
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

}
