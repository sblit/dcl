package org.dclayer.net.a2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.A2SMessageReceiver;
import org.dclayer.net.a2s.A2SRevisionSpecificMessage;
import org.dclayer.net.a2s.message.SlotAssignMessageI;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.component.NetworkTypeComponent;

public class SlotAssignMessage extends A2SRevisionSpecificMessage implements SlotAssignMessageI {

	private FlexNum slotFlexNum = new FlexNum(0, Integer.MAX_VALUE);
	private NetworkTypeComponent networkTypeComponent = new NetworkTypeComponent();
	
	private Data ownAddressData = new Data();
	private Data addressData = ownAddressData;
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		slotFlexNum.read(byteBuf);
		networkTypeComponent.read(byteBuf);
		(addressData = ownAddressData).parse(byteBuf.readSpaceTerminatedString());
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		slotFlexNum.write(byteBuf);
		byteBuf.write((byte)' ');
		networkTypeComponent.write(byteBuf);
		byteBuf.write((byte)' ');
		byteBuf.writeNonTerminatedString(addressData.toString());
	}
	
	@Override
	public int length() {
		return slotFlexNum.length() + 1 + networkTypeComponent.length() + 1 + 2*addressData.length();
	}
	
	@Override
	public String toString() {
		return String.format("SlotAssignMessage(slot=%d, address=%s)", slotFlexNum.getNum(), addressData);
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { networkTypeComponent };
	}
	
	@Override
	public byte getType() {
		return Rev0Message.SLOT_ASSIGN;
	}
	
	public NetworkTypeComponent getNetworkTypeComponent() {
		return networkTypeComponent;
	}
	
	public int getSlot() {
		return (int) slotFlexNum.getNum();
	}
	
	public void setSlot(int slot) {
		slotFlexNum.setNum(slot);
	}
	
	public void setAddressData(Data addressData) {
		this.addressData = addressData;
	}
	
	public Data getAddressData() {
		return addressData;
	}

	@Override
	public void callOnReceiveMethod(A2SMessageReceiver a2sMessageReceiver) {
		a2sMessageReceiver.onReceiveSlotAssignMessage(getSlot(), networkTypeComponent.getNetworkType(), addressData);
	}
	
}
