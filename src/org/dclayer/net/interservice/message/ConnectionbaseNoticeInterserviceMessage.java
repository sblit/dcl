package org.dclayer.net.interservice.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.interservice.InterserviceChannel;
import org.dclayer.net.interservice.InterserviceMessage;
import org.dclayer.net.interservice.InterservicePacket;

public class ConnectionbaseNoticeInterserviceMessage extends InterserviceMessage {
	
	//
	
	private byte connectionBase;
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		this.connectionBase = byteBuf.read();
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(connectionBase);
	}

	@Override
	public int length() {
		return 1;
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public String toString() {
		return String.format("ConnectionbaseNoticeInterserviceMessage(connectionBase=%d)", connectionBase);
	}
	
	public byte getConnectionBase() {
		return connectionBase;
	}
	
	public void setConnectionBase(byte connectionBase) {
		this.connectionBase = connectionBase;
	}
	
	@Override
	public int getTypeId() {
		return InterservicePacket.CONNECTIONBASE_NOTICE;
	}
	
	@Override
	public void callOnReceiveMethod(InterserviceChannel interserviceChannel) {
		interserviceChannel.onReceiveConnectionbaseNoticeInterserviceMessage(this);
	}
	
}
