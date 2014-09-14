package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the change management channel protocol confirmation BMCP command component
 */
public class ChangeMgmtChProtoConBMCPCommandComponent extends BMCPCommandComponent {
	
	/**
	 * the identifier of the new protocol to use
	 */
	private String protocol;

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		protocol = byteBuf.readString();
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.writeString(protocol);
	}

	@Override
	public int length() {
		return protocol.length() + 1;
	}
	
	@Override
	public String toString() {
		return String.format("ChangeMgmtChProtoCon(protocol=%s)", protocol);
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.CHANGE_MGMTCHANNEL_PROTOCOL_CONFIRMATION;
	}
	
	/**
	 * @return the protocol identifier of the new management channel protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * sets the protocol identifier of the new management channel protocol
	 * @param protocol the protocol identifier of the new management channel protocol
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		
	}
	
}
