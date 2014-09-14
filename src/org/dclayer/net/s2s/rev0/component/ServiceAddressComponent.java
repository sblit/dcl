package org.dclayer.net.s2s.rev0.component;

import java.net.UnknownHostException;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.InvalidServiceAddressException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Rev0PacketComponent;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * a {@link PacketComponent} containing a {@link ServiceAddress}
 */
public class ServiceAddressComponent extends Rev0PacketComponent {
	/**
	 * the {@link ServiceAddress} contained in this {@link PacketComponent}
	 */
	private ServiceAddress serviceAddress;

	/**
	 * constructor called when this {@link PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
    public ServiceAddressComponent(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
    
    /**
	 * constructor called when this {@link PacketComponent} is newly created rather than reconstructed from data
	 */
    public ServiceAddressComponent(ServiceAddress serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		byte type = byteBuf.read();
		try {
			this.serviceAddress = ServiceAddress.make(type, byteBuf);
		} catch (UnknownHostException e) {
			Log.exception(Log.PART_NET_PARSE, this, e);
			throw new InvalidServiceAddressException();
		}
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(serviceAddress.getType());
		serviceAddress.write(byteBuf);
	}

	@Override
	public int length() {
		return 1 + serviceAddress.length();
	}

	@Override
	public String toString() {
		return String.format("ServiceAddressComponent(type=%02X, address=%s)", serviceAddress.getType(), serviceAddress.toString());
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	/**
	 * returns the {@link ServiceAddress} contained in this {@link ServiceAddressComponent}
	 * @return the {@link ServiceAddress} contained in this {@link ServiceAddressComponent}
	 */
	public ServiceAddress getServiceAddress() {
		return serviceAddress;
	}
}
