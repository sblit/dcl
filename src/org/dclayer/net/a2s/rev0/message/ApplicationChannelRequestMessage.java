package org.dclayer.net.a2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.A2SMessageReceiver;
import org.dclayer.net.a2s.A2SRevisionSpecificMessage;
import org.dclayer.net.a2s.message.ApplicationChannelRequestMessageI;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.component.KeyComponent;

public class ApplicationChannelRequestMessage extends A2SRevisionSpecificMessage implements ApplicationChannelRequestMessageI {
	
	private FlexNum networkSlotFlexNum = new FlexNum(0, Integer.MAX_VALUE);
	private FlexNum channelSlotFlexNum = new FlexNum(0, Integer.MAX_VALUE);
	private KeyComponent remotePublicKeyComponent = new KeyComponent();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		networkSlotFlexNum.read(byteBuf);
		channelSlotFlexNum.read(byteBuf);
		remotePublicKeyComponent.read(byteBuf);
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		networkSlotFlexNum.write(byteBuf);
		channelSlotFlexNum.write(byteBuf);
		remotePublicKeyComponent.write(byteBuf);
	}
	
	@Override
	public int length() {
		return networkSlotFlexNum.length() + remotePublicKeyComponent.length();
	}
	
	@Override
	public String toString() {
		return String.format("ApplicationChannelRequestMessage(slot=%d)", networkSlotFlexNum.getNum());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { remotePublicKeyComponent };
	}
	
	@Override
	public byte getType() {
		return Rev0Message.APPLICATION_CHANNEL_REQUEST;
	}
	
	@Override
	public KeyComponent getKeyComponent() {
		return remotePublicKeyComponent;
	}

	@Override
	public void callOnReceiveMethod(A2SMessageReceiver a2sMessageReceiver) {
		a2sMessageReceiver.onReceiveApplicationChannelRequestMessage((int) networkSlotFlexNum.getNum(), (int) channelSlotFlexNum.getNum(), remotePublicKeyComponent.getKeyComponent());
	}

	@Override
	public int getMessageRevision() {
		return 0;
	}

	@Override
	public int getNetworkSlot() {
		return (int) networkSlotFlexNum.getNum();
	}

	@Override
	public void setNetworkSlot(int networkSlot) {
		networkSlotFlexNum.setNum(networkSlot);
	}

	@Override
	public int getChannelSlot() {
		return (int) channelSlotFlexNum.getNum();
	}

	@Override
	public void setChannelSlot(int channelSlot) {
		channelSlotFlexNum.setNum(channelSlot);
	}
	
}
