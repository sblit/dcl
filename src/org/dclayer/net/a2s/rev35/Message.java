package org.dclayer.net.a2s.rev35;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedMessageTypeException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.rev35.message.BindMessage;
import org.dclayer.net.a2s.rev35.message.DataMessage;
import org.dclayer.net.a2s.rev35.message.UnbindMessage;
import org.dclayer.net.buf.ByteBuf;

/**
 * a message of revision 35
 */
public class Message extends RevisionMessage {
	/**
	 * bind message type, revision 35
	 */
	public static final byte BIND = (byte)'B';
	/**
	 * unbind message type, revision 35
	 */
	public static final byte UNBIND = (byte)'U';
	/**
	 * data message type, revision 35
	 */
	public static final byte DATA = (byte)'D';

	/**
	 * the message of revision 0 this {@link Message} contains
	 */
	private Rev35Message message;

	/**
	 * creates a revision 35 {@link Message} object, reconstructed from the given {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} to reconstruct this revision 35 {@link Message} from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public Message(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * creates a revision 35 {@link Message} object, reconstructed from the given {@link Rev35Message}
	 * @param message the {@link Rev35Message} that this revision 35 {@link Message} contains
	 */
	public Message(Rev35Message message) {
		this.message = message;
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
		byte type = byteBuf.readSpaceTerminatedString().toUpperCase().getBytes(ByteBuf.CHARSET_ASCII)[0];
		switch (type) {
		case BIND: {
			message = new BindMessage(byteBuf);
			break;
		}
		case UNBIND: {
			message = new UnbindMessage(byteBuf);
			break;
		}
		case DATA: {
			message = new DataMessage(byteBuf);
			break;
		}
		default: {
			throw new UnsupportedMessageTypeException(type);
		}
		}
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
	public int getMessageTypeId() {
		return message.getType();
	}

}
