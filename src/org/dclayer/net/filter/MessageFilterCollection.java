package org.dclayer.net.filter;

import java.util.LinkedList;

import org.dclayer.DCL;

/**
 * a collection of {@link MessageFilter}s and Objects that are returned for each matching filter
 */
public class MessageFilterCollection {
	/**
	 * a two-dimensional array containing the Objects that are returned upon match of a filter, the outer array's indices represent revisions, the inner arrays' indices represent message type identifiers
	 */
	private LinkedList[][] revisionMessageTypes;
	
	/**
	 * returns a {@link LinkedList} of Objects whose {@link MessageFilter}s match the given revision and messageTypeId
	 * @param revision the revision to match
	 * @param messageTypeId the messageTypeId to match
	 * @return a {@link LinkedList} of Objects whose {@link MessageFilter}s match the given revision and messageTypeId
	 */
	public LinkedList getMatches(int revision, int messageTypeId) {
		if(revisionMessageTypes != null && revision < revisionMessageTypes.length) {
			LinkedList[] messageTypes = revisionMessageTypes[revision];
			if(messageTypes != null && messageTypeId < messageTypes.length) {
				return messageTypes[messageTypeId];
			}
		}
		return null;
	}
	
	/**
	 * adds a {@link MessageFilter} and a corresponding Object to this {@link MessageFilterCollection}
	 * @param messageFilter the {@link MessageFilter} to add
	 * @param o the Object to add
	 */
	public void addFilter(MessageFilter messageFilter, Object o) {
		RevisionMessageFilter[] revisionMessageFilters = messageFilter.getRevisionMessageFilters();
		for(RevisionMessageFilter revisionMessageFilter : revisionMessageFilters) {
			int revision = revisionMessageFilter.getRevision();
			int messageTypeId = revisionMessageFilter.getAllowedMessageTypeId();
			if(revisionMessageTypes == null) revisionMessageTypes = new LinkedList[Math.max(revision, DCL.REVISION)+1][];
			else if(revision >= revisionMessageTypes.length) {
				LinkedList[][] n = new LinkedList[revision+1][];
				System.arraycopy(revisionMessageTypes, 0, n, 0, revisionMessageTypes.length);
				revisionMessageTypes = n;
			}
			LinkedList[] messageTypes = revisionMessageTypes[revision];
			if(messageTypes == null) messageTypes = revisionMessageTypes[revision] = new LinkedList[messageTypeId+1];
			else if(messageTypeId >= messageTypes.length) {
				LinkedList[] n = new LinkedList[messageTypeId+1];
				System.arraycopy(messageTypes, 0, n, 0, messageTypes.length);
				messageTypes = revisionMessageTypes[revision] = n;
			}
			LinkedList objectList = messageTypes[messageTypeId];
			if(objectList == null) objectList = messageTypes[messageTypeId] = new LinkedList();
			objectList.add(o);
		}
	}
	
	/**
	 * removes the given Object from this {@link MessageFilterCollection}
	 * @param messageFilter the {@link MessageFilter} with which this Object was added
	 * @param o the Object to remove
	 */
	public void removeFilter(MessageFilter messageFilter, Object o) {
		if(revisionMessageTypes == null) return;
		RevisionMessageFilter[] revisionMessageFilters = messageFilter.getRevisionMessageFilters();
		for(RevisionMessageFilter revisionMessageFilter : revisionMessageFilters) {
			int revision = revisionMessageFilter.getRevision();
			int messageTypeId = revisionMessageFilter.getAllowedMessageTypeId();
			if(revision < revisionMessageTypes.length) {
				LinkedList[] messageTypes = revisionMessageTypes[revision];
				if(messageTypes != null && messageTypeId < messageTypes.length && messageTypes[messageTypeId] != null) {
					messageTypes[messageTypeId].remove(o);
				}
			}
		}
	}
}
