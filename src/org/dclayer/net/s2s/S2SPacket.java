package org.dclayer.net.s2s;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Message;

/**
 * a Service-to-Service packet, containing a {@link RevisionMessage}
 */
public class S2SPacket extends PacketComponent {
	private RevisionMessage message;
	
	/**
	 * constructor called when this {@link S2SPacket} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link S2SPacket} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public S2SPacket(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link S2SPacket} is newly created with a given {@link RevisionMessage}
	 * @param message the {@link RevisionMessage} this {@link S2SPacket} will contain
	 */
	public S2SPacket(RevisionMessage message) {
		this.message = message;
	}
	
	/**
	 * returns the revision of this {@link S2SPacket}
	 * @return the revision of this {@link S2SPacket}
	 */
	public byte getRevision() {
		return message.getRevision();
	}
	
	/**
	 * returns the {@link RevisionMessage} this {@link S2SPacket} contains
	 * @return the {@link RevisionMessage} this {@link S2SPacket} contains
	 */
	public RevisionMessage getMessage() {
		return message;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		byte revision = byteBuf.read();
		if(revision != 0) throw new UnsupportedRevisionException(revision);
		message = new Message(byteBuf);
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