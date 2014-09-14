package org.dclayer.net.s2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.Rev0Message;
import org.dclayer.net.s2s.rev0.component.ServiceAddressListComponent;

/**
 * a known addresses reply message of revision 0 of the service-to-service protocol
 */
public class KnownAddressesReplyMessage extends Rev0Message {
	/**
	 * the {@link ServiceAddressListComponent} contained in this {@link KnownAddressesReplyMessage}
	 */
	private ServiceAddressListComponent serviceAddressListComponent;
	
	/**
	 * constructor called when this {@link Rev0Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public KnownAddressesReplyMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev0Message} is newly created rather than reconstructed from data
	 */
	public KnownAddressesReplyMessage(ServiceAddressListComponent serviceAddressListComponent) {
		this.serviceAddressListComponent = serviceAddressListComponent;
	}
	
	/**
	 * returns the {@link ServiceAddressListComponent} contained in this {@link KnownAddressesReplyMessage}
	 * @return
	 */
	public ServiceAddressListComponent getServiceAddressListComponent() {
		return serviceAddressListComponent;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		serviceAddressListComponent = new ServiceAddressListComponent(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		serviceAddressListComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return serviceAddressListComponent.length();
	}

	@Override
	public String toString() {
		return "KnownAddressesReplyMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { serviceAddressListComponent };
	}

	@Override
	public byte getType() {
		return Message.KNOWN_ADDRESSES_REPLY;
	}
	
}
