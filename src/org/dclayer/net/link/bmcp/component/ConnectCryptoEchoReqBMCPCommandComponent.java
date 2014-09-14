package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.component.DataComponent;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the connect crypto echo request BMCP command component
 */
public class ConnectCryptoEchoReqBMCPCommandComponent extends BMCPCommandComponent {
	
	/**
	 * {@link FlexNum} instance used for reading and writing the unreliable channel id value
	 */
	private FlexNum unreliableChannelId = new FlexNum();
	/**
	 * local {@link BMCPCryptoCommandComponent} instance that is used for reading into
	 */
	private BMCPCryptoCommandComponent ownBMCPCryptoCommandComponent = new BMCPCryptoCommandComponent();
	/**
	 * {@link BMCPCryptoCommandComponent} instance that is used for writing from
	 */
	private BMCPCryptoCommandComponent bmcpCryptoCommandComponent = ownBMCPCryptoCommandComponent;
	
	/**
	 * local {@link DataComponent} instance that is used for reading into
	 */
	private DataComponent ownEchoDataComponent = new DataComponent();
	/**
	 * local {@link DataComponent} instance that is used for writing from
	 */
	private DataComponent echoDataComponent = ownEchoDataComponent;
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		unreliableChannelId.read(byteBuf);
		(bmcpCryptoCommandComponent = ownBMCPCryptoCommandComponent).read(byteBuf);
		(echoDataComponent = ownEchoDataComponent).read(byteBuf);
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		unreliableChannelId.write(byteBuf);
		bmcpCryptoCommandComponent.write(byteBuf);
		echoDataComponent.write(byteBuf);
	}
	
	@Override
	public int length() {
		return unreliableChannelId.length() + bmcpCryptoCommandComponent.length() + echoDataComponent.length();
	}
	
	@Override
	public String toString() {
		return String.format("ConnectCryptoEchoReqBMCPCommandComponent(unreliableChannelId=%d)", unreliableChannelId.getNum());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { bmcpCryptoCommandComponent, echoDataComponent };
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.CONNECT_CRYPTO_ECHOREQUEST;
	}
	
	/**
	 * sets the unreliable channel id value
	 * @param unreliableChannelId the unreliable channel id value
	 */
	public void setUnreliableChannelId(long unreliableChannelId) {
		this.unreliableChannelId.setNum(unreliableChannelId);
	}
	
	/**
	 * @return the unreliable channel id value
	 */
	public long getUnreliableChannelId() {
		return unreliableChannelId.getNum();
	}
	
	/**
	 * sets the {@link BMCPCryptoCommandComponent} instance that is used to write from
	 * @param bmcpCryptoCommandComponent the {@link BMCPCryptoCommandComponent} instance that is used to write from
	 */
	public void setBMCPCryptoCommandComponent(BMCPCryptoCommandComponent bmcpCryptoCommandComponent) {
		this.bmcpCryptoCommandComponent = bmcpCryptoCommandComponent;
	}
	
	/**
	 * @return the {@link BMCPCryptoCommandComponent} instance that was read into or is written from
	 */
	public BMCPCryptoCommandComponent getBMCPCryptoCommandComponent() {
		return bmcpCryptoCommandComponent;
	}
	
	/**
	 * @return the {@link DataComponent} instance that was read into or is written from
	 */
	public DataComponent getEchoDataComponent() {
		return echoDataComponent;
	}
	
	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveConnectCryptoEchoReq(discontinuousBlock, dataId, this);
	}
	
}
