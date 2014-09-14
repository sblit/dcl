package org.dclayer.net.process.template;

import org.dclayer.listener.net.FollowUpProcessSpawnInterface;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.CachedServiceAddressFilter;
import org.dclayer.net.filter.MessageFilter;

/**
 * base class for persistent point-to-point service-to-service receiver {@link Process}es
 */
public abstract class PersistentS2SPointToPointReceiverProcess extends Process {
	
	/**
	 * A {@link FollowUpProcessSpawnInterface} used to spawn new {@link Process}es
	 */
	private FollowUpProcessSpawnInterface followUpProcessSpawnInterface;

	/**
	 * the {@link CachedServiceAddressFilter} of this receiver process
	 */
	private CachedServiceAddressFilter cachedServiceAddressFilter;
	
	/**
	 * the {@link MessageFilter} of this receiver process
	 */
	private MessageFilter messageFilter;

	public PersistentS2SPointToPointReceiverProcess(CachedServiceAddress cachedServiceAddress, MessageFilter messageFilter) {
		this.cachedServiceAddressFilter = new CachedServiceAddressFilter(cachedServiceAddress);
		this.messageFilter = messageFilter;
	}

	@Override
	public int defineProperties() {
		return S2SRECEIVER | CACHEDSERVICEADDRESSFILTER | PERSISTENT | DAEMON;
	}
	
	@Override
	public final void start(FollowUpProcessSpawnInterface followUpProcessSpawnInterface) {
		this.followUpProcessSpawnInterface = followUpProcessSpawnInterface;
		start();
	}
	
	/**
	 * returns the {@link FollowUpProcessSpawnInterface} for this {@link DaemonProcess}
	 * @return the {@link FollowUpProcessSpawnInterface} for this {@link DaemonProcess}
	 */
	protected FollowUpProcessSpawnInterface getFollowUpProcessSpawnInterface() {
		return followUpProcessSpawnInterface;
	}
	
	@Override
	public abstract Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId);
	
	/**
	 * called upon start of this Process
	 */
	protected abstract void start();
	
	@Override
	public CachedServiceAddressFilter getCachedServiceAddressFilter() {
		return cachedServiceAddressFilter;
	}
	
	@Override
	public MessageFilter getMessageFilter() {
		return messageFilter;
	}

}
