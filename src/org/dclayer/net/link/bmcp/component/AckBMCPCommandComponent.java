package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the acknowledgement BMCP command component
 */
public class AckBMCPCommandComponent extends BMCPCommandComponent {
	
	private FlexNum ackDataId = new FlexNum();

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		ackDataId.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		ackDataId.write(byteBuf);
	}

	@Override
	public int length() {
		return ackDataId.length();
	}
	
	@Override
	public String toString() {
		return String.format("AckBMCPCommandComponent(ackDataId=%d)", ackDataId.getNum());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.ACK;
	}
	
	/**
	 * @return the data id that is acknowledged
	 */
	public long getAckDataId() {
		return ackDataId.getNum();
	}
	
	/**
	 * sets the data id to be acknowledged
	 * @param bytesPerSecond the data id that is acknowledged
	 */
	public void setAckDataId(long bytesPerSecond) {
		this.ackDataId.setNum(bytesPerSecond);
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveAck(discontinuousBlock, dataId, this);
	}
	
}
