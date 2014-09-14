package org.dclayer.net.link.bmcp.component;


import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedBMCPCommandTypeException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.link.channel.component.ChannelDataComponent;

/**
 * {@link PacketComponent} containing a basic management channel protocol (BMCP) command
 */
public class BMCPChannelDataComponent extends ChannelDataComponent {
	
	/**
	 * the currently selected {@link BMCPCommandComponent}
	 */
	private BMCPCommandComponent commandComponent;
	private ChangeMgmtChProtoReqBMCPCommandComponent changeMgmtChProtoReqBMCPCommandComponent = new ChangeMgmtChProtoReqBMCPCommandComponent();
	private ChangeMgmtChProtoConBMCPCommandComponent changeMgmtChProtoConBMCPCommandComponent = new ChangeMgmtChProtoConBMCPCommandComponent();
	private ConnectCryptoBMCPCommandComponent connectCryptoBMCPCommandComponent = new ConnectCryptoBMCPCommandComponent();
	private ConnectCryptoEchoReqBMCPCommandComponent connectCryptoEchoReqBMCPCommandComponent = new ConnectCryptoEchoReqBMCPCommandComponent();
	private ConnectEchoReplyBMCPCommandComponent connectEchoReplyBMCPCommandComponent = new ConnectEchoReplyBMCPCommandComponent();
	private ConnectFullEncryptionReqBMCPCommandComponent connectFullEncryptionReqBMCPCommandComponent = new ConnectFullEncryptionReqBMCPCommandComponent();
	private ConnectConfirmationBMCPCommandComponent connectConfirmationBMCPCommandComponent = new ConnectConfirmationBMCPCommandComponent();
	private ChannelBlockStatusReqBMCPCommandComponent channelBlockStatusReqBMCPCommandComponent = new ChannelBlockStatusReqBMCPCommandComponent();
	private ChannelBlockStatusReportBMCPCommandComponent channelBlockStatusReportBMCPCommandComponent = new ChannelBlockStatusReportBMCPCommandComponent();
	private OpenChannelRequestBMCPCommandComponent openChannelRequestBMCPCommandComponent = new OpenChannelRequestBMCPCommandComponent();
	private OpenChannelConfirmationBMCPCommandComponent openChannelConfirmationBMCPCommandComponent = new OpenChannelConfirmationBMCPCommandComponent();
	private ThrottleBMCPCommandComponent throttleBMCPCommandComponent = new ThrottleBMCPCommandComponent();
	private AckBMCPCommandComponent ackBMCPCommandComponent = new AckBMCPCommandComponent();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		byte commandType = byteBuf.read();
		switch(commandType) {
		case BMCPCommandComponent.CHANGE_MGMTCHANNEL_PROTOCOL_REQUEST: {
			setChangeMgmtChProtoReqBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CHANGE_MGMTCHANNEL_PROTOCOL_CONFIRMATION: {
			setChangeMgmtChProtoConBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CONNECT_CRYPTO: {
			setConnectCryptoBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CONNECT_CRYPTO_ECHOREQUEST: {
			setConnectCryptoEchoReqBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CONNECT_ECHOREPLY: {
			setConnectEchoReplyBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CONNECT_FULL_ENCRYPTION_REQUEST: {
			setConnectFullEncryptionReqBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CONNECT_CONFIRMATION: {
			setConnectConfirmationBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CHANNEL_BLOCK_STATUS_REQUEST: {
			setChannelBlockStatusReqBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.CHANNEL_BLOCK_STATUS_REPORT: {
			setChannelBlockStatusReportBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.OPEN_CHANNEL_REQUEST: {
			setOpenChannelRequestBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.OPEN_CHANNEL_CONFIRMATION: {
			setOpenChannelConfirmationBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.THROTTLE: {
			setThrottleBMCPCommandComponent();
			break;
		}
		case BMCPCommandComponent.ACK: {
			setAckBMCPCommandComponent();
			break;
		}
		default: {
			throw new UnsupportedBMCPCommandTypeException(commandType);
		}
		}
		commandComponent.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(commandComponent.getType());
		commandComponent.write(byteBuf);
	}
	
	@Override
	public String toString() {
		return String.format("BMCPChannelDataComponent(type=%d)", commandComponent.getType());
	}
	
	@Override
	public int length() {
		return 1 + commandComponent.length();
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { commandComponent };
	}
	
	/**
	 * sets the local {@link ChangeMgmtChProtoReqBMCPCommandComponent} to be used as command component
	 * @return the {@link ChangeMgmtChProtoReqBMCPCommandComponent} that is now used as command component
	 */
	public ChangeMgmtChProtoReqBMCPCommandComponent setChangeMgmtChProtoReqBMCPCommandComponent() {
		this.commandComponent = changeMgmtChProtoReqBMCPCommandComponent;
		return changeMgmtChProtoReqBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ChangeMgmtChProtoConBMCPCommandComponent} to be used as command component
	 * @return the {@link ChangeMgmtChProtoConBMCPCommandComponent} that is now used as command component
	 */
	public ChangeMgmtChProtoConBMCPCommandComponent setChangeMgmtChProtoConBMCPCommandComponent() {
		this.commandComponent = changeMgmtChProtoConBMCPCommandComponent;
		return changeMgmtChProtoConBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ConnectCryptoBMCPCommandComponent} to be used as command component
	 * @return the {@link ConnectCryptoBMCPCommandComponent} that is now used as command component
	 */
	public ConnectCryptoBMCPCommandComponent setConnectCryptoBMCPCommandComponent() {
		this.commandComponent = connectCryptoBMCPCommandComponent;
		return connectCryptoBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ConnectCryptoEchoReqBMCPCommandComponent} to be used as command component
	 * @return the {@link ConnectCryptoEchoReqBMCPCommandComponent} that is now used as command component
	 */
	public ConnectCryptoEchoReqBMCPCommandComponent setConnectCryptoEchoReqBMCPCommandComponent() {
		this.commandComponent = connectCryptoEchoReqBMCPCommandComponent;
		return connectCryptoEchoReqBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ConnectEchoReplyBMCPCommandComponent} to be used as command component
	 * @return the {@link ConnectEchoReplyBMCPCommandComponent} that is now used as command component
	 */
	public ConnectEchoReplyBMCPCommandComponent setConnectEchoReplyBMCPCommandComponent() {
		this.commandComponent = connectEchoReplyBMCPCommandComponent;
		return connectEchoReplyBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ConnectFullEncryptionReqBMCPCommandComponent} to be used as command component
	 * @return the {@link ConnectFullEncryptionReqBMCPCommandComponent} that is now used as command component
	 */
	public ConnectFullEncryptionReqBMCPCommandComponent setConnectFullEncryptionReqBMCPCommandComponent() {
		this.commandComponent = connectFullEncryptionReqBMCPCommandComponent;
		return connectFullEncryptionReqBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ConnectConfirmationBMCPCommandComponent} to be used as command component
	 * @return the {@link ConnectConfirmationBMCPCommandComponent} that is now used as command component
	 */
	public ConnectConfirmationBMCPCommandComponent setConnectConfirmationBMCPCommandComponent() {
		this.commandComponent = connectConfirmationBMCPCommandComponent;
		return connectConfirmationBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ChannelBlockStatusReqBMCPCommandComponent} to be used as command component
	 * @return the {@link ChannelBlockStatusReqBMCPCommandComponent} that is now used as command component
	 */
	public ChannelBlockStatusReqBMCPCommandComponent setChannelBlockStatusReqBMCPCommandComponent() {
		this.commandComponent = channelBlockStatusReqBMCPCommandComponent;
		return channelBlockStatusReqBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ChannelBlockStatusReportBMCPCommandComponent} to be used as command component
	 * @return the {@link ChannelBlockStatusReportBMCPCommandComponent} that is now used as command component
	 */
	public ChannelBlockStatusReportBMCPCommandComponent setChannelBlockStatusReportBMCPCommandComponent() {
		this.commandComponent = channelBlockStatusReportBMCPCommandComponent;
		return channelBlockStatusReportBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link OpenChannelRequestBMCPCommandComponent} to be used as command component
	 * @return the {@link OpenChannelRequestBMCPCommandComponent} that is now used as command component
	 */
	public OpenChannelRequestBMCPCommandComponent setOpenChannelRequestBMCPCommandComponent() {
		this.commandComponent = openChannelRequestBMCPCommandComponent;
		return openChannelRequestBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link OpenChannelConfirmationBMCPCommandComponent} to be used as command component
	 * @return the {@link OpenChannelConfirmationBMCPCommandComponent} that is now used as command component
	 */
	public OpenChannelConfirmationBMCPCommandComponent setOpenChannelConfirmationBMCPCommandComponent() {
		this.commandComponent = openChannelConfirmationBMCPCommandComponent;
		return openChannelConfirmationBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link ThrottleBMCPCommandComponent} to be used as command component
	 * @return the {@link ThrottleBMCPCommandComponent} that is now used as command component
	 */
	public ThrottleBMCPCommandComponent setThrottleBMCPCommandComponent() {
		this.commandComponent = throttleBMCPCommandComponent;
		return throttleBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link AckBMCPCommandComponent} to be used as command component
	 * @return the {@link AckBMCPCommandComponent} that is now used as command component
	 */
	public AckBMCPCommandComponent setAckBMCPCommandComponent() {
		this.commandComponent = ackBMCPCommandComponent;
		return ackBMCPCommandComponent;
	}
	
	/**
	 * sets the local {@link BMCPCommandComponent} to be used as command component
	 * @return the {@link BMCPCommandComponent} that is now used as command component
	 */
	public BMCPCommandComponent getBMCPCommandComponent() {
		return commandComponent;
	}
	
}
