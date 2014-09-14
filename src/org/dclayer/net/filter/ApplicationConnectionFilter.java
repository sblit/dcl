package org.dclayer.net.filter;

import org.dclayer.net.a2s.A2SPacket;
import org.dclayer.net.a2s.ApplicationConnection;

/**
 * a filter that filters {@link A2SPacket}s based on an {@link ApplicationConnection}
 */
public class ApplicationConnectionFilter {
	/**
	 * the {@link ApplicationConnection} to base filtering on
	 */
	private ApplicationConnection applicationConnection;
	
	/**
	 * creates a new {@link ApplicationConnectionFilter} for the given {@link ApplicationConnection}
	 * @param applicationConnection the {@link ApplicationConnection} to base filtering on
	 */
	public ApplicationConnectionFilter(ApplicationConnection applicationConnection) {
		this.applicationConnection = applicationConnection;
	}
	
	/**
	 * returns the {@link ApplicationConnection} that this filter bases its filtering on
	 * @return the {@link ApplicationConnection} that this filter bases its filtering on
	 */
	public ApplicationConnection getApplicationConnection() {
		return applicationConnection;
	}
}
