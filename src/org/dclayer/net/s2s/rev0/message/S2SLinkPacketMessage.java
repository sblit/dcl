package org.dclayer.net.s2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.Rev0Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;

/**
 * a link packet message of revision 0 of the service-to-service protocol
 */
public class S2SLinkPacketMessage extends Rev0Message {
	/**
	 * the data that represents this link packet
	 */
	private DataComponent dataComponent;
	
	/**
	 * constructor called when this {@link S2SLinkPacketMessage} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link S2SLinkPacketMessage} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public S2SLinkPacketMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link S2SLinkPacketMessage} is newly created rather than reconstructed from data
	 * @param dataComponent the data that represents this link packet
	 */
	public S2SLinkPacketMessage(DataComponent dataComponent) {
		this.dataComponent = dataComponent;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		dataComponent = new DataComponent(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		dataComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return dataComponent.length();
	}

	@Override
	public String toString() {
		return "S2SLinkPacketMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { dataComponent };
	}
	
	/**
	 * returns the {@link DataComponent} of this {@link S2SLinkPacketMessage}
	 * @return the {@link DataComponent} of this {@link S2SLinkPacketMessage}
	 */
	public DataComponent getDataComponent() {
		return dataComponent;
	}

	@Override
	public byte getType() {
		return Message.LINK_PACKET;
	}
	
}
