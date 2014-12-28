package org.dclayer.net.circle;

import org.dclayer.crypto.hash.HashAlgorithm;
import org.dclayer.exception.net.parse.MalformedAttributeStringException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnknownHashAlgorithmException;
import org.dclayer.net.Data;
import org.dclayer.net.address.Address;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.component.NetworkPayload;
import org.dclayer.net.network.properties.CommonNetworkPayloadProperties;
import org.dclayer.net.network.routing.RoutingTable;
import org.dclayer.net.network.slot.GenericNetworkSlot;


public class CircleNetworkType extends NetworkType<CircleNetworkType> {
	
	private HashAlgorithm hashAlgorithm;
	private int byteLength;
	
	public CircleNetworkType(String attributeString) throws ParseException {
		super(NetworkType.IDENTIFIER_CIRCLE);
		parseAttributeString(attributeString);
	}
	
	public <T extends NetworkPacket> CircleNetworkType(HashAlgorithm hashAlgorithm, int byteLength) {
		super(NetworkType.IDENTIFIER_CIRCLE);
		this.hashAlgorithm = hashAlgorithm;
		this.byteLength = byteLength;
	}
	
	private void parseAttributeString(String attributeString) throws ParseException {
		String[] attributes = attributeString.split("/");
		if(attributes.length != 2) throw new MalformedAttributeStringException(this, attributeString);
		this.hashAlgorithm = HashAlgorithm.byIdentifier(attributes[0]);
		if(this.hashAlgorithm == null) throw new UnknownHashAlgorithmException(attributes[0]);
		try {
			this.byteLength = Integer.parseInt(attributes[1]);
		} catch(NumberFormatException e) {
			throw new MalformedAttributeStringException(this, attributeString, e);
		}
	}

	@Override
	public String getAttributeString() {
		return String.format("%s/%d", hashAlgorithm.getIdentifier(), byteLength);
	}

	@Override
	public int getAddressNumBytes() {
		return byteLength;
	}

	@Override
	public Data scaleAddress(Address address) {
		
		Data fullData = address.toData();
		if(byteLength == hashAlgorithm.getDigestSize()) return fullData;
		
		Data scaledData = new Data(byteLength);
		
		for(int i = 0; i < fullData.length(); i++) {
			int scaledIndex = i % scaledData.length();
			scaledData.setByte(scaledIndex, (byte)(scaledData.getByte(scaledIndex) ^ fullData.getByte(i)));
		}
		
		return scaledData;
		
	}

	@Override
	public NetworkPacket makeNetworkPacket(GenericNetworkSlot networkSlot) {
		return new CirclePacket(networkSlot, this);
	}

	@Override
	public NetworkPayload makeInNetworkPayload(CommonNetworkPayloadProperties commonNetworkPayloadProperties) {
		return new CircleNetworkPayload(this, commonNetworkPayloadProperties);
	}
	
	@Override
	public NetworkPayload makeOutNetworkPayload(Data scaledAddressData, CommonNetworkPayloadProperties commonNetworkPayloadProperties) {
		return new CircleNetworkPayload(this, scaledAddressData, commonNetworkPayloadProperties);
	}

	@Override
	public RoutingTable makeRoutingTable(NetworkInstance networkInstance) {
		return new CircleRoutingTable(this, networkInstance);
	}

	@Override
	public boolean attributesEqual(CircleNetworkType circleNetworkType) {
		return this.hashAlgorithm == circleNetworkType.hashAlgorithm && this.byteLength == circleNetworkType.byteLength;
	}

	@Override
	public int attributesHashCode() {
		return hashAlgorithm.hashCode() + byteLength;
	}

}
