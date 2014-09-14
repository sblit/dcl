package org.dclayer.net.process;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeSendProcess;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;
import org.dclayer.net.s2s.rev0.message.PongRedirectMessage;

/**
 * a one-time service-to-service pong redirect message send process
 */
public class PongRedirectSendProcess extends OneTimeSendProcess {
	
	private byte[] data;
	private CachedServiceAddress fromCachedServiceAddress;
	
	public PongRedirectSendProcess(byte[] data, CachedServiceAddress fromCachedServiceAddress, CachedServiceAddress toCachedServiceAddress) {
		super(toCachedServiceAddress);
		this.data = data;
		this.fromCachedServiceAddress = fromCachedServiceAddress;
	}

	@Override
	public RevisionMessage getS2SMessage(int revision) {
		switch(revision) {
		case 0:
		default: {
			return new Message(new PongRedirectMessage(new ServiceAddressComponent(fromCachedServiceAddress.getServiceAddress()), new DataComponent(data)));
		}
		}
	}
	
}
