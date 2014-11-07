package org.dclayer.net.network;

import org.dclayer.apbr.APBRPacket;
import org.dclayer.apbr.APBRRoutingTable;
import org.dclayer.exception.net.parse.MalformedAttributeStringException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.address.Address;
import org.dclayer.net.routing.ForwardDestination;
import org.dclayer.net.routing.RoutingTable;


public class APBRNetworkType extends NetworkType {
	
	private int numParts = 1;
	private int partBits = 1;
	private int bitLength;
	
	private APBRRoutingTable apbrRoutingTable;
	private Data scaledAddress;
	
	public APBRNetworkType(String attributeString) throws ParseException {
		super(NetworkType.IDENTIFIER_APBR);
		parseAttributeString(attributeString);
		this.bitLength = this.numParts * this.partBits;
	}
	
	public <T> APBRNetworkType(int numParts, int partBits, ForwardDestination<T> localForwardDestination) {
		super(NetworkType.IDENTIFIER_APBR);
		this.numParts = numParts;
		this.partBits = partBits;
		this.bitLength = this.numParts * this.partBits;
		activate(localForwardDestination);
	}
	
	private void parseAttributeString(String attributeString) throws ParseException {
		String[] attributes = attributeString.split("/");
		if(attributes.length != 2) throw new MalformedAttributeStringException(this, attributeString);
		try {
			this.numParts = Integer.parseInt(attributes[0]);
			this.partBits = Integer.parseInt(attributes[1]);
		} catch(NumberFormatException e) {
			throw new MalformedAttributeStringException(this, attributeString, e);
		}
	}

	@Override
	public String getAttributeString() {
		return String.format("%d/%d", numParts, partBits);
	}
	
	public int getNumParts() {
		return numParts;
	}
	
	public int getPartBits() {
		return partBits;
	}
	
	public void setNumParts(int numParts) {
		this.numParts = numParts;
	}
	
	public void setPartBits(int partBits) {
		this.partBits = partBits;
	}

	@Override
	public RoutingTable getRoutingTable() {
		return apbrRoutingTable;
	}

	@Override
	public Data scaleAddress(Address address) {
		
		Data fullData = address.hashData();
		Data scaledData = new Data((int) Math.ceil(this.bitLength / 8d));
		
		for(int i = 0; i < fullData.length(); i++) {
			int scaledIndex = i % scaledData.length();
			scaledData.setByte(scaledIndex, (byte)(scaledData.getByte(scaledIndex) ^ fullData.getByte(i)));
		}
		
		return scaledData;
		
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof APBRNetworkType)) return false;
		APBRNetworkType otherAPBRNetworkType = (APBRNetworkType) o;
		return this.getNumParts() == otherAPBRNetworkType.getNumParts() && this.getPartBits() == otherAPBRNetworkType.getPartBits();
	}

	@Override
	public boolean isActive() {
		return apbrRoutingTable != null;
	}

	@Override
	public <T> void activate(ForwardDestination<T> localForwardDestination) {
		this.apbrRoutingTable = new APBRRoutingTable(this, localForwardDestination);
		this.scaledAddress = this.scaleAddress(localForwardDestination.getAddress());
	}

	@Override
	public NetworkPacket makeNetworkPacket(NetworkSlot networkSlot) {
		return new APBRPacket(networkSlot, this);
	}

	@Override
	public Data getScaledAddress() {
		return scaledAddress;
	}

}
