package org.dclayer.net.filter;

/**
 * a filter that matches exactly one revision and one message type identifier
 */
public abstract class RevisionMessageFilter {
	/**
	 * returns the revision this filter matches
	 * @return the revision this filter matches
	 */
	public abstract int getRevision();
	/**
	 * returns the message type id this filter matches
	 * @return the message type id this filter matches
	 */
	public abstract int getAllowedMessageTypeId();
}
