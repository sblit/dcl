package org.dclayer.net.addresscache;

import org.dclayer.net.filter.MessageFilterCollection;
import org.dclayer.net.link.Link;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * a class holding a {@link ServiceAddress}, another {@link CachedServiceAddress} as the address from which this address was received, a status and other variables used for address management and packet distribution
 */
public class CachedServiceAddress {
	
	/**
	 * true if this {@link CachedServiceAddress} was at no time reachable yet
	 */
	public boolean neverReachable = true;
	
	/**
	 * the {@link Link} connected to this {@link CachedServiceAddress}
	 */
	public Link link;
	
	/**
	 * the actual {@link ServiceAddress} that is cached
	 */
	protected ServiceAddress serviceAddress;
	/**
	 * the address from which this address was received from
	 */
	protected CachedServiceAddress originCachedServiceAddress;
	/**
	 * the status of this address
	 */
	protected int status;
	/**
	 * the timestamp of the last ping sent to this address
	 */
	protected long lastPing;
	/**
	 * the timestamp of the last receipt from this address
	 */
	protected long lastRecv;
	/**
	 * the {@link MessageFilterCollection} applying to Packets received from this address
	 */
	protected MessageFilterCollection messageFilterCollection;
	
	/**
	 * creates a new {@link CachedServiceAddress}
	 * @param serviceAddress the {@link ServiceAddress} to cache
	 * @param originCachedServiceAddress the {@link CachedServiceAddress} from which this address was received
	 * @param status the initial status of this {@link CachedServiceAddress}
	 */
	CachedServiceAddress(ServiceAddress serviceAddress, CachedServiceAddress originCachedServiceAddress, int status) {
		this.serviceAddress = serviceAddress;
		this.originCachedServiceAddress = originCachedServiceAddress;
		this.status = status;
	}
	
	/**
	 * returns the cached {@link ServiceAddress}
	 * @return the cached {@link ServiceAddress}
	 */
	public ServiceAddress getServiceAddress() {
		return serviceAddress;
	}
	
	/**
	 * returns the {@link CachedServiceAddress} this {@link ServiceAddress} was received from
	 * @return the {@link CachedServiceAddress} this {@link ServiceAddress} was received from
	 */
	public CachedServiceAddress getOriginCachedServiceAddress() {
		return originCachedServiceAddress;
	}
	
	/**
	 * returns the current status of this {@link CachedServiceAddress}
	 * @return the current status of this {@link CachedServiceAddress}
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * returns the {@link MessageFilterCollection} attached to this {@link CachedServiceAddress}, optionally creating it if specified and if it does not already exist
	 * @param create if true, attach a new {@link MessageFilterCollection} if there is none attached, otherwise return null if no {@link MessageFilterCollection} is attached
	 * @return the {@link MessageFilterCollection} attached to this {@link CachedServiceAddress} (may be null if none is attached and false is passed as the create parameter)
	 */
	public MessageFilterCollection getMessageFilterCollection(boolean create) {
		if(create && this.messageFilterCollection == null) this.messageFilterCollection = new MessageFilterCollection();
		return this.messageFilterCollection;
	}
	
	/**
	 * returns the {@link MessageFilterCollection} attached to this {@link CachedServiceAddress}
	 * @return the {@link MessageFilterCollection} attached to this {@link CachedServiceAddress} (may be null if none is attached)
	 */
	public MessageFilterCollection getMessageFilterCollection() {
		return getMessageFilterCollection(false);
	}
	
	/**
	 * returns true if this {@link CachedServiceAddress} matches the given status, false otherwise
	 * @param statusMask a binary mask specifying the area of a {@link CachedServiceAddress}'s status that has to equal the given status
	 * @param status the status that a {@link CachedServiceAddress}'s status has to equal inside of the given statusMask
	 * @return true if this {@link CachedServiceAddress} matches the given status, false otherwise
	 */
	protected boolean hasStatus(int statusMask, int status) {
		return (this.status & statusMask) == status;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s) (origin %s)", serviceAddress.toString(), AddressCache.statusToString(status), originCachedServiceAddress != null ? originCachedServiceAddress.getServiceAddress().toString() : null);
	}
}