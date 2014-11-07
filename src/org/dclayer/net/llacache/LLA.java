package org.dclayer.net.llacache;

import java.net.SocketAddress;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedServiceAddressTypeException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.ByteBuf;

public abstract class LLA {
	
	public static final byte TYPE_INETSOCKET = 0x00;
	public static final byte TYPE_INET4SOCKET = 0x01;
	public static final byte TYPE_INET6SOCKET = 0x02;
	
	public static LLA fromByteBuf(ByteBuf byteBuf) throws BufException, ParseException {
		byte type = byteBuf.read();
		switch(type) {
		case TYPE_INETSOCKET:
		case TYPE_INET4SOCKET:
		case TYPE_INET6SOCKET: {
			return new InetSocketLLA(type, byteBuf);
		}
		default: {
			throw new UnsupportedServiceAddressTypeException(type);
		}
		}
	}
	
	//
	
	@Override
	public String toString() {
		return getSocketAddress().toString();
	}
	
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(getData());
	}
	
	/**
	 * @return a {@link Data} object containing the type and the information of this LLA
	 */
	public abstract Data getData();
	public abstract int length();
	public abstract SocketAddress getSocketAddress();
	
}
