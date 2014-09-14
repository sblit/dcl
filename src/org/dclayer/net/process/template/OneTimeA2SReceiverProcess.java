package org.dclayer.net.process.template;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.filter.ApplicationConnectionFilter;
import org.dclayer.net.filter.MessageFilter;

/**
 * base class for one-time application-to-service message receiver {@link Process}es
 */
public abstract class OneTimeA2SReceiverProcess extends Process {

	private ApplicationConnectionFilter applicationConnectionFilter;
	private MessageFilter messageFilter;
	private long timeout;

	public OneTimeA2SReceiverProcess(ApplicationConnection applicationConnection, MessageFilter messageFilter, long timeout) {
		this.applicationConnectionFilter = new ApplicationConnectionFilter(applicationConnection);
		this.messageFilter = messageFilter;
		this.timeout = timeout;
	}

	@Override
	public int defineProperties() {
		return A2SRECEIVER | APPLICATIONCONNECTIONFILTER | TIMEOUT | FINALIZECALLBACK;
	}
	
	@Override
	public MessageFilter getMessageFilter() {
		return messageFilter;
	}
	
	@Override
	public abstract Process receiveA2S(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId);
	
	@Override
	public ApplicationConnectionFilter getApplicationConnectionFilter() {
		return applicationConnectionFilter;
	}
	
	@Override
	public long getTimeout() {
		return timeout;
	}
	
	@Override
	public abstract Process onFinalize(boolean timeout);
	
	
}
