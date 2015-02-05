package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the kill link notification BMCP command component
 */
public class KillLinkBMCPCommandComponent extends BMCPCommandComponent {
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		
	}

	@Override
	public int length() {
		return 0;
	}
	
	@Override
	public String toString() {
		return "KillLinkBMCPCommandComponent";
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.KILL_LINK;
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveKillLink(discontinuousBlock, dataId, this);
	}
	
}