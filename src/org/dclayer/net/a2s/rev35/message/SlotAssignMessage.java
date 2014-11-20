package org.dclayer.net.a2s.rev35.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev35.Message;
import org.dclayer.net.a2s.rev35.Rev35Message;
import org.dclayer.net.a2s.rev35.component.NetworkTypeComponent;
import org.dclayer.net.a2s.rev35.component.NumberComponent;
import org.dclayer.net.buf.ByteBuf;

public class SlotAssignMessage extends Rev35Message {

	private NumberComponent slotNumberComponent = new NumberComponent();
	private NetworkTypeComponent networkTypeComponent = new NetworkTypeComponent();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		slotNumberComponent.read(byteBuf);
		networkTypeComponent.read(byteBuf);
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		slotNumberComponent.write(byteBuf);
		byteBuf.write((byte)' ');
		networkTypeComponent.write(byteBuf);
	}
	
	@Override
	public int length() {
		return slotNumberComponent.length() + networkTypeComponent.length();
	}
	
	@Override
	public String toString() {
		return String.format("SlotAssignMessage(slot=%d)", slotNumberComponent.getNumber());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { networkTypeComponent };
	}
	
	@Override
	public byte getType() {
		return Message.SLOT_ASSIGN;
	}
	
	public NetworkTypeComponent getNetworkTypeComponent() {
		return networkTypeComponent;
	}
	
	public int getSlot() {
		return slotNumberComponent.getNumber();
	}
	
	public void setSlot(int slot) {
		slotNumberComponent.setNumber(slot);
	}

	@Override
	public void callOnReceiveMethod(ApplicationConnection applicationConnection) {
		applicationConnection.onReceiveSlotAssignMessage(getSlot(), networkTypeComponent.getNetworkType());
	}
	
}
