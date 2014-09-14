package org.dclayer.net.s2s;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * a subclass of {@link S2SPacket} that also provides a {@link ServiceAddress} from which this was received or to which this should be sent
 */
public class AddressedPacket extends S2SPacket {

	private ServiceAddress serviceAddress;

	public AddressedPacket(ByteBuf byteBuf, ServiceAddress serviceAddress) throws ParseException, BufException {
		super(byteBuf);
		this.serviceAddress = serviceAddress;
	}
	
	public AddressedPacket(RevisionMessage message, ServiceAddress serviceAddress) {
		super(message);
		this.serviceAddress = serviceAddress;
	}

	/**
	 * @return the {@link ServiceAddress} from which this {@link AddressedPacket} was received or to which this {@link AddressedPacket} should be sent
	 */
	public ServiceAddress getServiceAddress() {
		return serviceAddress;
	}

}
