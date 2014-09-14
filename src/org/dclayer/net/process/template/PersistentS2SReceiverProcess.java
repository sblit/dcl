package org.dclayer.net.process.template;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.MessageFilter;

/**
 * base class for persistent service-to-service receiver {@link Process}es
 */
public abstract class PersistentS2SReceiverProcess extends Process {

	/**
	 * the {@link MessageFilter} of this receiver process
	 */
	private MessageFilter messageFilter;

	public PersistentS2SReceiverProcess(MessageFilter messageFilter) {
		this.messageFilter = messageFilter;
	}

	@Override
	public int defineProperties() {
		return S2SRECEIVER | PERSISTENT;
	}
	
	@Override
	public abstract Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId);
	
	@Override
	public MessageFilter getMessageFilter() {
		return messageFilter;
	}

}
