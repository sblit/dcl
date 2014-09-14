package org.dclayer.net.process;

import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeSendProcess;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.message.S2SApplicationDataMessage;

/**
 * a one-time service-to-service application data message send process
 */
public class S2SApplicationDataSendProcess extends OneTimeSendProcess {
	
	private ApplicationIdentifier destinationApplicationIdentifier, sourceApplicationIdentifier;
	private byte[] data;
	
	public S2SApplicationDataSendProcess(CachedServiceAddress cachedServiceAddress, ApplicationIdentifier destinationApplicationIdentifier, ApplicationIdentifier sourceApplicationIdentifier, byte[] data) {
		super(cachedServiceAddress);
		this.destinationApplicationIdentifier = destinationApplicationIdentifier;
		this.sourceApplicationIdentifier = sourceApplicationIdentifier;
		this.data = data;
	}

	@Override
	public RevisionMessage getS2SMessage(int revision) {
		switch(revision) {
		case 0:
		default: {
			return new Message(new S2SApplicationDataMessage(
					new ApplicationIdentifierComponent(destinationApplicationIdentifier),
					new ApplicationIdentifierComponent(sourceApplicationIdentifier),
					new DataComponent(data)));
		}
		}
	}
	
}
