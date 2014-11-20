package org.dclayer.net.network;

import org.dclayer.apbr.APBRNetworkType;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.MalformedNetworkDescriptorException;
import org.dclayer.exception.net.parse.NotImplementedParseException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedNetworkIdentifierException;
import org.dclayer.net.Data;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.address.Address;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.component.NetworkPayload;
import org.dclayer.net.network.properties.CommonNetworkPayloadProperties;
import org.dclayer.net.network.slot.NetworkSlot;
import org.dclayer.net.routing.RoutingTable;

public abstract class NetworkType<T extends NetworkType> extends PacketComponent {
	
	public static final String IDENTIFIER_APBR = "org.dclayer.apbr";
	
	public static NetworkType fromByteBuf(ByteBuf byteBuf) throws BufException, ParseException {
		String descriptor = byteBuf.readString();
		String[] parts = descriptor.split(" ", 2);
		if(parts.length < 2) throw new MalformedNetworkDescriptorException(descriptor);
		String identifier = parts[0];
		String attributeString = parts[1];
		return make(identifier, attributeString);
	}
	
	public static NetworkType make(String identifier, String attributeString) throws ParseException {
		switch(identifier) {
		case IDENTIFIER_APBR: {
			return new APBRNetworkType(attributeString);
		}
		default: {
			throw new UnsupportedNetworkIdentifierException(identifier);
		}
		}
	}
	
	//
	
	private String identifier;
	
	public NetworkType(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public final void write(ByteBuf byteBuf) throws BufException {
		byteBuf.writeNonTerminatedString(identifier);
		byteBuf.write((byte) ' ');
		byteBuf.writeString(getAttributeString());
	}
	
	@Override
	public final void read(ByteBuf byteBuf) throws BufException, ParseException {
		throw new NotImplementedParseException();
	}
	
	@Override
	public final PacketComponent[] getChildren() {
		return null;
	}
	
	@Override
	public final int length() {
		return identifier.length() + 1 + getAttributeString().length() + 1;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s", identifier, getAttributeString());
	}
	
	@Override
	public int hashCode() {
		return identifier.hashCode() + attributesHashCode();
	}
	
	public abstract String getAttributeString();
	
	public abstract Data scaleAddress(Address address);
	public abstract int getAddressNumBytes();
	
	public abstract RoutingTable makeRoutingTable(NetworkInstance networkInstance);
	public abstract NetworkPacket makeNetworkPacket(NetworkSlot networkSlot);
	
	public abstract NetworkPayload makeInNetworkPayload(CommonNetworkPayloadProperties commonNetworkPayloadProperties);
	public abstract NetworkPayload makeOutNetworkPayload(Data sourceAddressData, CommonNetworkPayloadProperties commonNetworkPayloadProperties);
	
	public abstract int attributesHashCode();
	public abstract boolean attributesEqual(T networkType);
	
	public final boolean equals(Object o) {
		if(!(this.getClass().isInstance(o))) return false;
		return attributesEqual((T) o);
	}
	
}
