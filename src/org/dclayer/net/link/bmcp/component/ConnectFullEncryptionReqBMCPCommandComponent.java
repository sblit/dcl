package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the connect full encryption request BMCP command component
 */
public class ConnectFullEncryptionReqBMCPCommandComponent extends BMCPCommandComponent {
	
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
		return "ConnectFullEncryptionReqBMCPCommandComponent";
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.CONNECT_FULL_ENCRYPTION_REQUEST;
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveFullEncryptionRequest(discontinuousBlock, dataId, this);
	}
	
}
