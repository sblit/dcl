package org.dclayer.net.s2s.rev0.component;

import java.util.ArrayList;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Rev0PacketComponent;

/**
 * a {@link PacketComponent} containing a list of {@link ServiceAddressComponent}s
 */
public class ServiceAddressListComponent extends Rev0PacketComponent {
	/**
	 * the list of {@link ServiceAddressComponent}s contained in this {@link PacketComponent}
	 */
	private ArrayList<ServiceAddressComponent> addresses;
	/**
	 * the length in bytes of this {@link PacketComponent}
	 */
	private int length;
	
	/**
	 * calculates and returns the length of this {@link PacketComponent}
	 * @return the calculated length of this {@link PacketComponent}
	 */
	private int calcLength() {
		int length = 4; // 4-byte number of addresses field
		for(ServiceAddressComponent serviceAddressComponent : addresses) {
			length += serviceAddressComponent.length();
		}
		return length;
	}
	
	/**
	 * constructor called when this {@link PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
    public ServiceAddressListComponent(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
		length = calcLength();
	}
    
    /**
	 * constructor called when this {@link PacketComponent} is newly created rather than reconstructed from data
	 */
	public ServiceAddressListComponent(ArrayList<ServiceAddressComponent> addresses) {
		this.addresses = addresses;
		length = calcLength();
	}

	/**
	 * @return the list of {@link ServiceAddressComponent}s contained in this {@link PacketComponent}
	 */
    public ArrayList<ServiceAddressComponent> getAddresses() {
    	return addresses;
    }
    
	@Override
	public String toString() {
		return String.format("ServiceAddressListComponent(numaddresses=%d)", addresses.size());
	}

	@Override
	public PacketComponent[] getChildren() {
		return addresses.toArray(new PacketComponent[addresses.size()]);
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		long unsignedcount = byteBuf.read32();
		int count = (int) Math.min(Integer.MAX_VALUE, unsignedcount);
		addresses = new ArrayList<ServiceAddressComponent>(count);
		for(int i = 0; i < count; i++) addresses.add(new ServiceAddressComponent(byteBuf));
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write32(addresses.size());
		for(ServiceAddressComponent address : addresses) {
			address.write(byteBuf);
		}
	}

	@Override
	public int length() {
		return length;
	}
	
}
