package org.dclayer.net.process;

import java.util.ArrayList;
import java.util.List;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeSendProcess;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressListComponent;
import org.dclayer.net.s2s.rev0.message.KnownAddressesReplyMessage;

/**
 * a one-time service-to-service known addresses reply message send process
 */
public class KnownAddressesReplySendProcess extends OneTimeSendProcess {
	
	private long limit, offset;
	
	public KnownAddressesReplySendProcess(AddressCache addressCache, CachedServiceAddress cachedServiceAddress, long limit, long offset) {
		super(cachedServiceAddress);
		this.limit = limit;
		this.offset = offset;
	}

	@Override
	public RevisionMessage getS2SMessage(int revision) {
		List<CachedServiceAddress> cachedServiceAddresses = getAddressCache().getReachableAddresses(limit, offset);
		switch(revision) {
		case 0:
		default: {
			ArrayList<ServiceAddressComponent> serviceAddressComponents = new ArrayList<ServiceAddressComponent>(cachedServiceAddresses.size());
			for(CachedServiceAddress cachedServiceAddress : cachedServiceAddresses) serviceAddressComponents.add(new ServiceAddressComponent(cachedServiceAddress.getServiceAddress()));
			ServiceAddressListComponent serviceAddressListComponent = new ServiceAddressListComponent(serviceAddressComponents);
			return new Message(new KnownAddressesReplyMessage(serviceAddressListComponent));
		}
		}
	}
	
}
