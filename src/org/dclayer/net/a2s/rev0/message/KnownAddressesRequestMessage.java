package org.dclayer.net.a2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.Rev0Message;

/**
 * a known addresses reply message of revision 0 of the application-to-service protocol
 */
public class KnownAddressesRequestMessage extends Rev0Message {
	
	/**
	 * the limit 4-byte-integer contained in this {@link KnownAddressesRequestMessage}
	 */
	private long limit;
	/**
	 * the offset 4-byte-integer contained in this {@link KnownAddressesRequestMessage}
	 */
	private long offset;
	
	/**
	 * constructor called when this {@link Rev0Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public KnownAddressesRequestMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev0Message} is newly created rather than reconstructed from data
	 */
	public KnownAddressesRequestMessage(long limit, long offset) {
		this.limit = limit;
		this.offset = offset;
	}
	
	/**
	 * returns the limit 4-byte-integer contained in this {@link KnownAddressesRequestMessage}
	 * @return the limit 4-byte-integer contained in this {@link KnownAddressesRequestMessage}
	 */
	public long getLimit() {
		return limit;
	}
	
	/**
	 * returns the offset 4-byte-integer contained in this {@link KnownAddressesRequestMessage}
	 * @return the offset 4-byte-integer contained in this {@link KnownAddressesRequestMessage}
	 */
	public long getOffset() {
		return offset;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		limit = byteBuf.read32();
		offset = byteBuf.read32();
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write32((int)limit);
		byteBuf.write32((int)offset);
	}

	@Override
	public int length() {
		return 8;
	}

	@Override
	public String toString() {
		return String.format("KnownAddressesRequestMessage(limit=%d, offset=%d)", limit, offset);
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public byte getType() {
		return Message.KNOWN_ADDRESSES_REQUEST;
	}
	
}
