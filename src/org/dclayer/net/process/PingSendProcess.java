package org.dclayer.net.process;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeSendProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.message.PingMessage;

/**
 * a one-time service-to-service ping message send process
 */
public class PingSendProcess extends OneTimeSendProcess {
	
	/**
	 * if a direct ping fails, try a redirected ping
	 */
	public static final int FLAG_TRYPINGREDIRECT = 1;
	
	/**
	 * if the ping is successful, proceed with a known addresses request
	 */
	public static final int FLAG_REQUESTKNOWNADDRESSES = (1<<1);
	
	public static byte[] makeData() {
		long t = System.currentTimeMillis();
		return new byte[] { (byte)(t>>56), (byte)(t>>48), (byte)(t>>40), (byte)(t>>32), (byte)(t>>24), (byte)(t>>16), (byte)(t>>8), (byte)t };
	}
	
	private int flags, numRetries;
	private byte[] data;
	
	public PingSendProcess(CachedServiceAddress cachedServiceAddress, int flags, int numRetries) {
		super(cachedServiceAddress);
		this.flags = flags;
		this.numRetries = numRetries;
		this.data = makeData();
	}
	
	public PingSendProcess(CachedServiceAddress cachedServiceAddress, int numRetries) {
		this(cachedServiceAddress, 0, numRetries);
	}

	@Override
	public Process onFinalize(boolean timeout) {
		getAddressCache().setStatus(this.getCachedServiceAddress(), AddressCache.STATUS_PING, AddressCache.STATUS_PING_SENT);
		return new PongReceiveTimeoutProcess(this.getCachedServiceAddress(), data, flags, numRetries);
	}

	@Override
	public RevisionMessage getS2SMessage(int revision) {
		switch(revision) {
		case 0:
		default: {
			return new Message(new PingMessage(new DataComponent(data)));
		}
		}
	}
	
}
