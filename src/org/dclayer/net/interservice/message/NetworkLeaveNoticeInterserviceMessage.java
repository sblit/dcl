package org.dclayer.net.interservice.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.interservice.InterserviceChannel;
import org.dclayer.net.interservice.InterserviceMessage;
import org.dclayer.net.interservice.InterservicePacket;

public class NetworkLeaveNoticeInterserviceMessage extends InterserviceMessage {
	
	private FlexNum slotFlexNum = new FlexNum(0, Integer.MAX_VALUE);

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		slotFlexNum.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		slotFlexNum.write(byteBuf);
	}

	@Override
	public int length() {
		return slotFlexNum.length();
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public String toString() {
		return String.format("NetworkLeaveNoticeInterserviceMessage(slot=%d)", slotFlexNum.getNum());
	}
	
	public int getSlot() {
		return (int) slotFlexNum.getNum();
	}
	
	public void setSlot(int slot) {
		slotFlexNum.setNum(slot);
	}

	@Override
	public int getTypeId() {
		return InterservicePacket.NETWORK_JOIN_NOTICE;
	}

	@Override
	public void callOnReceiveMethod(InterserviceChannel interserviceChannel) {
		interserviceChannel.onReceiveNetworkLeaveNoticeInterserviceMessage(this);
	}
	
}
