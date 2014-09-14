package org.dclayer.net.a2s.rev0;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.buf.ByteBuf;

/**
 * super class for all message classes of revision 0 of the application-to-service protocol
 */
public abstract class Rev0Message extends Rev0PacketComponent {
	
	/**
	 * constructor called when this {@link Rev0Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public Rev0Message(ByteBuf byteBuf) throws BufException, ParseException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev0Message} is newly created rather than reconstructed from data
	 */
	public Rev0Message() {
		
	}
	
	/**
	 * returns the message type of this {@link Rev0Message}
	 * @return the message type of this {@link Rev0Message}
	 */
	public abstract byte getType();
}
