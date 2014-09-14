package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the throttle BMCP command component
 */
public class ThrottleBMCPCommandComponent extends BMCPCommandComponent {
	
	private FlexNum bytesPerSecond = new FlexNum();

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		bytesPerSecond.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		bytesPerSecond.write(byteBuf);
	}

	@Override
	public int length() {
		return bytesPerSecond.length();
	}
	
	@Override
	public String toString() {
		return String.format("ThrottleBMCPCommandComponent(bytesPerSecond=%d)", bytesPerSecond.getNum());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.THROTTLE;
	}
	
	/**
	 * @return the bytes per second value of this throttle command
	 */
	public long getBytesPerSecond() {
		return bytesPerSecond.getNum();
	}
	
	/**
	 * the bytes per second value of this throttle command
	 * @param bytesPerSecond the bytes per second value to use for this throttle command
	 */
	public void setBytesPerSecond(long bytesPerSecond) {
		this.bytesPerSecond.setNum(bytesPerSecond);
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveThrottle(discontinuousBlock, dataId, this);
	}
	
}
