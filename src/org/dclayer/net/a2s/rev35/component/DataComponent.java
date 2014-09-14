package org.dclayer.net.a2s.rev35.component;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.rev35.Rev35PacketComponent;
import org.dclayer.net.buf.ByteBuf;

/**
 * a {@link PacketComponent} containing data
 */
public class DataComponent extends Rev35PacketComponent {
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
		String string = byteBuf.readTextModeString();
		data = string.getBytes(ByteBuf.CHARSET_ASCII);
		byteBuf.readTilSpaceOrEOL();
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.writeTextModeString(new String(data, ByteBuf.CHARSET_ASCII));
		byteBuf.write((byte)' ');
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
