package org.dclayer.net.a2s.rev0;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedMessageTypeException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.A2SMessage;
import org.dclayer.net.a2s.A2SMessageReceiver;
import org.dclayer.net.a2s.A2SRevisionSpecificMessage;
import org.dclayer.net.a2s.message.AddressPublicKeyMessageI;
import org.dclayer.net.a2s.message.JoinDefaultNetworksMessageI;
import org.dclayer.net.a2s.rev0.message.AddressPublicKeyMessage;
import org.dclayer.net.a2s.rev0.message.DataMessage;
import org.dclayer.net.a2s.rev0.message.GenerateKeyMessage;
import org.dclayer.net.a2s.rev0.message.JoinDefaultNetworksMessage;
import org.dclayer.net.a2s.rev0.message.JoinNetworkMessage;
import org.dclayer.net.a2s.rev0.message.RevisionMessage;
import org.dclayer.net.a2s.rev0.message.SlotAssignMessage;
import org.dclayer.net.buf.ByteBuf;

/**
 * a message of revision 35
 */
public class Rev0Message extends A2SMessage {

	public static final int REVISION = 0;
	public static final int GENERATE_KEY = 1;
	public static final int JOIN_NETWORK = 2;
	public static final int SLOT_ASSIGN = 3;
	public static final int DATA = 4;
	public static final int ADDRESS_PUBLIC_KEY = 5;
	public static final int JOIN_DEFAULT_NETWORKS = 6;

	private A2SRevisionSpecificMessage message;
	
	private A2SRevisionSpecificMessage[] messages = new A2SRevisionSpecificMessage[] {
		new RevisionMessage(),
		new GenerateKeyMessage(),
		new JoinNetworkMessage(),
		new SlotAssignMessage(),
		new DataMessage(),
		new AddressPublicKeyMessage(),
		new JoinDefaultNetworksMessage()
	};

	@Override
	public byte getRevision() {
		return 0;
	}
	
	/**
	 * returns the {@link A2SRevisionSpecificMessage} this revision 35 {@link Rev0Message} contains
	 * @return the {@link A2SRevisionSpecificMessage} this revision 35 {@link Rev0Message} contains
	 */
	public A2SRevisionSpecificMessage getMessage() {
		return message;
	}
	
	/**
	 * returns the type of the contained {@link A2SRevisionSpecificMessage}
	 * @return the type of the contained {@link A2SRevisionSpecificMessage}
	 */
	public byte getType() {
		return message.getType();
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		int type = 0xFF & byteBuf.read();
		if(type < messages.length) {
			message = messages[type];
		} else {
			throw new UnsupportedMessageTypeException(type);
		}
		message.read(byteBuf);
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
		return String.format("Rev0Message(type=%02X)", message.getType());
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { message };
	}

	@Override
	public void callOnReceiveMethod(A2SMessageReceiver a2sMessageReceiver) {
		message.callOnReceiveMethod(a2sMessageReceiver);
	}
	
	public RevisionMessage setRevisionMessage() {
		return (RevisionMessage)(this.message = messages[REVISION]);
	}
	
	public DataMessage setDataMessage() {
		return (DataMessage)(this.message = messages[DATA]);
	}
	
	public SlotAssignMessage setSlotAssignMessage() {
		return (SlotAssignMessage)(this.message = messages[SLOT_ASSIGN]);
	}

	@Override
	public AddressPublicKeyMessageI setAddressPublicKeyMessage() {
		return (AddressPublicKeyMessage)(this.message = messages[ADDRESS_PUBLIC_KEY]);
	}

	@Override
	public JoinDefaultNetworksMessageI setJoinDefaultNetworksMessage() {
		return (JoinDefaultNetworksMessage)(this.message = messages[JOIN_DEFAULT_NETWORKS]);
	}

}
