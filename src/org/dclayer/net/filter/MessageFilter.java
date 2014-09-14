package org.dclayer.net.filter;

/**
 * a filter that filters packets based on revisions and message type identifiers
 */
public class MessageFilter {
	/**
	 * array containing {@link RevisionMessageFilter}s
	 */
	private RevisionMessageFilter[] revisionMessageFilters;
	
	/**
	 * creates a new {@link MessageFilter} for the given {@link RevisionMessageFilter}s
	 * @param revisionMessageFilters the {@link RevisionMessageFilter}s to base filtering on
	 */
	public MessageFilter(RevisionMessageFilter... revisionMessageFilters) {
		this.revisionMessageFilters = revisionMessageFilters;
	}
	
	/**
	 * returns the array containing {@link RevisionMessageFilter}s that this {@link MessageFilter} bases its filtering on
	 * @return the array containing {@link RevisionMessageFilter}s that this {@link MessageFilter} bases its filtering on
	 */
	public RevisionMessageFilter[] getRevisionMessageFilters() {
		return revisionMessageFilters;
	}
}
