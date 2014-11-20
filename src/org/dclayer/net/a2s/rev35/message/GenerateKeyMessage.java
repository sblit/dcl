package org.dclayer.net.a2s.rev35.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev35.Message;
import org.dclayer.net.a2s.rev35.Rev35Message;
import org.dclayer.net.buf.ByteBuf;

public class GenerateKeyMessage extends Rev35Message {
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		
	}
	
	@Override
	public int length() {
		return 0;
	}
	
	@Override
	public String toString() {
		return "GenerateKeyMessage";
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	@Override
	public byte getType() {
		return Message.GENERATE_KEY;
	}

	@Override
	public void callOnReceiveMethod(ApplicationConnection applicationConnection) {
		applicationConnection.onReceiveGenerateKeyMessage();
	}
	
}
