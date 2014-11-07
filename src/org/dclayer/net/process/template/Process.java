package org.dclayer.net.process.template;

import org.dclayer.exception.net.process.PropertyMethodNotImplementedException;
import org.dclayer.listener.net.FollowUpProcessSpawnInterface;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.filter.ApplicationConnectionFilter;
import org.dclayer.net.filter.CachedServiceAddressFilter;
import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.link.Link;
import org.dclayer.net.llacache.AddressCache;
import org.dclayer.net.llacache.CachedServiceAddress;

/**
 * a part of DCL's message processing mechanism that can provide and/or request different properties, such as: provide an address and a message to be sent to that address, accept specific messages from all or one specific address, request the spawning other {@link Process}es, etc.
 */
public abstract class Process {
	
	/**
	 * a {@link Process} with no properties (i.e. a {@link Process} that does nothing)
	 */
	private static class NullProcess extends Process {
		@Override
		protected int defineProperties() {
			return 0;
		}
	};
	
	/**
	 * a {@link Process} with no properties (i.e. a {@link Process} that does nothing); used by receiver {@link Process}es to accept a packet without spawning a new {@link Process}
	 */
	public static final Process NULLPROCESS = new NullProcess();
	
	/**
	 * the properties of this {@link Process}
	 */
	private int properties;
	/**
	 * whether (true) or not (false) this {@link Process} is finished
	 */
	private boolean dead = false;
	/**
	 * the {@link AddressCache} to use inside this {@link Process}
	 */
	private AddressCache addressCache;
	
	public Process() {
		this.properties = defineProperties();
	}
	
	/**
	 * initializes this Process, setting its {@link AddressCache}
	 * @param addressCache the {@link AddressCache} to use inside this {@link Process}
	 */
	public final void init(AddressCache addressCache) {
		this.addressCache = addressCache;
	}
	
	/**
	 * returns true if this {@link Process} is finished, false otherwise
	 * @return true if this {@link Process} is finished, false otherwise
	 */
	public final boolean isDead() {
		return dead;
	}
	
	/**
	 * sets the finished state of this {@link Process}
	 * @param dead the finished state of this {@link Process} (true if finished, false if not)
	 */
	public final void setDead(boolean dead) {
		this.dead = dead;
	}
	
	/**
	 * returns the AddressCache to use inside this {@link Process}
	 * @return the AddressCache to use inside this {@link Process}
	 */
	public final AddressCache getAddressCache() {
		return addressCache;
	}
	
	/**
	 * returns an integer describing the properties of this {@link Process}
	 * @return an integer describing the properties of this {@link Process}
	 */
	protected abstract int defineProperties();

	// ---
	
	/**
	 * this {@link Process} provides a service-to-service message and a destination {@link CachedServiceAddress}
	 */
	public static final int S2SMESSAGE = 1;
	/**
	 * this {@link Process} accepts service-to-service messages and provides a {@link MessageFilter}
	 */
	public static final int S2SRECEIVER = (1 << 1);
	/**
	 * this {@link Process} provides a {@link CachedServiceAddressFilter}
	 */
	public static final int CACHEDSERVICEADDRESSFILTER = (1 << 2);
	/**
	 * this {@link Process} provides an application-to-service message and a destination {@link ApplicationConnection}
	 */
	public static final int A2SMESSAGE = (1 << 3);
	/**
	 * this {@link Process} accepts application-to-service messages and provides a {@link MessageFilter}
	 */
	public static final int A2SRECEIVER = (1 << 4);
	/**
	 * this {@link Process} provides an {@link ApplicationConnectionFilter}
	 */
	public static final int APPLICATIONCONNECTIONFILTER = (1 << 5);
	/**
	 * this {@link Process} is a daemon running in its own Thread
	 */
	public static final int DAEMON = (1 << 6);
	/**
	 * this {@link Process} is persistent (i.e. it accepts more than one message)
	 */
	public static final int PERSISTENT = (1 << 7);
	/**
	 * this {@link Process} has a timeout after which it should be finalized
	 */
	public static final int TIMEOUT = (1 << 8);
	/**
	 * this {@link Process} requests a callback to be called on finalization
	 */
	public static final int FINALIZECALLBACK = (1 << 9);
	/**
	 * this {@link Process} holds a link to a remote service instance
	 */
	public static final int LINK = (1 << 10);
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#S2SMESSAGE} property set, false otherwise
	 */
	public boolean hasS2SMessage() {
		return (properties & S2SMESSAGE) != 0;
	}
	/**
	 * @param revision the desired service-to-service protocol revision
	 * @return the provided service-to-service message
	 */
	public RevisionMessage getS2SMessage(int revision) {
		throw new PropertyMethodNotImplementedException();
	}
	/**
	 * @return the {@link CachedServiceAddress} to which the service-to-service message should be sent
	 */
	public CachedServiceAddress getCachedServiceAddress() {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#S2SRECEIVER} property set, false otherwise
	 */
	public boolean hasS2SReceiver() {
		return (properties & S2SRECEIVER) != 0;
	}
	/**
	 * @return true if this {@link Process} has the {@link Process#A2SRECEIVER} property set, false otherwise
	 */
	public boolean hasA2SReceiver() {
		return (properties & A2SRECEIVER) != 0;
	}
	/**
	 * @return the provided {@link MessageFilter}
	 */
	public MessageFilter getMessageFilter() {
		throw new PropertyMethodNotImplementedException();
	}
	/**
	 * callback to call when a service-to-service message matching the {@link MessageFilter} was received
	 * @param cachedServiceAddress the {@link CachedServiceAddress} from which the message was received
	 * @param revisionMessage the {@link RevisionMessage} that was received
	 * @param revision the revision of the received packet
	 * @param messageTypeId the message type id of the received message
	 * @return a Process to be spawned if the message is accepted or null if the message is not accepted
	 */
	public Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		throw new PropertyMethodNotImplementedException();
	}
	/**
	 * callback to call when an application-to-service message matching the {@link MessageFilter} was received
	 * @param applicationConnection the {@link ApplicationConnection} from which the message was received
	 * @param revisionMessage the {@link RevisionMessage} that was received
	 * @param revision the revision of the received packet
	 * @param messageTypeId the message type id of the received message
	 * @return a Process to be spawned if the message is accepted or null if the message is not accepted
	 */
	public Process receiveA2S(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#CACHEDSERVICEADDRESSFILTER} property set, false otherwise
	 */
	public boolean hasCachedServiceAddressFilter() {
		return (properties & CACHEDSERVICEADDRESSFILTER) != 0;
	}
	/**
	 * @return the provided {@link CachedServiceAddressFilter}
	 */
	public CachedServiceAddressFilter getCachedServiceAddressFilter() {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#A2SMESSAGE} property set, false otherwise
	 */
	public boolean hasA2SMessage() {
		return (properties & A2SMESSAGE) != 0;
	}
	/**
	 * @param revision the desired application-to-service protocol revision
	 * @return the provided application-to-service message
	 */
	public RevisionMessage getA2SMessage(int revision) {
		throw new PropertyMethodNotImplementedException();
	}
	/**
	 * @return the {@link ApplicationConnection} to which the application-to-service message should be sent
	 */
	public ApplicationConnection getApplicationConnection() {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#APPLICATIONCONNECTIONFILTER} property set, false otherwise
	 */
	public boolean hasApplicationConnectionFilter() {
		return (properties & APPLICATIONCONNECTIONFILTER) != 0;
	}
	/**
	 * @return the provided {@link ApplicationConnectionFilter}
	 */
	public ApplicationConnectionFilter getApplicationConnectionFilter() {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#DAEMON} property set, false otherwise
	 */
	public boolean isDaemon() {
		return (properties & DAEMON) != 0;
	}
	/**
	 * starts the daemon {@link Process}'s thread
	 * @param followUpProcessSpawnInterface a {@link FollowUpProcessSpawnInterface} to use for spawning new {@link Process}es
	 */
	public void start(FollowUpProcessSpawnInterface followUpProcessSpawnInterface) {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#PERSISTENT} property set, false otherwise
	 */
	public boolean isPersistent() {
		return (properties & PERSISTENT) != 0;
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#TIMEOUT} property set, false otherwise
	 */
	public boolean hasTimeout() {
		return (properties & TIMEOUT) != 0;
	}
	/**
	 * @return this {@link Process}'s timeout, in milliseconds
	 */
	public long getTimeout() {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#FINALIZECALLBACK} property set, false otherwise
	 */
	public boolean hasFinalizeCallback() {
		return (properties & FINALIZECALLBACK) != 0;
	}
	/**
	 * callback to call on finalization of this {@link Process}
	 * @param timeout whether (true) or not (false) this {@link Process} is finalized because it timed out
	 * @return a {@link Process} to spawn, or null if no {@link Process} should be spawned
	 */
	public Process onFinalize(boolean timeout) {
		throw new PropertyMethodNotImplementedException();
	}
	
	
	/**
	 * @return true if this {@link Process} has the {@link Process#LINK} property set, false otherwise
	 */
	public boolean hasLink() {
		return (properties & LINK) != 0;
	}
	/**
	 * getter for the {@link Link} this {@link Process} is holding
	 * @return the {@link Link} this {@link Process} is holding
	 */
	public Link getLink() {
		throw new PropertyMethodNotImplementedException();
	}
	
}
