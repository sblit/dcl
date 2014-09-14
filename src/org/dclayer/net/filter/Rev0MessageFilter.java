package org.dclayer.net.filter;

/**
 * a subclass of {@link RevisionMessageFilter} matching exactly one message type id of revision 0
 */
public class Rev0MessageFilter extends RevisionMessageFilter {
	
	/**
	 * the message type id this filter matches
	 */
	private int allowedMessageTypeId;
	
	/**
	 * creates a new {@link Rev0MessageFilter} matching the given message type id
	 * @param allowedMessageTypeId the message type id to match
	 */
	public Rev0MessageFilter(int allowedMessageTypeId) {
		this.allowedMessageTypeId = allowedMessageTypeId;
	}

	@Override
	public int getRevision() {
		return 0;
	}
	
	@Override
	public int getAllowedMessageTypeId() {
		return allowedMessageTypeId;
	}
	
}
