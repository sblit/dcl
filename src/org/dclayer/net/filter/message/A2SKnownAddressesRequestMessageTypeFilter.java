package org.dclayer.net.filter.message;

import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.Rev0MessageFilter;

/**
 * a {@link MessageFilter} matching bind messages of revision 0 of the application-to-service protocol
 */
public class A2SKnownAddressesRequestMessageTypeFilter extends MessageFilter {
	
	public A2SKnownAddressesRequestMessageTypeFilter() {
		super(new Rev0MessageFilter(org.dclayer.net.a2s.rev0.Message.KNOWN_ADDRESSES_REQUEST));
	}

}
