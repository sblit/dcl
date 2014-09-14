package org.dclayer.net.s2s.rev0;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedMessageTypeException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.message.KnownAddressesReplyMessage;
import org.dclayer.net.s2s.rev0.message.KnownAddressesRequestMessage;
import org.dclayer.net.s2s.rev0.message.PingMessage;
import org.dclayer.net.s2s.rev0.message.PingRedirectMessage;
import org.dclayer.net.s2s.rev0.message.PongMessage;
import org.dclayer.net.s2s.rev0.message.PongRedirectMessage;
import org.dclayer.net.s2s.rev0.message.S2SApplicationDataMessage;
import org.dclayer.net.s2s.rev0.message.S2SLinkPacketMessage;

/**
 * a message of revision 0
 */
public class Message extends RevisionMessage {
	/**
	 * ping message type, revision 0
	 */
	public static final byte PING = 0x00;
	/**
	 * pong message type, revision 0
	 */
	public static final byte PONG = 0x01;
	/**
	 * known addresses request message type, revision 0
	 */
	public static final byte KNOWN_ADDRESSES_REQUEST = 0x02;
	/**
	 * known addresses reply message type, revision 0
	 */
	public static final byte KNOWN_ADDRESSES_REPLY = 0x03;
	/**
	 * redirect ping message type, revision 0
	 */
	public static final byte REDIRECT_PING = 0x04;
	/**
	 * redirect pong message type, revision 0
	 */
	public static final byte REDIRECT_PONG = 0x05;
	/**
	 * application data message type, revision 0
	 */
	public static final byte APPLICATION_DATA = 0x06;
	/**
	 * link packet message type, revision 0
	 */
	public static final byte LINK_PACKET = 0x07;
	/**
	 * the number of different message types
	 */
	public static final byte NUM_MESSAGETYPES = 0x08;

	/**
	 * the message of revision 0 this {@link Message} contains
	 */
	private Rev0Message message;

	/**
	 * creates a revision 0 {@link Message} object, reconstructed from the given {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} to reconstruct this revision 0 {@link Message} from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public Message(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * creates a revision 0 {@link Message} object, reconstructed from the given {@link Rev0Message}
	 * @param message the {@link Rev0Message} that this revision 0 {@link Message} contains
	 */
	public Message(Rev0Message message) {
		this.message = message;
	}

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
		case PING: {
			message = new PingMessage(byteBuf);
			break;
		}
		case PONG: {
			message = new PongMessage(byteBuf);
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
		case REDIRECT_PING: {
			message = new PingRedirectMessage(byteBuf);
			break;
		}
		case REDIRECT_PONG: {
			message = new PongRedirectMessage(byteBuf);
			break;
		}
		case APPLICATION_DATA: {
			message = new S2SApplicationDataMessage(byteBuf);
			break;
		}
		case LINK_PACKET: {
			message = new S2SLinkPacketMessage(byteBuf);
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
	public int getMessageTypeId() {
		return message.getType();
	}

}
