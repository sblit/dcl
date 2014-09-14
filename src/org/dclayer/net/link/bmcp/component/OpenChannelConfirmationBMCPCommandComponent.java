package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the open channel confirmation BMCP command component
 */
public class OpenChannelConfirmationBMCPCommandComponent extends BMCPCommandComponent {
	
	/**
	 * the data id of the preceding open channel request command that this command is acknowledging
	 */
	private FlexNum ackDataId = new FlexNum();
	/**
	 * the id of the new channel
	 */
	private FlexNum channelId = new FlexNum();
	/**
	 * the protocol identifier of the protocol used on the new channel
	 */
	private String protocol;

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		ackDataId.read(byteBuf);
		channelId.read(byteBuf);
		protocol = byteBuf.readString();
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		ackDataId.write(byteBuf);
		channelId.write(byteBuf);
		byteBuf.writeString(protocol);
	}

	@Override
	public int length() {
		return ackDataId.length() + channelId.length() + protocol.length() + 1;
	}
	
	@Override
	public String toString() {
		return String.format("OpenChannelConfirmationBMCPCommandComponent(ackDataId=%d, channelId=%d, protocol=%s)", ackDataId.getNum(), channelId.getNum(), protocol);
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.OPEN_CHANNEL_CONFIRMATION;
	}
	
	/**
	 * @return the data id of the preceding open channel request command that this command is acknowledging
	 */
	public long getAckDataId() {
		return ackDataId.getNum();
	}
	
	/**
	 * sets the data id of the preceding open channel request command that this command is acknowledging
	 * @param ackDataId the data id of the preceding open channel request command that this command is acknowledging
	 */
	public void setAckDataId(long ackDataId) {
		this.ackDataId.setNum(ackDataId);
	}
	
	/**
	 * @return the protocol identifier of the protocol used on the new channel
	 */
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * sets the protocol identifier of the protocol used on the new channel
	 * @param protocol the protocol identifier of the protocol used on the new channel
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * @return the id of the new channel
	 */
	public long getChannelId() {
		return channelId.getNum();
	}
	
	/**
	 * sets the id of the new channel
	 * @param channelId the id of the new channel
	 */
	public void setChannelId(long channelId) {
		this.channelId.setNum(channelId);
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveOpenChannelConfirmation(discontinuousBlock, dataId, this);
	}
	
}
