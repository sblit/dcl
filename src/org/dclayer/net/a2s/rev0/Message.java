package org.dclayer.net.a2s.rev0;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedMessageTypeException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.A2SRevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.message.BindMessage;
import org.dclayer.net.a2s.rev0.message.DataMessage;
import org.dclayer.net.a2s.rev0.message.KnownAddressesReplyMessage;
import org.dclayer.net.a2s.rev0.message.KnownAddressesRequestMessage;
import org.dclayer.net.a2s.rev0.message.UnbindMessage;
import org.dclayer.net.buf.ByteBuf;

/**
 * a message of revision 0
 */
public class Message extends A2SRevisionMessage {
	/**
	 * bind message type, revision 0
	 */
	public static final byte BIND = 0x00;
	/**
	 * unbind message type, revision 0
	 */
	public static final byte UNBIND = 0x01;
	/**
	 * data message type, revision 0
	 */
	public static final byte DATA = 0x02;
	/**
	 * known addresses request message, revision 0
	 */
	public static final byte KNOWN_ADDRESSES_REQUEST = 0x03;
	/**
	 * known addresses reply message, revision 0
	 */
	public static final byte KNOWN_ADDRESSES_REPLY = 0x04;

	/**
	 * the message of revision 0 this {@link Message} contains
	 */
	private Rev0Message message;

	@Override
	public byte getRevision() {
		return 0;
	}
	
	/**
	 * returns the {@link Rev0Message} this revision 0 {@link Message} contains
	 * @return the {@link Rev0Message} this revision 0 {@link Message} contains
	 */
	public Rev0Message getMessage() {
		return message;
	}
	
	/**
	 * returns the type of the contained {@link Rev0Message}
	 * @return the type of the contained {@link Rev0Message}
	 */
	public byte getType() {
		return message.getType();
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		byte type = byteBuf.read();
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
		case KNOWN_ADDRESSES_REQUEST: {
			message = new KnownAddressesRequestMessage(byteBuf);
			break;
		}
		case KNOWN_ADDRESSES_REPLY: {
			message = new KnownAddressesReplyMessage(byteBuf);
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
		message.write(byteBuf);
	}

	@Override
	public int length() {
		return 1 + message.length();
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
		throw new RuntimeException("implement me");
	}

}
