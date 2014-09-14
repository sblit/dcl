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
	/**
	 * the {@link RevisionMessage} that this {@link A2SPacket} contains
	 */
	private RevisionMessage message;
	
	/**
	 * constructor called when this {@link A2SPacket} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link A2SPacket} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public A2SPacket(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link A2SPacket} is newly created with a given {@link RevisionMessage}
	 * @param message the {@link RevisionMessage} this {@link A2SPacket} will contain
	 */
	public A2SPacket(RevisionMessage message) {
		this.message = message;
	}
	
	/**
	 * returns the revision of this {@link A2SPacket}
	 * @return the revision of this {@link A2SPacket}
	 */
	public byte getRevision() {
		return message.getRevision();
	}
	
	/**
	 * returns the {@link RevisionMessage} this {@link A2SPacket} contains
	 * @return the {@link RevisionMessage} this {@link A2SPacket} contains
	 */
	public RevisionMessage getMessage() {
		return message;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		byte revision = byteBuf.read();
		if(revision == 0) message = new org.dclayer.net.a2s.rev0.Message(byteBuf);
		else if(revision == 35) message = new org.dclayer.net.a2s.rev35.Message(byteBuf);
		else throw new UnsupportedRevisionException(revision);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(message.getRevision());
		message.write(byteBuf);
	}

	@Override
	public int length() {
		return 1 + message.length();
	}

	@Override
	public String toString() {
		return String.format("Packet(rev=%d)", message.getRevision());
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { message };
	}
}