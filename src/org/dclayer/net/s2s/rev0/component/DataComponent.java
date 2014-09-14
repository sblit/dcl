package org.dclayer.net.s2s.rev0.component;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Rev0PacketComponent;

/**
 * a {@link PacketComponent} containing data
 */
public class DataComponent extends Rev0PacketComponent {
	/**
	 * the contained data
	 */
	private byte[] data;

	/**
	 * constructor called when this {@link PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
    public DataComponent(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
    
    /**
	 * constructor called when this {@link PacketComponent} is newly created rather than reconstructed from data
	 */
    public DataComponent(byte[] data) {
    	this.data = data;
    }
    
    /**
     * returns the byte array holding the data contained in this {@link DataComponent}
     * @return the byte array holding the data contained in this {@link DataComponent}
     */
    public byte[] getData() {
    	return data;
    }

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		int length = byteBuf.read16();
		data = new byte[length];
		byteBuf.read(data);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write16(data.length);
		byteBuf.write(data);
	}

	@Override
	public int length() {
		return 2 + data.length;
	}

	@Override
	public String toString() {
		return String.format("DataComponent(len=%d)", data.length);
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
}
