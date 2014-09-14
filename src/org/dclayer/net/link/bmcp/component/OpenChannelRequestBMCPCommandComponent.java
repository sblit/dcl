package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the open channel request BMCP command component
 */
public class OpenChannelRequestBMCPCommandComponent extends BMCPCommandComponent {
	
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
		channelId.read(byteBuf);
		protocol = byteBuf.readString();
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		channelId.write(byteBuf);
		byteBuf.writeString(protocol);
	}

	@Override
	public int length() {
		return channelId.length() + protocol.length() + 1;
	}
	
	@Override
	public String toString() {
		return String.format("OpenChannelRequestBMCPCommandComponent(channelId=%d, protocol=%s)", channelId.getNum(), protocol);
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.OPEN_CHANNEL_REQUEST;
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
		bmcpManagementChannel.onReceiveOpenChannelRequest(discontinuousBlock, dataId, this);
	}
	
}
