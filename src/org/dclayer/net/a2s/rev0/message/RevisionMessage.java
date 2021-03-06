package org.dclayer.net.a2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.A2SMessageReceiver;
import org.dclayer.net.a2s.A2SRevisionSpecificMessage;
import org.dclayer.net.a2s.message.RevisionMessageI;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;

public class RevisionMessage extends A2SRevisionSpecificMessage implements RevisionMessageI {
	
	public FlexNum revisionFlexNum = new FlexNum(0, Integer.MAX_VALUE);
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		revisionFlexNum.read(byteBuf);
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		revisionFlexNum.write(byteBuf);
	}
	
	@Override
	public int length() {
		return revisionFlexNum.length();
	}
	
	@Override
	public String toString() {
		return String.format("RevisionMessage(revision=%d)", revisionFlexNum.getNum());
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	@Override
	public byte getType() {
		return Rev0Message.REVISION;
	}

	@Override
	public void callOnReceiveMethod(A2SMessageReceiver a2sMessageReceiver) {
		a2sMessageReceiver.onReceiveRevisionMessage((int) revisionFlexNum.getNum());
	}

	@Override
	public void setRevision(int revision) {
		revisionFlexNum.setNum(revision);
	}

	@Override
	public int getRevision() {
		return (int) revisionFlexNum.getNum();
	}

	@Override
	public int getMessageRevision() {
		return 0;
	}
	
}
