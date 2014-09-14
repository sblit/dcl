package org.dclayer.net.filter.message;

import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.Rev0MessageFilter;

/**
 * a {@link MessageFilter} matching known addresses reply messages of revision 0 of the service-to-service protocol
 */
public class KnownAddressesReplyMessageTypeFilter extends MessageFilter {
	
	public KnownAddressesReplyMessageTypeFilter() {
		super(new Rev0MessageFilter(org.dclayer.net.s2s.rev0.Message.KNOWN_ADDRESSES_REPLY));
	}

}
