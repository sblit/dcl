package org.dclayer.net.filter.message;

import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.Rev0MessageFilter;

/**
 * a {@link MessageFilter} matching ping redirect messages of revision 0 of the service-to-service protocol
 */
public class PingRedirectMessageTypeFilter extends MessageFilter {
	
	public PingRedirectMessageTypeFilter() {
		super(new Rev0MessageFilter(org.dclayer.net.s2s.rev0.Message.REDIRECT_PING));
	}

}
