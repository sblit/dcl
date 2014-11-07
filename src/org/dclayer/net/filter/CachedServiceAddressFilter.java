package org.dclayer.net.filter;

import org.dclayer.net.llacache.CachedServiceAddress;
import org.dclayer.net.s2s.S2SPacket;

/**
 * a filter that filters {@link S2SPacket}s based on a {@link CachedServiceAddress}
 */
public class CachedServiceAddressFilter {
	/**
	 * the {@link CachedServiceAddress} to base filtering on
	 */
	private CachedServiceAddress cachedServiceAddress;
	
	/**
	 * creates a new {@link CachedServiceAddressFilter} for the given {@link CachedServiceAddress}
	 * @param cachedServiceAddress the {@link CachedServiceAddress} to base filtering on
	 */
	public CachedServiceAddressFilter(CachedServiceAddress cachedServiceAddress) {
		this.cachedServiceAddress = cachedServiceAddress;
	}
	
	/**
	 * returns the {@link CachedServiceAddress} that this {@link CachedServiceAddressFilter} bases its filtering on
	 * @return the {@link CachedServiceAddress} that this {@link CachedServiceAddressFilter} bases its filtering on
	 */
	public CachedServiceAddress getCachedServiceAddress() {
		return cachedServiceAddress;
	}
}
