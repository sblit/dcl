package org.dclayer.net.process;

import java.util.ArrayList;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.message.KnownAddressesReplyMessageTypeFilter;
import org.dclayer.net.process.template.OneTimeReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressListComponent;
import org.dclayer.net.s2s.rev0.message.KnownAddressesReplyMessage;

/**
 * a one-time service-to-service known addresses reply message timeout receiver process
 */
public class KnownAddressesReplyReceiveTimeoutProcess extends OneTimeReceiverProcess {
	
	private static long TIMEOUT = 5000;
	
	// ---
	
	private long limit, offset;

	public KnownAddressesReplyReceiveTimeoutProcess(CachedServiceAddress cachedServiceAddress, long limit, long offset) {
		super(cachedServiceAddress, new KnownAddressesReplyMessageTypeFilter(), TIMEOUT);
		this.limit = limit;
		this.offset = offset;
	}

	@Override
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		switch(revision) {
		case 0: {
			ServiceAddressListComponent serviceAddressListComponent = ((KnownAddressesReplyMessage) ((Message) revisionMessage).getMessage()).getServiceAddressListComponent();
			ArrayList<ServiceAddressComponent> serviceAddressComponents = serviceAddressListComponent.getAddresses();
			Log.debug(Log.PART_PROCESS, this, String.format("received reply from %s (%d addresses, requested: %d (offset %d)); adding addresses to AddressCache", cachedServiceAddress.getServiceAddress().toString(), serviceAddressComponents.size(), this.limit, this.offset));
			for(ServiceAddressComponent serviceAddressComponent : serviceAddressComponents) {
				getAddressCache().addServiceAddress(serviceAddressComponent.getServiceAddress(), cachedServiceAddress, AddressCache.STATUS_REACHABLEFROMREACHABLE);
			}
			return Process.NULLPROCESS;
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

	@Override
	public Process onFinalize(boolean timeout) {
		Log.debug(Log.PART_PROCESS, this, String.format("onFinalize: timeout=%s", timeout));
		getAddressCache().setStatus(this.getCachedServiceAddressFilter().getCachedServiceAddress(), AddressCache.STATUS_KNOWNADDRESSESREQUEST, timeout ? AddressCache.STATUS_KNOWNADDRESSESREQUEST_TIMEOUT : AddressCache.STATUS_KNOWNADDRESSESREQUEST_SUCCESSFUL);
		return null;
	}

}
