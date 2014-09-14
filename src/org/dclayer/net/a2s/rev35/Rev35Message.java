package org.dclayer.net.a2s.rev35;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.buf.ByteBuf;

/**
 * super class for all message classes of revision 35 of the application-to-service protocol
 */
public abstract class Rev35Message extends Rev35PacketComponent {
	
	/**
	 * constructor called when this {@link Rev35Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev35Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public Rev35Message(ByteBuf byteBuf) throws BufException, ParseException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev35Message} is newly created rather than reconstructed from data
	 */
	public Rev35Message() {
		
	}
	
	/**
	 * returns the message type of this {@link Rev35Message}
	 * @return the message type of this {@link Rev35Message}
	 */
	public abstract byte getType();
}
