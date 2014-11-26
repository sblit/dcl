package org.dclayer.net.a2s.rev0.component;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.componentinterface.AddressComponentI;

public class AddressComponent extends PacketComponent implements AddressComponentI {
	
	private Data addressData;
	private Data ownAddressData = new Data();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		byteBuf.read(addressData = ownAddressData);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(addressData);
	}

	@Override
	public int length() {
		return addressData == null ? 0 : addressData.length();
	}

	@Override
	public String toString() {
		return String.format("AddressComponent(address=%s)", addressData);
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	@Override
	public Data getAddressData() {
		return addressData;
	}
	
	@Override
	public void setAddressData(Data addressData) {
		this.addressData = addressData;
	}
	
}
