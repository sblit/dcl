package org.dclayer.net.a2s.rev35;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;

/**
 * a {@link PacketComponent} of revision 35
 */
public abstract class Rev35PacketComponent extends PacketComponent {

	/**
	 * constructor called when this {@link Rev35PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev35PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public Rev35PacketComponent(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}

	/**
	 * constructor called when this {@link Rev35PacketComponent} is newly created rather than reconstructed from data
	 */
	public Rev35PacketComponent() {
	}

}
