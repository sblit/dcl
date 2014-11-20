package org.dclayer.net.a2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.buf.ByteBuf;

/**
 * a bind message of revision 35 of the application-to-service protocol
 */
public class KeyMessage extends Rev0Message {

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		
	}

	@Override
	public int length() {
		return 0; // TODO
	}

	@Override
	public String toString() {
		return "KeyMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] {  }; // TODO
	}

	@Override
	public byte getType() {
		return 0; // Message.KEY; TODO
	}
	
}
