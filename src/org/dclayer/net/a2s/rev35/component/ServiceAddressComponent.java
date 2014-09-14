package org.dclayer.net.a2s.rev35.component;

import java.net.UnknownHostException;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.InvalidServiceAddressException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.rev35.Rev35PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * a {@link PacketComponent} containing a {@link ServiceAddress}
 */
public class ServiceAddressComponent extends Rev35PacketComponent {
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
		String typeString = byteBuf.readNonTerminatedString(5).toLowerCase();
		String addrString = byteBuf.readSpaceTerminatedString();
		byte[] data;
		byte type = 0;
		if(typeString.startsWith("ipv4")) {
			type = ServiceAddress.IPV4_WITH_PORT;
			data = new byte[6];
			String[] addrParts = addrString.split(":");
			if(addrParts.length != 2) throw new ParseException("invalid IPv4 serviceaddress");
			String hostString = addrParts[0], portString = addrParts[1];
			String[] vals = hostString.split("\\.");
			if(vals.length != 4) throw new ParseException("invalid IPv4 host address");
			for(int i = 0; i < 4; i++) {
				data[i] = (byte)Integer.parseInt(vals[i]);				
			}
			int port = Integer.parseInt(portString);
			data[4] = (byte) ((port >> 8) & 0xFF);
			data[5] = (byte) (port & 0xFF);
			
		} else if(typeString.startsWith("ipv6")) {
			type = ServiceAddress.IPV6_WITH_PORT;
			data = new byte[18];
			String[] vals = addrString.split(":");
			if(vals.length != 17) throw new ParseException("invalid IPv6 serviceaddress");
			for(int i = 0; i < 16; i++) {
				data[i] = (byte)Integer.parseInt(vals[i], 16);
			}
			int port = Integer.parseInt(vals[16]);
			data[16] = (byte) ((port >> 8) & 0xFF);
			data[17] = (byte) (port & 0xFF);
			
		} else {
			throw new InvalidServiceAddressException();
		}
		try {
			this.serviceAddress = ServiceAddress.make(type, new DataByteBuf(data));
		} catch (UnknownHostException e) {
			Log.exception(Log.PART_NET_PARSE, this, e);
			throw new InvalidServiceAddressException();
		}
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byte type = serviceAddress.getType();
		String typeString;
		if(type == ServiceAddress.IPV6_WITH_PORT) {
			typeString = "IPv6 ";
		} else {
			typeString = "IPv4 ";
		}
		byteBuf.writeNonTerminatedString(typeString);
		byteBuf.writeNonTerminatedString(serviceAddress.toString());
		byteBuf.write((byte)' ');
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
