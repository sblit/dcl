package org.dclayer.net.process;

import org.dclayer.DCL;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.listener.net.FollowUpProcessSpawnInterface;
import org.dclayer.meta.Log;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.DataMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.filter.ApplicationConnectionFilter;
import org.dclayer.net.filter.message.A2SDataMessageFilter;
import org.dclayer.net.link.channel.data.ApplicationDataChannel;
import org.dclayer.net.process.template.PersistentA2SReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.message.S2SApplicationDataMessage;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * persistent application-to-service receiver process accepting data messages
 */
public class A2SDataReceiveProcess extends PersistentA2SReceiverProcess {
	
	/**
	 * the {@link ApplicationConnectionFilter} specifying from which {@link ApplicationConnection} this {@link Process} accepts Packets
	 */
	private ApplicationConnectionFilter applicationConnectionFilter;
	/**
	 * the {@link ApplicationIdentifier} from which this {@link Process} accepts Packets
	 */
	private ApplicationIdentifier applicationIdentifier;
	
	private FollowUpProcessSpawnInterface followUpProcessSpawnInterface;

	public A2SDataReceiveProcess(ApplicationConnection applicationConnection, ApplicationIdentifier applicationIdentifier) {
		super(new A2SDataMessageFilter());
		this.applicationConnectionFilter = new ApplicationConnectionFilter(applicationConnection);
		this.applicationIdentifier = applicationIdentifier;
	}
	
	@Override
	public int defineProperties() {
		return super.defineProperties() | APPLICATIONCONNECTIONFILTER | DAEMON;
	}
	
	@Override
	public void start(FollowUpProcessSpawnInterface followUpProcessSpawnInterface) {
		this.followUpProcessSpawnInterface = followUpProcessSpawnInterface;
	}
	
	@Override
	public ApplicationConnectionFilter getApplicationConnectionFilter() {
		return applicationConnectionFilter;
	}

	@Override
	public Process receiveA2S(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		ApplicationIdentifier destinationApplicationIdentifier, sourceApplicationIdentifier;
		ServiceAddress serviceAddress;
		byte[] data;
		ApplicationIdentifier applicationIdentifier;
		
		switch(revision) {
		case 35: {
			org.dclayer.net.a2s.rev35.message.DataMessage dataMessage = (org.dclayer.net.a2s.rev35.message.DataMessage) ((org.dclayer.net.a2s.rev35.Message) revisionMessage).getMessage();
			destinationApplicationIdentifier = dataMessage.getDestinationApplicationIdentifierComponent().getApplicationIdentifier();
			sourceApplicationIdentifier = dataMessage.getSourceApplicationIdentifierComponent().getApplicationIdentifier();
			serviceAddress = dataMessage.getServiceAddressComponent().getServiceAddress();
			data = dataMessage.getDataComponent().getData();
			applicationIdentifier = dataMessage.getSourceApplicationIdentifierComponent().getApplicationIdentifier();
			break;
		}
		case 0: {
			DataMessage dataMessage = (DataMessage) ((Message) revisionMessage).getMessage();
			destinationApplicationIdentifier = dataMessage.getDestinationApplicationIdentifierComponent().getApplicationIdentifier();
			sourceApplicationIdentifier = dataMessage.getSourceApplicationIdentifierComponent().getApplicationIdentifier();
			serviceAddress = dataMessage.getServiceAddressComponent().getServiceAddress();
			data = dataMessage.getDataComponent().getData();
			applicationIdentifier = dataMessage.getSourceApplicationIdentifierComponent().getApplicationIdentifier();
			break;
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
		
		boolean match = this.applicationIdentifier.equals(applicationIdentifier);
		CachedServiceAddress cachedServiceAddress = getAddressCache().addServiceAddress(serviceAddress, 0);
		Log.debug(Log.PART_PROCESS, this,
				String.format("received data message from ApplicationConnection %s: destapplication=%s, destserviceaddress=%s, srcapplication=%s; match=%s",
						applicationConnection.toString(),
						destinationApplicationIdentifier.toString(),
						cachedServiceAddress.toString(),
						sourceApplicationIdentifier.toString(),
						match));
		if(match) {
			Log.debug(Log.PART_PROCESS, this, String.format("cachedServiceAddress.link=%s", cachedServiceAddress.link));
			if(cachedServiceAddress.link != null) {
				ApplicationDataChannel channel = cachedServiceAddress.link.getChannel(DCL.CHANNEL_APPDATA);
				if(channel == null) {
					channel = (ApplicationDataChannel) cachedServiceAddress.link.openChannel(DCL.CHANNEL_APPDATA);
				}
				S2SApplicationDataMessage s2sApplicationDataMessage = new S2SApplicationDataMessage(
						new ApplicationIdentifierComponent(destinationApplicationIdentifier),
						new ApplicationIdentifierComponent(sourceApplicationIdentifier),
						new DataComponent(data)
				);
				DataByteBuf dataByteBuf = new DataByteBuf(s2sApplicationDataMessage.length());
				try {
					s2sApplicationDataMessage.write(dataByteBuf);
				} catch (BufException e) {
					e.printStackTrace();
					return NULLPROCESS;
				}
				channel.send(dataByteBuf.getData());
				return NULLPROCESS;
			} else {
				return new S2SApplicationDataSendProcess(cachedServiceAddress, destinationApplicationIdentifier, sourceApplicationIdentifier, data);
			}
		} else {
			return null;
		}
	}
	
}
