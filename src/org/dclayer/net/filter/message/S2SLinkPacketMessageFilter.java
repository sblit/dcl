package org.dclayer.net.filter.message;

import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.Rev0MessageFilter;

/**
 * a {@link MessageFilter} matching link packet messages of revision 0 of the service-to-service protocol
 */
public class S2SLinkPacketMessageFilter extends MessageFilter {
	
	public S2SLinkPacketMessageFilter() {
		super(new Rev0MessageFilter(org.dclayer.net.s2s.rev0.Message.LINK_PACKET));
	}

}
