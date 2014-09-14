package org.dclayer.net.component;


import java.math.BigInteger;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;

/**
 * a {@link PacketComponent} containing a {@link BigInteger}
 */
public class BigIntComponent extends PacketComponent {
	/**
	 * the contained {@link BigInteger}
	 */
	private BigInteger bigInteger;
	
	/**
	 * the amount of bytes contained in the {@link BigInteger}
	 */
	private FlexNum dataLength;
	
	/**
	 * a buffer for reading the bytes representing the {@link BigInteger}
	 */
	private byte[] buf = null;
	
	/**
	 * constructs an empty {@link BigIntComponent} that must at least be read into once before writing from
	 */
	public BigIntComponent() {
		
	}
	
	/**
	 * constructs a {@link BigIntComponent} that contains the given {@link BigInteger}
	 * @param bigInteger the {@link BigInteger} for this {@link BigIntComponent} to contain
	 */
	public BigIntComponent(BigInteger bigInteger) {
		this.setBigInteger(bigInteger);
	}
    
    /**
     * returns the {@link BigInteger} holding the data contained in this {@link BigIntComponent}
     * @return the {@link BigInteger} holding the data contained in this {@link BigIntComponent}
     */
    public BigInteger getBigInteger() {
    	return bigInteger;
    }
    
    /**
     * sets the {@link BigInteger} for this {@link BigIntComponent} to contain
     * @param bigInteger the {@link BigInteger} for this {@link BigIntComponent} to contain
     */
    public void setBigInteger(BigInteger bigInteger) {
    	this.bigInteger = bigInteger;
    	buf = bigInteger.toByteArray();
    }

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		dataLength.read(byteBuf);
		
		if(buf == null || dataLength.getNum() != buf.length) buf = new byte[(int) dataLength.getNum()];
		byteBuf.read(buf);
		bigInteger = new BigInteger(buf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		if(buf == null) throw new BufException("cannot write empty BigInteger");
		dataLength.write(byteBuf);
		byteBuf.write(buf);
	}

	@Override
	public int length() {
		return dataLength.length() + buf.length;
	}

	@Override
	public String toString() {
		return String.format("BigIntComponent(%s)", bigInteger == null ? "null" : bigInteger.toString());
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
}
