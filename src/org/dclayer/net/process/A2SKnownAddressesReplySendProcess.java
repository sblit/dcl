package org.dclayer.net.process;

import java.util.ArrayList;
import java.util.List;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.KnownAddressesReplyMessage;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeA2SSendProcess;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressListComponent;

/**
 * a one-time application-to-service known addresses reply message send process
 */
public class A2SKnownAddressesReplySendProcess extends OneTimeA2SSendProcess {
	
	private long limit;
	private long offset;
	
	public A2SKnownAddressesReplySendProcess(AddressCache addressCache, ApplicationConnection applicationConnection, long limit, long offset) {
		super(applicationConnection);
		this.limit = limit;
		this.offset = offset;
	}

	@Override
	public RevisionMessage getA2SMessage(int revision) {
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
