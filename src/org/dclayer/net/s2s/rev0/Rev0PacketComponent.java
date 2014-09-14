package org.dclayer.net.s2s.rev0;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;

/**
 * a {@link PacketComponent} of revision 0
 */
public abstract class Rev0PacketComponent extends PacketComponent {

	/**
	 * constructor called when this {@link Rev0PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public Rev0PacketComponent(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}

	/**
	 * constructor called when this {@link Rev0PacketComponent} is newly created rather than reconstructed from data
	 */
	public Rev0PacketComponent() {
	}

}
