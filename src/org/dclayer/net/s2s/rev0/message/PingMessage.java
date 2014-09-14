package org.dclayer.net.s2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.Rev0Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;

/**
 * a ping message of revision 0 of the service-to-service protocol
 */
public class PingMessage extends Rev0Message {
	/**
	 * the {@link DataComponent} contained in this {@link PingMessage}
	 */
	private DataComponent data;
	
	/**
	 * constructor called when this {@link Rev0Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public PingMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev0Message} is newly created rather than reconstructed from data
	 */
	public PingMessage(DataComponent data) {
		this.data = data;
	}
	
	/**
	 * @return the {@link DataComponent} contained in this {@link PingMessage}
	 */
	public DataComponent getDataComponent() {
		return data;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		data = new DataComponent(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		data.write(byteBuf);
	}

	@Override
	public int length() {
		return data.length();
	}

	@Override
	public String toString() {
		return "PingMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { data };
	}

	@Override
	public byte getType() {
		return Message.PING;
	}
	
}
