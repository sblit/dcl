package org.dclayer.net.link;

import org.dclayer.net.link.channel.data.DataChannel;

/**
 * interface used by {@link Link} instances to determine whether or not a channel open request from the peer should be permitted
 */
public interface OnOpenChannelRequestListener {
	/**
	 * called when a new {@link DataChannel} should be opened
	 * @param link the {@link Link} the channel should be created on
	 * @param channelId the channel id of the new channel
	 * @param protocol the protocol identifier of the protocol to use on the channel
	 * @return the new {@link DataChannel}
	 */
	public DataChannel onOpenChannelRequest(Link link, long channelId, String protocol);
}
