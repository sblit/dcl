package org.dclayer.net.process.deliveryagent;

import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.process.template.Process;

/**
 * class holding a {@link Process} and its filter information
 */
public class ProcessMatch {
	/**
	 * the {@link Process} this object is holding filter information about
	 */
	protected Process process;
	/**
	 * the {@link CachedServiceAddress} this {@link ProcessMatch} is attached to
	 */
	protected CachedServiceAddress cachedServiceAddressAttachedTo = null;
	/**
	 * the {@link ApplicationConnection} this {@link ProcessMatch} is attached to
	 */
	protected ApplicationConnection applicationConnectionAttachedTo = null;
	/**
	 * the {@link MessageFilter} of the {@link Process}
	 */
	protected MessageFilter messageFilter = null;
	public ProcessMatch(Process process) {
		this.process = process;
	}
}