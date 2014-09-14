package org.dclayer.net.link.bmcp.component;
import java.util.LinkedList;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the channel block status request BMCP command component
 */
public class ChannelBlockStatusReqBMCPCommandComponent extends BMCPCommandComponent {
	
	/**
	 * the amount of following channel ids
	 */
	private FlexNum numFollowingChannelIds = new FlexNum();
	/**
	 * a list of channel ids, of which the first {@link ChannelBlockStatusReqBMCPCommandComponent#numFollowingChannelIds}
	 * elements represent the channel ids of this block status request
	 */
	private LinkedList<FlexNum> channelIds = new LinkedList<FlexNum>();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		numFollowingChannelIds.read(byteBuf);
		final long num = numFollowingChannelIds.getNum();
		long i = 0;
		for(FlexNum channelId : channelIds) {
			if(i >= num) break;
			channelId.read(byteBuf);
			i++;
		}
		while(i < num) {
			FlexNum channelId = new FlexNum();
			channelId.read(byteBuf);
			channelIds.add(channelId);
			i++;
		}
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		numFollowingChannelIds.write(byteBuf);
		final long num = numFollowingChannelIds.getNum();
		long i = 0;
		for(FlexNum channelId : channelIds) {
			if(i >= num) break;
			channelId.write(byteBuf);
			i++;
		}
	}

	@Override
	public int length() {
		int length = 0;
		final long num = numFollowingChannelIds.getNum();
		long i = 0;
		for(FlexNum channelId : channelIds) {
			if(i++ >= num) break;
			length += channelId.length();
		}
		return numFollowingChannelIds.length() + length;
	}
	
	@Override
	public String toString() {
		return String.format("ChannelBlockStatusReqBMCPCommandComponent(numChannelIds=%d)", numFollowingChannelIds.getNum());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		FlexNum[] channelIdArray = new FlexNum[(int) Math.min(Integer.MAX_VALUE, numFollowingChannelIds.getNum())];
		int i = 0;
		for(FlexNum channelId : channelIds) {
			if(i >= channelIdArray.length) break;
			channelIdArray[i++] = channelId;
		}
		return channelIdArray;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.CHANNEL_BLOCK_STATUS_REQUEST;
	}
	
	/**
	 * sets the amount of following channel ids
	 * @param numFollowingChannelIds the amount of following channel ids
	 */
	public void setNumFollowingChannelIds(long numFollowingChannelIds) {
		this.numFollowingChannelIds.setNum(numFollowingChannelIds);
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveChannelBlockStatusRequest(discontinuousBlock, dataId, this);
	}
	
}
