package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the connect crypto BMCP command component
 */
public class ConnectCryptoBMCPCommandComponent extends BMCPCommandComponent {
	
	/**
	 * the {@link BMCPCryptoCommandComponent} containing the crypto information for this connect crypto command
	 */
	private BMCPCryptoCommandComponent bmcpCryptoCommandComponent = new BMCPCryptoCommandComponent();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		bmcpCryptoCommandComponent.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		bmcpCryptoCommandComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return bmcpCryptoCommandComponent.length();
	}
	
	@Override
	public String toString() {
		return "ConnectCryptoBMCPCommandComponent";
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { bmcpCryptoCommandComponent };
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.CONNECT_CRYPTO;
	}
	
	/**
	 * @return the {@link BMCPCryptoCommandComponent} of this {@link ConnectCryptoBMCPCommandComponent}
	 */
	public BMCPCryptoCommandComponent getBMCPCryptoCommandComponent() {
		return bmcpCryptoCommandComponent;
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveConnectCrypto(discontinuousBlock, dataId, this);
	}
	
}
