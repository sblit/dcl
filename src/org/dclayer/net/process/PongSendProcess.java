package org.dclayer.net.process;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeSendProcess;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.message.PongMessage;

/**
 * a one-time service-to-service pong message send process
 */
public class PongSendProcess extends OneTimeSendProcess {
	
	private byte[] data;
	
	public PongSendProcess(byte[] data, CachedServiceAddress cachedServiceAddress) {
		super(cachedServiceAddress);
		this.data = data;
	}

	@Override
	public RevisionMessage getS2SMessage(int revision) {
		switch(revision) {
		case 0:
		default: {
			return new Message(new PongMessage(new DataComponent(data)));
		}
		}
	}
	
}
