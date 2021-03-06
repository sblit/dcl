package org.dclayer.net.interservice.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.interservice.InterserviceChannel;
import org.dclayer.net.interservice.InterserviceMessage;
import org.dclayer.net.interservice.InterservicePacket;

public class CryptoChallengeRequestInterserviceMessage extends InterserviceMessage {
	
	private FlexNum addressSlotFlexNum = new FlexNum(0, Integer.MAX_VALUE);
	private DataComponent plainData = new DataComponent();

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		addressSlotFlexNum.read(byteBuf);
		plainData.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		addressSlotFlexNum.write(byteBuf);
		plainData.write(byteBuf);
	}

	@Override
	public int length() {
		return addressSlotFlexNum.length() + plainData.length();
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { plainData };
	}

	@Override
	public String toString() {
		return String.format("CryptoChallengeRequestInterserviceMessage(addressSlot=%d)", addressSlotFlexNum.getNum());
	}
	
	public int getAddressSlot() {
		return (int) addressSlotFlexNum.getNum();
	}
	
	public void setAddressSlot(int slot) {
		addressSlotFlexNum.setNum(slot);
	}
	
	public DataComponent getDataComponent() {
		return plainData;
	}

	@Override
	public int getTypeId() {
		return InterservicePacket.CRYPTO_CHALLENGE_REQUEST;
	}

	@Override
	public void callOnReceiveMethod(InterserviceChannel interserviceChannel) {
		interserviceChannel.onReceiveCryptoChallengeRequestInterserviceMessage(this);
	}
	
}
