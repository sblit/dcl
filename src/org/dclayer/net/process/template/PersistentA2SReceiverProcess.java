package org.dclayer.net.process.template;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.filter.MessageFilter;

/**
 * base class for persistent application-to-service receiver {@link Process}es
 */
public abstract class PersistentA2SReceiverProcess extends Process {

	/**
	 * the {@link MessageFilter} of this receiver process
	 */
	private MessageFilter messageFilter;

	public PersistentA2SReceiverProcess(MessageFilter messageFilter) {
		this.messageFilter = messageFilter;
	}

	@Override
	public int defineProperties() {
		return A2SRECEIVER | PERSISTENT;
	}
	
	@Override
	public abstract Process receiveA2S(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId);
	
	@Override
	public MessageFilter getMessageFilter() {
		return messageFilter;
	}

}
