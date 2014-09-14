package org.dclayer.net.link.bmcp.component;
import java.util.LinkedList;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;

/**
 * a block status report for a single channel
 */
public class BMCPChannelReport extends PacketComponent {
	
	/**
	 * the channel id of the channel this is a report for
	 */
	private FlexNum channelId = new FlexNum();
	/**
	 * the lowest data id received on the channel
	 */
	private FlexNum lowestDataId = new FlexNum();
	/**
	 * the highest data id received on the channel
	 */
	private FlexNum highestDataId = new FlexNum();
	/**
	 * the amount of different data ids received on the channel
	 */
	private FlexNum numDataIds = new FlexNum();
	/**
	 * the amount of missing single data ids on the channel
	 */
	private FlexNum numMissingSingleIds = new FlexNum();
	/**
	 * a list of single data ids, of which the first {@link BMCPChannelReport#numMissingSingleIds} elements
	 * represent the missing single data ids on the channel
	 */
	private LinkedList<FlexNum> missingSingleIds = new LinkedList<FlexNum>();
	/**
	 * the amount of missing data id blocks on the channel
	 */
	private FlexNum numMissingIdBlocks = new FlexNum();
	/**
	 * a list of data id blocks, of which the first {@link BMCPChannelReport#numMissingIdBlocks} elements
	 * represent the missing data id blocks on the channel
	 */
	private LinkedList<BMCPIdBlock> missingIdBlocks = new LinkedList<BMCPIdBlock>();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		
		channelId.read(byteBuf);
		lowestDataId.read(byteBuf);
		highestDataId.read(byteBuf);
		numDataIds.read(byteBuf);
		numMissingSingleIds.read(byteBuf);
		
		final long numSingleIds = numMissingSingleIds.getNum();
		long i = 0;
		for(FlexNum singleId : missingSingleIds) {
			if(i >= numSingleIds) break;
			singleId.read(byteBuf);
			i++;
		}
		while(i < numSingleIds) {
			FlexNum singleId = new FlexNum();
			singleId.read(byteBuf);
			missingSingleIds.add(singleId);
			i++;
		}
		
		numMissingIdBlocks.read(byteBuf);
		
		final long numBlocks = numMissingIdBlocks.getNum();
		i = 0;
		for(BMCPIdBlock idBlock : missingIdBlocks) {
			if(i >= numBlocks) break;
			idBlock.read(byteBuf);
			i++;
		}
		while(i < numBlocks) {
			BMCPIdBlock idBlock = new BMCPIdBlock();
			idBlock.read(byteBuf);
			missingIdBlocks.add(idBlock);
			i++;
		}
		
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		
		channelId.write(byteBuf);
		lowestDataId.write(byteBuf);
		highestDataId.write(byteBuf);
		numDataIds.write(byteBuf);
		numMissingSingleIds.write(byteBuf);
		
		final long numSingleIds = numMissingSingleIds.getNum();
		long i = 0;
		for(FlexNum singleId : missingSingleIds) {
			if(i >= numSingleIds) break;
			singleId.write(byteBuf);
			i++;
		}
		
		numMissingIdBlocks.write(byteBuf);
		
		final long numBlocks = numMissingIdBlocks.getNum();
		i = 0;
		for(BMCPIdBlock idBlock : missingIdBlocks) {
			if(i >= numBlocks) break;
			idBlock.write(byteBuf);
			i++;
		}
		
	}

	@Override
	public PacketComponent[] getChildren() {
		PacketComponent[] children = new PacketComponent[(int) Math.min(Integer.MAX_VALUE, numMissingSingleIds.getNum() + numMissingIdBlocks.getNum())];
		int i = 0;
		final long numSingleIds = numMissingSingleIds.getNum();
		long j = 0;
		for(FlexNum singleId : missingSingleIds) {
			if(j++ >= numSingleIds) break;
			children[i++] = singleId;
		}
		final long numBlocks = numMissingIdBlocks.getNum();
		j = 0;
		for(BMCPIdBlock idBlock : missingIdBlocks) {
			if(j++ >= numBlocks) break;
			children[i++] = idBlock;
		}
		return children;
	}

	@Override
	public int length() {
		int length = 0;
		final long numSingleIds = numMissingSingleIds.getNum();
		long i = 0;
		for(FlexNum singleId : missingSingleIds) {
			if(i++ >= numSingleIds) break;
			length += singleId.length();
		}
		final long numBlocks = numMissingIdBlocks.getNum();
		i = 0;
		for(BMCPIdBlock idBlock : missingIdBlocks) {
			if(i++ >= numBlocks) break;
			length += idBlock.length();
		}
		return channelId.length() + lowestDataId.length() + highestDataId.length() + numDataIds.length() + numMissingSingleIds.length() + numMissingIdBlocks.length() + length;
	}
	
	@Override
	public String toString() {
		return String.format("ChannelReport(channelId=%d, lowestDataId=%d, highestDataId=%d, numDataIds=%d, numMissingSingleIds=%d, numMissingIdBlocks=%d)", channelId.getNum(), lowestDataId.getNum(), highestDataId.getNum(), numDataIds.getNum(), numMissingSingleIds.getNum(), numMissingIdBlocks.getNum());
	}
	
	/**
	 * @return a list of single data ids, of which the first {@link BMCPChannelReport#getNumMissingSingleIds()} elements
	 * represent the missing single data ids on the channel
	 */
	public LinkedList<FlexNum> getMissingSingleIds() {
		return missingSingleIds;
	}
	
	/**
	 * @return a list of data id blocks, of which the first {@link BMCPChannelReport#getNumMissingIdBlocks()} elements
	 * represent the missing data id blocks on the channel
	 */
	public LinkedList<BMCPIdBlock> getMissingIdBlocks() {
		return missingIdBlocks;
	}
	
	/**
	 * sets the channel id this is a report for
	 * @param channelId the channel id this is a report for
	 */
	public void setChannelId(long channelId) {
		this.channelId.setNum(channelId);
	}
	
	/**
	 * sets the lowest data id received on this channel
	 * @param lowestDataId the lowest data id
	 */
	public void setLowestDataId(long lowestDataId) {
		this.lowestDataId.setNum(lowestDataId);
	}
	
	/**
	 * sets the highest data id received on this channel
	 * @param highestDataId the highest data id
	 */
	public void setHighestDataId(long highestDataId) {
		this.highestDataId.setNum(highestDataId);
	}
	
	/**
	 * sets the amount of different data ids received on this channel
	 * @param numDataIds the amount of different data ids received on this channel
	 */
	public void setNumDataIds(long numDataIds) {
		this.numDataIds.setNum(numDataIds);
	}
	
	/**
	 * sets the amount of missing single data ids on this channel
	 * @param numMissingSingleIds the amount of missing single data ids on this channel
	 */
	public void setNumMissingSingleIds(long numMissingSingleIds) {
		this.numMissingSingleIds.setNum(numMissingSingleIds);
	}
	
	/**
	 * sets the amount of missing data id blocks on this channel
	 * @param numMissingIdBlocks the amount of missing data id blocks on this channel
	 */
	public void setNumMissingIdBlocks(long numMissingIdBlocks) {
		this.numMissingIdBlocks.setNum(numMissingIdBlocks);
	}
	
	/**
	 * @return the channel id of the channel this is a report for
	 */
	public long getChannelId() {
		return channelId.getNum();
	}
	
	/**
	 * @return the lowest data id received on the channel
	 */
	public long getLowestDataId() {
		return lowestDataId.getNum();
	}
	
	/**
	 * @return the highest data id received on the channel
	 */
	public long getHighestDataId() {
		return highestDataId.getNum();
	}
	
	/**
	 * @return the amount of different data ids received on the channel
	 */
	public long getNumDataIds() {
		return numDataIds.getNum();
	}
	
	/**
	 * @return the amount of missing single data ids on the channel
	 */
	public long getNumMissingSingleIds() {
		return numMissingSingleIds.getNum();
	}
	
	/**
	 * @return the amount of missing data id blocks on the channel
	 */
	public long getNumMissingIdBlocks() {
		return numMissingIdBlocks.getNum();
	}

}
