package org.dclayer.net.filter.message;

import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.Rev0MessageFilter;

/**
 * a {@link MessageFilter} matching known addresses request messages of revision 0 of the service-to-service protocol
 */
public class KnownAddressesRequestMessageTypeFilter extends MessageFilter {
	
	public KnownAddressesRequestMessageTypeFilter() {
		super(new Rev0MessageFilter(org.dclayer.net.s2s.rev0.Message.KNOWN_ADDRESSES_REQUEST));
	}

}
