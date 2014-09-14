package org.dclayer.net.filter.message;

import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.Rev0MessageFilter;
import org.dclayer.net.filter.Rev35MessageFilter;

/**
 * a {@link MessageFilter} matching data messages of revisions 0 and 35 of the application-to-service protocol
 */
public class A2SDataMessageFilter extends MessageFilter {
	
	public A2SDataMessageFilter() {
		super(	new Rev0MessageFilter(org.dclayer.net.a2s.rev0.Message.DATA),
				new Rev35MessageFilter(org.dclayer.net.a2s.rev35.Message.DATA)	);
	}

}
