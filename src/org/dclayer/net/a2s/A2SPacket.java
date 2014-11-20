package org.dclayer.net.a2s;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.buf.ByteBuf;

/**
 * an Application-to-Service packet, containing a {@link RevisionMessage}
 */
public class A2SPacket extends PacketComponent {
	
	private A2SRevisionMessage revisionMessage;
	
	private A2SRevisionMessage[] revisionMessages = new A2SRevisionMessage[36]; {
		revisionMessages[0] = new org.dclayer.net.a2s.rev0.Message();
		revisionMessages[35] = new org.dclayer.net.a2s.rev35.Message();
	}
	
	public A2SRevisionMessage getA2SRevisionMessage() {
		return revisionMessage;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		int revision = 0xFF & byteBuf.read();
		if(revision >= revisionMessages.length) {
			revisionMessage = null;
		} else {
			revisionMessage = revisionMessages[revision];
		}
		if(revisionMessage == null) {
			throw new UnsupportedRevisionException(revision);
		}
		revisionMessage.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(revisionMessage.getRevision());
		revisionMessage.write(byteBuf);
	}

	@Override
	public int length() {
		return 1 + revisionMessage.length();
	}

	@Override
	public String toString() {
		return "A2SPacket";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { revisionMessage };
	}
	
	public void callOnReceiveMethod(ApplicationConnection applicationConnection) {
		revisionMessage.callOnReceiveMethod(applicationConnection);
	}
	
	public org.dclayer.net.a2s.rev0.Message setRevision0Message() {
		return (org.dclayer.net.a2s.rev0.Message)(this.revisionMessage = revisionMessages[0]);
	}
	
	public org.dclayer.net.a2s.rev35.Message setRevision35Message() {
		return (org.dclayer.net.a2s.rev35.Message)(this.revisionMessage = revisionMessages[35]);
	}
	
}