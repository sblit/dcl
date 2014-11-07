package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * the connect echo reply BMCP command component
 */
public class ConnectEchoReplyBMCPCommandComponent extends BMCPCommandComponent {
	
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
		(echoDataComponent = ownEchoDataComponent).read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		echoDataComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return echoDataComponent.length();
	}
	
	@Override
	public String toString() {
		return "ConnectEchoReplyBMCPCommandComponent";
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { echoDataComponent };
	}

	@Override
	public byte getType() {
		return BMCPCommandComponent.CONNECT_ECHOREPLY;
	}
	
	/**
	 * sets the {@link DataComponent} instance that is written from
	 * @param echoDataComponent sets the {@link DataComponent} instance that is written from
	 */
	public void setEchoDataComponent(DataComponent echoDataComponent) {
		this.echoDataComponent = echoDataComponent;
	}
	
	/**
	 * @return the {@link DataComponent} instance that was read into or is written from
	 */
	public DataComponent getEchoDataComponent() {
		return echoDataComponent;
	}

	@Override
	public void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel) {
		bmcpManagementChannel.onReceiveConnectEchoReply(discontinuousBlock, dataId, this);
	}
	
}
