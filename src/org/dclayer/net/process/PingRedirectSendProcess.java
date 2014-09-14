package org.dclayer.net.process;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeSendProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;
import org.dclayer.net.s2s.rev0.message.PingRedirectMessage;

/**
 * a one-time service-to-service ping redirect message send process
 */
public class PingRedirectSendProcess extends OneTimeSendProcess {
	
	private byte[] data;
	private CachedServiceAddress redirectCachedServiceAddress;
	private int flags, numRetries;
	
	public PingRedirectSendProcess(CachedServiceAddress cachedServiceAddress, CachedServiceAddress redirectCachedServiceAddress, int flags, int numRetries) {
		super(cachedServiceAddress);
		this.data = PingSendProcess.makeData();
		this.redirectCachedServiceAddress = redirectCachedServiceAddress;
		this.flags = flags;
		this.numRetries = numRetries;
	}

	@Override
	public Process onFinalize(boolean timeout) {
		return new PongReceiveTimeoutProcess(redirectCachedServiceAddress, this.data, flags, numRetries);
	}

	@Override
	public RevisionMessage getS2SMessage(int revision) {
		switch(revision) {
		case 0:
		default: {
			return new Message(new PingRedirectMessage(new ServiceAddressComponent(redirectCachedServiceAddress.getServiceAddress()), new DataComponent(this.data)));
		}
		}
	}
	
}
