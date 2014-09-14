package org.dclayer.net.process;

import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.message.DataMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.OneTimeA2SSendProcess;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;

/**
 * a one-time application-to-service data message send process
 */
public class A2SDataSendProcess extends OneTimeA2SSendProcess {
	
	/**
	 * the destination {@link ApplicationIdentifier} of the data message sent by this {@link Process}
	 */
	private ApplicationIdentifier destinationApplicationIdentifier;
	/**
	 * the source {@link ApplicationIdentifier} of the data message sent by this {@link Process}
	 */
	private ApplicationIdentifier sourceApplicationIdentifier;
	/**
	 * the {@link CachedServiceAddress} of the data message sent by this {@link Process} 
	 */
	private CachedServiceAddress cachedServiceAddress;
	/**
	 * the data of the data message sent by this {@link Process}
	 */
	private byte[] data;
	
	public A2SDataSendProcess(ApplicationConnection applicationConnection, ApplicationIdentifier destinationApplicationIdentifier, ApplicationIdentifier sourceApplicationIdentifier, CachedServiceAddress cachedServiceAddress, byte[] data) {
		super(applicationConnection);
		this.destinationApplicationIdentifier = destinationApplicationIdentifier;
		this.sourceApplicationIdentifier = sourceApplicationIdentifier;
		this.cachedServiceAddress = cachedServiceAddress;
		this.data = data;
	}

	@Override
	public RevisionMessage getA2SMessage(int revision) {
		switch(revision) {
		case 35: {
			return new org.dclayer.net.a2s.rev35.Message(
							new org.dclayer.net.a2s.rev35.message.DataMessage(
									new org.dclayer.net.a2s.rev35.component.ApplicationIdentifierComponent(destinationApplicationIdentifier),
									new org.dclayer.net.a2s.rev35.component.ApplicationIdentifierComponent(sourceApplicationIdentifier),
									new org.dclayer.net.a2s.rev35.component.ServiceAddressComponent(cachedServiceAddress.getServiceAddress()),
									new org.dclayer.net.a2s.rev35.component.DataComponent(data)
								)
						);
		}
		case 0:
		default: {
			return new Message(new DataMessage(
					new ApplicationIdentifierComponent(destinationApplicationIdentifier),
					new ApplicationIdentifierComponent(sourceApplicationIdentifier),
					new ServiceAddressComponent(cachedServiceAddress.getServiceAddress()),
					new DataComponent(data)));
		}
		}
	}
	
}
