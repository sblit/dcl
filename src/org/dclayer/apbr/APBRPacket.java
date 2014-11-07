package org.dclayer.apbr;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.network.APBRNetworkType;
import org.dclayer.net.network.NetworkPacket;
import org.dclayer.net.network.NetworkSlot;

/**
 * Main address part based routing packet type containing both the type and the message
 * @author Martin Exner
 */
public class APBRPacket extends NetworkPacket {
	
	private APBRNetworkType apbrNetworkType;
	
	private Data addressData;
	private DataComponent dataComponent = new DataComponent();
	
	public APBRPacket(NetworkSlot networkSlot, APBRNetworkType apbrNetworkType) {
		super(networkSlot);
		this.apbrNetworkType = apbrNetworkType;
		this.addressData = new Data((int) Math.ceil(apbrNetworkType.getNumParts()*apbrNetworkType.getPartBits()/8d));
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		byteBuf.read(addressData);
		dataComponent.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(addressData);
		dataComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return addressData.length() + dataComponent.length();
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { dataComponent };
	}

	@Override
	public String toString() {
		return String.format("APBRPacket(address=%s)", addressData);
	}
	
	public APBRNetworkType getAPBRNetworkType() {
		return apbrNetworkType;
	}

	@Override
	public Data getDestinationAddressData() {
		return addressData;
	}

}
