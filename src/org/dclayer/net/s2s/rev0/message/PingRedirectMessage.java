package org.dclayer.net.s2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.Rev0Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;

/**
 * a ping redirect message of revision 0 of the service-to-service protocol
 */
public class PingRedirectMessage extends Rev0Message {
	/**
	 * the {@link ServiceAddressComponent} contained in this {@link PingRedirectMessage}
	 */
	private ServiceAddressComponent toAddress;
	/**
	 * the {@link DataComponent} contained in this {@link PingRedirectMessage}
	 */
	private DataComponent data;
	
	/**
	 * constructor called when this {@link Rev0Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public PingRedirectMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev0Message} is newly created rather than reconstructed from data
	 */
	public PingRedirectMessage(ServiceAddressComponent toAddress, DataComponent data) {
		this.toAddress = toAddress;
		this.data = data;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		toAddress = new ServiceAddressComponent(byteBuf);
		data = new DataComponent(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		toAddress.write(byteBuf);
		data.write(byteBuf);
	}

	@Override
	public int length() {
		return toAddress.length() + data.length();
	}

	@Override
	public String toString() {
		return "PingRedirectMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { toAddress, data };
	}
	
	/**
	 * @return the {@link ServiceAddressComponent} contained in this {@link PingRedirectMessage}
	 */
	public ServiceAddressComponent getServiceAddressComponent() {
		return toAddress;
	}
	
	/**
	 * @return the {@link DataComponent} contained in this {@link PingRedirectMessage}
	 */
	public DataComponent getDataComponent() {
		return data;
	}

	@Override
	public byte getType() {
		return Message.REDIRECT_PING;
	}
	
}
