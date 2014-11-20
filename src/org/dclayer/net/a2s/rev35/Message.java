package org.dclayer.net.a2s.rev35;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedMessageTypeException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.A2SRevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev35.message.DataMessage;
import org.dclayer.net.a2s.rev35.message.GenerateKeyMessage;
import org.dclayer.net.a2s.rev35.message.JoinNetworkMessage;
import org.dclayer.net.a2s.rev35.message.SlotAssignMessage;
import org.dclayer.net.buf.ByteBuf;

/**
 * a message of revision 35
 */
public class Message extends A2SRevisionMessage {
	
	public static final int GENERATE_KEY = 0xFF & (byte)'G';
	public static final int DATA = 0xFF & (byte)'D';
	public static final int JOIN_NETWORK = 0xFF & (byte)'J';
	public static final int SLOT_ASSIGN = 0xFF & (byte)'S';

	private Rev35Message message;
	
	private Rev35Message[] messages = new Rev35Message[((int)SLOT_ASSIGN)+1]; {
		messages[GENERATE_KEY] = new GenerateKeyMessage();
		messages[DATA] = new DataMessage();
		messages[JOIN_NETWORK] = new JoinNetworkMessage();
		messages[SLOT_ASSIGN] = new SlotAssignMessage();
	}

	@Override
	public byte getRevision() {
		return 35;
	}
	
	/**
	 * returns the {@link Rev35Message} this revision 35 {@link Message} contains
	 * @return the {@link Rev35Message} this revision 35 {@link Message} contains
	 */
	public Rev35Message getMessage() {
		return message;
	}
	
	/**
	 * returns the type of the contained {@link Rev35Message}
	 * @return the type of the contained {@link Rev35Message}
	 */
	public byte getType() {
		return message.getType();
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		int type = byteBuf.readSpaceTerminatedString().toUpperCase().getBytes(ByteBuf.CHARSET_ASCII)[0] & 0xFF;
		if(type >= messages.length) {
			message = null;
		} else {
			message = messages[type];
		}
		if(message == null) {
			throw new UnsupportedMessageTypeException(type);
		}
		message.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(message.getType());
		byteBuf.write((byte)' ');
		message.write(byteBuf);
		byteBuf.write((byte)'\n');
	}

	@Override
	public int length() {
		return 3 + message.length();
	}

	@Override
	public String toString() {
		return String.format("Message(type=%02X)", message.getType());
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { message };
	}

	@Override
	public void callOnReceiveMethod(ApplicationConnection applicationConnection) {
		message.callOnReceiveMethod(applicationConnection);
	}
	
	public DataMessage setDataMessage() {
		return (DataMessage)(this.message = messages[DATA]);
	}
	
	public SlotAssignMessage setSlotAssignMessage() {
		return (SlotAssignMessage)(this.message = messages[SLOT_ASSIGN]);
	}

}
