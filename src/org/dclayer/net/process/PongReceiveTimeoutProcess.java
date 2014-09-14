package org.dclayer.net.process;

import java.util.Arrays;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.listener.net.FollowUpProcessSpawnInterface;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.message.PongMessageTypeFilter;
import org.dclayer.net.process.template.OneTimeReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.message.PongMessage;

/**
 * a one-time service-to-service pong message receiver process
 */
public class PongReceiveTimeoutProcess extends OneTimeReceiverProcess {
	
	private static long TIMEOUT = 5000;
	
	// ---
	
	private byte[] data;
	private int flags, numRetries;
	
	private FollowUpProcessSpawnInterface followUpProcessSpawnInterface;

	public PongReceiveTimeoutProcess(CachedServiceAddress cachedServiceAddress, byte[] data, int flags, int numRetries) {
		super(cachedServiceAddress, new PongMessageTypeFilter(), TIMEOUT);
		this.data = data;
		this.flags = flags;
		this.numRetries = numRetries;
	}
	
	@Override
	public int defineProperties() {
		return super.defineProperties() | DAEMON;
	}
	
	@Override
	public void start(FollowUpProcessSpawnInterface followUpProcessSpawnInterface) {
		this.followUpProcessSpawnInterface = followUpProcessSpawnInterface;
	}

	@Override
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		switch(revision) {
		case 0: {
			byte[] recvdata = ((PongMessage) ((Message) revisionMessage).getMessage()).getDataComponent().getData();
			boolean equal = Arrays.equals(recvdata, data);
			Log.debug(Log.PART_PROCESS, this, String.format("received reply from %s (%d bytes, original data is %d bytes); equal=%s", cachedServiceAddress.getServiceAddress().toString(), recvdata.length, this.data.length, equal));
			return equal ? Process.NULLPROCESS : null;
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

	@Override
	public Process onFinalize(boolean timeout) {
		Log.debug(Log.PART_PROCESS, this, String.format("onFinalize: timeout=%s", timeout));
		//getAddressCache().setStatus(this.getCachedServiceAddressFilter().getCachedServiceAddress(), AddressCache.STATUS_REACHABLE|AddressCache.STATUS_TIMEOUT, (timeout ? 0 : AddressCache.STATUS_REACHABLE)|(timeout ? AddressCache.STATUS_TIMEOUT : 0));
		if(timeout) {
			if((flags & PingSendProcess.FLAG_TRYPINGREDIRECT) != 0
					&& getCachedServiceAddressFilter().getCachedServiceAddress().getOriginCachedServiceAddress() != null
					&& getAddressCache().isReachable(getCachedServiceAddressFilter().getCachedServiceAddress().getOriginCachedServiceAddress())) {
				Log.debug(Log.PART_PROCESS, this, String.format("ping timed out, trying redirected ping"));
				return new PingRedirectSendProcess(getCachedServiceAddressFilter().getCachedServiceAddress().getOriginCachedServiceAddress(), this.getCachedServiceAddressFilter().getCachedServiceAddress(), numRetries > 0 ? flags : (flags & ~PingSendProcess.FLAG_TRYPINGREDIRECT), numRetries-1);
			} else if(numRetries > 0) {
				Log.debug(Log.PART_PROCESS, this, String.format("ping timed out, retrying (numRetries=%d)", numRetries));
				return new PingSendProcess(getCachedServiceAddressFilter().getCachedServiceAddress(), flags, numRetries-1);
			} else {
				Log.debug(Log.PART_PROCESS, this, String.format("ping timed out, giving up"));
				getAddressCache().setStatus(this.getCachedServiceAddressFilter().getCachedServiceAddress(), AddressCache.STATUS_PING, AddressCache.STATUS_PING_TIMEOUT);
			}
		} else {
			boolean firstTimeReachable = getAddressCache().setStatus(this.getCachedServiceAddressFilter().getCachedServiceAddress(), AddressCache.STATUS_PING, AddressCache.STATUS_PING_SUCCESSFUL);
			Log.debug(Log.PART_PROCESS, this, String.format("ping successful, firstTimeReachable=%s", firstTimeReachable));
			if(firstTimeReachable) {
				followUpProcessSpawnInterface.addFollowUpProcess(this, new S2SLinkProcess(this.getCachedServiceAddressFilter().getCachedServiceAddress()));
			}
			if((flags & PingSendProcess.FLAG_REQUESTKNOWNADDRESSES) != 0) {
				Log.debug(Log.PART_PROCESS, this, String.format("ping successful, sending known addresses request"));
				return new KnownAddressesRequestSendProcess(this.getCachedServiceAddressFilter().getCachedServiceAddress());
			} else {
				Log.debug(Log.PART_PROCESS, this, String.format("ping successful"));
			}
		}
		return null;
	}

}
