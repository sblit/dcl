package org.dclayer.net.process;

import org.dclayer.DCL;
import org.dclayer.DCLService;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.filter.message.S2SLinkPacketMessageFilter;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.LinkSendInterface;
import org.dclayer.net.link.OnOpenChannelRequestListener;
import org.dclayer.net.link.channel.component.DataChannelDataComponent;
import org.dclayer.net.link.channel.data.ApplicationDataChannel;
import org.dclayer.net.link.channel.data.DataChannel;
import org.dclayer.net.process.template.PersistentS2SPointToPointReceiverProcess;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.AddressedPacket;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.message.S2SApplicationDataMessage;
import org.dclayer.net.s2s.rev0.message.S2SLinkPacketMessage;

/**
 * a persistent service-to-service point-to-point process handling links
 */
public class S2SLinkProcess extends PersistentS2SPointToPointReceiverProcess implements LinkSendInterface, OnOpenChannelRequestListener, ApplicationDataChannel.OnChannelDataListener {
	
	private DCLService dclService;
	private Link link;
	private boolean received = false;
	
	public S2SLinkProcess(CachedServiceAddress cachedServiceAddress) {
		super(cachedServiceAddress, new S2SLinkPacketMessageFilter());
	}
	
	@Override
	public int defineProperties() {
		return super.defineProperties() | LINK;
	}
	
	@Override
	public Link getLink() {
		return link;
	}

	@Override
	public synchronized Process receiveS2S(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		switch(revision) {
		case 0: {
			S2SLinkPacketMessage linkPacketMessage = (S2SLinkPacketMessage) ((Message) revisionMessage).getMessage();
			byte[] data = linkPacketMessage.getDataComponent().getData();
			Log.debug(Log.PART_PROCESS, this, String.format("received link packet message from %s", cachedServiceAddress.toString()));
			
			received = true;
			link.onReceive(new DataByteBuf(new Data(data)));
			
			return Process.NULLPROCESS;
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
	}

	@Override
	protected void start() {
		this.dclService = (DCLService) getFollowUpProcessSpawnInterface();
		link = new Link(this, this);
		(new Thread() {
			public void run() {
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
				boolean initiator = false;
				
				synchronized(S2SLinkProcess.this) {
					if(!received) {
						link.connect();
						initiator = true;
					}
				}
				
				if(!initiator) return;
				
				for(;;) {
					if(link.getStatus() == Link.Status.Connected) break;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
				
				link.openChannel(DCL.CHANNEL_APPDATA);
				
			}
		}).start();
	}

	@Override
	public void sendLinkPacket(Link link, Data data) {
		dclService.send(new AddressedPacket(new Message(new S2SLinkPacketMessage(new DataComponent(data.copyToByteArray()))), getCachedServiceAddressFilter().getCachedServiceAddress().getServiceAddress()));
	}

	@Override
	public DataChannel onOpenChannelRequest(Link link, long channelId, String protocol) {
		if(protocol.equals(DCL.CHANNEL_APPDATA)) {
			ApplicationDataChannel channel = new ApplicationDataChannel(link, channelId, protocol);
			channel.setOnChannelDataListener(this);
			return channel;
		}
		return null;
	}

	@Override
	public void onChannelData(ApplicationDataChannel applicationDataChannel, DataChannelDataComponent dataChannelDataComponent) {
		DataByteBuf byteBuf = new DataByteBuf(dataChannelDataComponent.getDataComponent().getData());
		S2SApplicationDataMessage s2sApplicationDataMessage;
		try {
			s2sApplicationDataMessage = new S2SApplicationDataMessage(byteBuf);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		} catch (BufException e) {
			e.printStackTrace();
			return;
		}
		dclService.getProcessReceiveQueue().receive(
				this.getCachedServiceAddressFilter().getCachedServiceAddress(),
				new Message(s2sApplicationDataMessage),
				0,
				s2sApplicationDataMessage.getType());
	}

}
