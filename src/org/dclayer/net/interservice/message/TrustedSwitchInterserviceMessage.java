package org.dclayer.net.interservice.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.KeyComponent;
import org.dclayer.net.interservice.InterserviceChannel;
import org.dclayer.net.interservice.InterserviceMessage;
import org.dclayer.net.interservice.InterservicePacket;

public class TrustedSwitchInterserviceMessage extends InterserviceMessage {
	
	private KeyComponent keyComponent = new KeyComponent();

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		keyComponent.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		keyComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return keyComponent.length();
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { keyComponent };
	}

	@Override
	public String toString() {
		return "TrustedSwitchInterserviceMessage";
	}
	
	public KeyComponent getKeyComponent() {
		return keyComponent;
	}

	@Override
	public int getTypeId() {
		return InterservicePacket.TRUSTED_SWITCH;
	}

	@Override
	public void callOnReceiveMethod(InterserviceChannel interserviceChannel) {
		interserviceChannel.onReceiveTrustedSwitchInterserviceMessage(this);
	}
	
}
