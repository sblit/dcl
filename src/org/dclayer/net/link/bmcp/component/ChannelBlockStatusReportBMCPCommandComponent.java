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
 * the channel block status report BMCP command component
 */
public class ChannelBlockStatusReportBMCPCommandComponent extends BMCPCommandComponent {
	
	/**
	 * the amount of following channel reports
	 */
	private FlexNum numFollowingChannels = new FlexNum();
	/**
	 * a list of channel reports, of which the first {@link ChannelBlockStatusReportBMCPCommandComponent#numFollowingChannels}
	 * elements represent each channel's report of this block status report
	 */
	private LinkedList<BMCPChannelReport> channelReports = new LinkedList<BMCPChannelReport>();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		numFollowingChannels.read(byteBuf);
		final long num = numFollowingChannels.getNum();
		long i = 0;
		for(BMCPChannelReport channelReport : channelReports) {
			if(i >= num) break;
			channelReport.read(byteBuf);
			i++;
		}
		while(i < num) {
			BMCPChannelReport channelReport = new BMCPChannelReport();
			channelReport.read(byteBuf);
			channelReports.add(channelReport);
			i++;
		}
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		numFollowingChannels.write(byteBuf);
		final long num = numFollowingChannels.getNum();
		long i = 0;
		for(BMCPChannelReport channelReport : channelReports) {
			if(i >= num) break;
			channelReport.write(byteBuf);
			i++;
		}
	}

	@Override
	public int length() {
		int length = 0;
		final long num = numFollowingChannels.getNum();
		long i = 0;
		for(BMCPChannelReport channelReport : channelReports) {
			if(i++ >= num) break;
			length += channelReport.length();
		}
		return numFollowingChannels.length() + length;
	}
	
	@Override
	public String toString() {
		return String.format("ChannelBlockStatusReportBMCPCommandComponent(numChannelIds=%d)", numFollowingChannels.getNum());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		BMCPChannelReport[] channelReportArray = new BMCPChannelReport[(int) Math.min(Integer.MAX_VALUE, numFollowingChannels.getNum())];
		int i = 0;
		for(BMCPChannelReport channelReport : channelReports) {
			if(i >= channelReportArray.length) break;
			channelReportArray[i++] = channelReport;
		}
		return channelReportArray;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.CHANNEL_BLOCK_STATUS_REPORT;
	}
	
	/**
	 * sets the amount of following channel reports
	 * @param numFollowingChannelIds the amount of following channel reports
	 */
	public void setNumFollowingChannels(long numFollowingChannelIds) {
		this.numFollowingChannels.setNum(numFollowingChannelIds);
	}
	
	/**
	 * @return the amount of following channel reports
	 */
	public long getNumFollowingChannels() {
		return numFollowingChannels.getNum();
	}
	
	/**
	 * @return a list of channel reports, of which the first {@link ChannelBlockStatusReportBMCPCommandComponent#getNumFollowingChannels()}
	 * elements represent each channel's report of this block status report
	 */
	public LinkedList<BMCPChannelReport> getChannelReports() {
		return channelReports;
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveChannelBlockStatusReport(discontinuousBlock, dataId, this);
	}
	
}
