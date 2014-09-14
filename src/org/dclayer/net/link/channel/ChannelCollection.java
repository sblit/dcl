package org.dclayer.net.link.channel;
import java.util.LinkedList;
import java.util.List;

import org.dclayer.datastructure.tree.ParentTreeNode;


/**
 * a tree data structure for efficient mapping of channel ids to {@link Channel} objects
 */
public class ChannelCollection {
	
	/**
	 * the root node of the tree
	 */
	private ParentTreeNode<Channel> channelTree = new ParentTreeNode<Channel>(0);
	/**
	 * an additional list of all channels, used for iteration
	 */
	private LinkedList<Channel> channelList = new LinkedList<Channel>();
	
	/**
	 * creates a new, empty {@link ChannelCollection}
	 */
	public ChannelCollection() {
		
	}
	
	/**
	 * returns the corresponding {@link Channel} object for the given channel id, if found
	 * @param channelId the channel id to search for
	 * @return the corresponding {@link Channel} object for the given channel id, if found, null otherwise
	 */
	public synchronized Channel get(long channelId) {
		return channelTree.get(channelId);
	}
	
	/**
	 * puts the given {@link Channel} object into this {@link ChannelCollection}, storing it for the given channel id
	 * @param channel the {@link Channel} to store
	 */
	public synchronized void put(Channel channel) {
		long channelId = channel.getChannelId();
		assign(channelId, channel);
		channelList.add(channel);
	}
	
	/**
	 * assigns the given {@link Channel} to the given channel id
	 * @param channelId the channel id to assign the {@link Channel} to
	 * @param channel the {@link Channel} to assign to the given channel id
	 */
	public void assign(long channelId, Channel channel) {
		Channel oldChannel = channelTree.get(channelId);
		if(oldChannel != null) {
			channelList.remove(oldChannel);
		}
		channelTree.put(channelId, channel);
	}
	
	/**
	 * clears the corresponding {@link Channel} object for the given channel id out of this {@link ChannelCollection}, if any
	 * @param channelId the channel id to clear the {@link Channel} object for
	 */
	public synchronized void clear(long channelId) {
		Channel channel = channelTree.remove(channelId);
		if(channel != null) {
			channelList.remove(channel);
		}
	}
	
	/**
	 * @return a list of all contained channels
	 */
	public synchronized List<Channel> getChannels() {
		return channelList;
	}
	
	@Override
	public synchronized String toString() {
		return String.format("ChannelCollection(%d channels)", channelList.size());
	}
	
	/**
	 * represents this {@link ChannelCollection} and its tree data structure
	 * @param tree if true, representation will be multi-lined
	 * @return a string representation of this {@link ChannelCollection} and its tree data structure
	 */
	public synchronized String represent(boolean tree) {
		return String.format("%s: %s", this.toString(), channelTree.represent(tree));
	}
	
}
