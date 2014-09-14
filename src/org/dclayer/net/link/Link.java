package org.dclayer.net.link;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.bmcp.crypto.TransparentByteBufGenerator;
import org.dclayer.net.link.channel.Channel;
import org.dclayer.net.link.channel.ChannelCollection;
import org.dclayer.net.link.channel.data.ApplicationDataChannel;
import org.dclayer.net.link.channel.data.DataChannel;
import org.dclayer.net.link.channel.management.ManagementChannel;
import org.dclayer.net.link.component.LinkPacketHeader;
import org.dclayer.net.link.control.FlowControl;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlockCollection;
import org.dclayer.net.link.control.packetbackup.PacketBackup;

/**
 * a layer that is placed above the UDP socket and provides reliable and encrypted communication
 */
public class Link implements HierarchicalLevel {
	
	public static enum Status {
		None,
		ConnectingActiveConnectRequested,
		ConnectingPassiveEchoRequested,
		ConnectingActiveEchoReplied,
		ConnectingPassiveFullEncryptionRequested,
		Connected;
	}
	
	@Override
	public String toString() {
		return String.format("Link@%x", System.identityHashCode(this));
	}
	
	/**
	 * An {@link OnOpenChannelRequestListener} called upon opening of a new channel
	 */
	private OnOpenChannelRequestListener onOpenChannelRequestListener;
	
	/**
	 * {@link LinkPacketHeader} for parsing inbound packets
	 */
	private LinkPacketHeader inLinkPacketHeader = new LinkPacketHeader();
	/**
	 * {@link LinkPacketHeader} for writing outbound packets
	 */
	private LinkPacketHeader outLinkPacketHeader = new LinkPacketHeader();
	
	/**
	 * the {@link ManagementChannel}
	 */
	private ManagementChannel managementChannel;
	
	/**
	 * the current {@link TransparentByteBufGenerator} for inbound packets
	 */
	private TransparentByteBufGenerator inTransparentByteBufGenerator = null;
	/**
	 * the current {@link TransparentByteBufGenerator} for inbound packets
	 */
	private TransparentByteBufGenerator outTransparentByteBufGenerator = null;
	
	/**
	 * the next (not yet applied) {@link TransparentByteBuf} for outbound headers
	 */
	private TransparentByteBuf newOutHeaderTransparentByteBuf = null;
	/**
	 * the current {@link TransparentByteBuf} for outbound headers
	 */
	private TransparentByteBuf outHeaderTransparentByteBuf = null;
	
	/**
	 * the next (not yet applied) {@link TransparentByteBuf} for inbound headers
	 */
	private TransparentByteBuf newInHeaderTransparentByteBuf = null;
	/**
	 * the current {@link TransparentByteBuf} for inbound headers
	 */
	private TransparentByteBuf inHeaderTransparentByteBuf = null;
	
	/**
	 * a map mapping all {@link ApplicationDataChannel}s to their channel names
	 */
	private Map<String, ApplicationDataChannel> applicationChannelMap = new HashMap<String, ApplicationDataChannel>();
	/**
	 * the {@link ChannelCollection} containing all {@link Channel}s
	 */
	private ChannelCollection channelCollection = new ChannelCollection();
	
	/**
	 * the transmission rate regulating {@link FlowControl} instance for this {@link Link}
	 */
	private FlowControl flowControl;
	
	/**
	 * the total amount of bytes received on this {@link Link} so far
	 */
	private long numBytesReceived = 0;
	/**
	 * the value of {@link System#nanoTime()} at the point in time this {@link Link} started to receive packets
	 */
	private long startedReceiving = 0;
	
	/**
	 * the link's status
	 */
	private Status status = Status.None;
	
	/**
	 * {@link ReentrantLock} locked while receiving
	 */
	private ReentrantLock receiveLock = new ReentrantLock();
	/**
	 * {@link ReentrantLock} locked while sending
	 */
	private ReentrantLock sendLock = new ReentrantLock();
	
	/**
	 * creates a new {@link Link}
	 * @param linkSendInterface the {@link LinkSendInterface} to use
	 * @param onOpenChannelRequestListener the {@link OnOpenChannelRequestListener} to use
	 */
	public Link(LinkSendInterface linkSendInterface, OnOpenChannelRequestListener onOpenChannelRequestListener) {
		this.flowControl = new FlowControl(this, linkSendInterface);
		this.onOpenChannelRequestListener = onOpenChannelRequestListener;
	}

	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		// TODO
		return null;
	}
	
	/**
	 * locks the receiveLock
	 * used by BMCPManagementChannel's ChannelBlockStatusRequest Thread
	 */
	public void lockReceive() {
		receiveLock.lock();
	}
	
	/**
	 * unlocks the receiveLock
	 * used by BMCPManagementChannel's ChannelBlockStatusRequest Thread
	 */
	public void unlockReceive() {
		receiveLock.unlock();
	}
	
	/**
	 * sets the status of this link
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		Log.debug(this, "setting status: %s", status);
		this.status = status;
	}
	
	/**
	 * returns the status of this link
	 * @return the status of this link
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * returns the {@link ChannelCollection} of this link
	 * @return the {@link ChannelCollection} of this link
	 */
	public ChannelCollection getChannelCollection() {
		return channelCollection;
	}
	
	/**
	 * Sets this {@link Link}'s data transmission rate to the given value
	 * @param bytesPerSecond the rate at which data is sent over this {@link Link}
	 */
	public void setFlowControlBytesPerSecond(long bytesPerSecond) {
		flowControl.setBytesPerSecond(bytesPerSecond);
	}
	
	/**
	 * @return the current maximum rate at which data can be sent over this {@link Link} 
	 */
	public long getFlowControlBytesPerSecond() {
		return flowControl.getBytesPerSecond();
	}
	
	/**
	 * @return the total amount of bytes received on this {@link Link} so far
	 */
	public long getNumBytesReceived() {
		return numBytesReceived;
	}
	
	/**
	 * @return the total amount of bytes sent on this {@link Link} so far
	 */
	public long getNumBytesSent() {
		return flowControl.getNumBytesSent();
	}
	
	/**
	 * @return the value of {@link System#nanoTime()} at the point in time this {@link Link} started to receive packets
	 */
	public long getStartedReceiving() {
		return startedReceiving;
	}
	
	/**
	 * onReceive callback, called when a new packet was received for this link
	 * @param byteBuf the {@link ByteBuf} containing the packet data
	 * @return true if this packet was received successfully, false otherwise
	 */
	// no need for synchronization, receiveLinkPacket() locks receiveLock
	public boolean onReceive(ByteBuf byteBuf) {
		
		Log.debug(this, "onReceive()");
		
		if(startedReceiving == 0) {
			startedReceiving = System.nanoTime();
		}
		
		try {
			receiveLinkPacket(byteBuf);
		} catch(ParseException e) {
			e.printStackTrace();
			return false;
		} catch(BufException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * parses the received link packet
	 * @param byteBuf the {@link ByteBuf} containing the link packet data
	 * @throws ParseException
	 * @throws BufException
	 */
	// locks receiveLock; called by onReceive() only
	private void receiveLinkPacket(ByteBuf byteBuf) throws ParseException, BufException {
		
		receiveLock.lock();
		
		if(inHeaderTransparentByteBuf != null) {
			inHeaderTransparentByteBuf.setByteBuf(byteBuf);
			inLinkPacketHeader.read(inHeaderTransparentByteBuf);
		} else {
			inLinkPacketHeader.read(byteBuf);
		}
		
		long channelId = inLinkPacketHeader.getChannelId().getNum();
		long dataId = inLinkPacketHeader.getDataId().getNum();
		int length = (int) inLinkPacketHeader.getChannelDataLength().getNum();
		
		int linkPacketLength = inLinkPacketHeader.length() + length;
		this.numBytesReceived += linkPacketLength;
		
		Log.debug(this, "received (%d bytes): %s", linkPacketLength, inLinkPacketHeader.represent(true));
		
		Channel channel;
		
		if(managementChannel == null) {
			
			// first create and add the management channel, then unlock
			managementChannel = new BMCPManagementChannel(this, channelId, -1, "mgmt");
			putChannel(managementChannel);
			channel = managementChannel;
			
			receiveLock.unlock();
			
		} else {
			
			// safe to unlock here (ChannelCollection is synchronized)
			receiveLock.unlock();
			
			channel = channelCollection.get(channelId);
			
		}
		
		if(channel != null) {
			Log.debug(this, "dataId %d: calling channel.receiveLinkPacketBody()", dataId);
			channel.receiveLinkPacketBody(dataId, channelId, byteBuf, length);
		} else {
			Log.debug(this, "dataId %d: no channel with channelId %d, ignoring", dataId, channelId);
		}
			
	}
	
	/**
	 * reports the existence of a gap in one of this {@link Link}'s channels' {@link DiscontinuousBlockCollection} to the management channel upon receipt of a packet
	 */
	public void onGapReceive() {
		this.managementChannel.onGapReceive();
	}
	
	/**
	 * sets the {@link TransparentByteBufGenerator} for inbound packets
	 * @param inTransparentByteBufGenerator the {@link TransparentByteBufGenerator} to use for for inbound packets
	 */
	// no need for synchronization
	public void setInTransparentByteBufGenerator(TransparentByteBufGenerator inTransparentByteBufGenerator) {
		this.inTransparentByteBufGenerator = inTransparentByteBufGenerator;
	}
	
	/**
	 * sets the {@link TransparentByteBufGenerator} for outbound packets
	 * @param inTransparentByteBufGenerator the {@link TransparentByteBufGenerator} to use for for outbound packets
	 */
	// no need for synchronization
	public void setOutTransparentByteBufGenerator(TransparentByteBufGenerator outTransparentByteBufGenerator) {
		this.outTransparentByteBufGenerator = outTransparentByteBufGenerator;
	}
	
	/**
	 * sets the next {@link TransparentByteBuf} for outbound packet headers
	 * @param outHeaderTransparentByteBuf the next {@link TransparentByteBuf} for outbound packet headers
	 */
	// no need for synchronization, this does not apply the TransparentByteBuf
	public void setNewOutHeaderTransparentByteBuf(TransparentByteBuf outHeaderTransparentByteBuf) {
		Log.debug(this, "new out-header-TransparentByteBuf: %s (not applying yet)", outHeaderTransparentByteBuf);
		this.newOutHeaderTransparentByteBuf = outHeaderTransparentByteBuf;
	}
	
	/**
	 * sets the next {@link TransparentByteBuf} for inbound packet headers
	 * @param inHeaderTransparentByteBuf the next {@link TransparentByteBuf} for inbound packet headers
	 */
	// no need for synchronization, this does not apply the TransparentByteBuf
	public void setNewInHeaderTransparentByteBuf(TransparentByteBuf inHeaderTransparentByteBuf) {
		Log.debug(this, "new in-header-TransparentByteBuf: %s (not applying yet)", inHeaderTransparentByteBuf);
		this.newInHeaderTransparentByteBuf = inHeaderTransparentByteBuf;
	}
	
	/**
	 * applies the next outbound header {@link TransparentByteBuf}
	 */
	// locks sendLock
	public void applyNewOutHeaderTransparentByteBuf() {
		Log.debug(this, "applying new out-header-TransparentByteBuf: %s", newOutHeaderTransparentByteBuf);
		sendLock.lock();
		outHeaderTransparentByteBuf = newOutHeaderTransparentByteBuf;
		sendLock.unlock();
		newOutHeaderTransparentByteBuf = null;
	}
	
	/**
	 * applies the next inbound header {@link TransparentByteBuf}
	 */
	// locks receiveLock
	public void applyNewInHeaderTransparentByteBuf() {
		Log.debug(this, "applying new in-header-TransparentByteBuf: %s", newInHeaderTransparentByteBuf);
		receiveLock.lock();
		inHeaderTransparentByteBuf = newInHeaderTransparentByteBuf;
		receiveLock.unlock();
		newInHeaderTransparentByteBuf = null;
	}
	
	/**
	 * called when peer wants to open a new channel
	 * @param channelId the channelId of the new channel
	 * @param protocol the protocol of the new channel
	 * @return true if channel opening is permitted, false otherwise
	 */
	// locks receiveLock; newChannel is isolated and ChannelCollection (which is being added to in putChannel) is synchronized
	// called by the ManagementChannel only
	public boolean onOpenChannelRequest(long channelId, String protocol) {
		
		receiveLock.lock();
		
		Channel channel = newChannel(channelId, protocol);
		if(channel != null) {
			putChannel(channel);
		}
		
		receiveLock.unlock();
		
		return channel != null;
		
	}
	
	//
	
	/**
	 * writes the header to the given {@link Data} object
	 * @param dataId the data id for this packet
	 * @param channelId the channel id of the channel this packet belongs to
	 * @param channelDataLength the length of the channel data
	 * @param data the {@link Data} object the header should be written to
	 * @return the length of the packet header (or in other words, how many bytes have been written)
	 * @throws BufException
	 */
	// locks sendLock
	public int writeHeader(long dataId, long channelId, int channelDataLength, Data data) throws BufException {
		
		sendLock.lock();
		
		outLinkPacketHeader.setDataId(dataId);
		outLinkPacketHeader.setChannelId(channelId);
		outLinkPacketHeader.setChannelDataLength(channelDataLength);
		
		int linkPacketHeaderLength = outLinkPacketHeader.length();
		
		Log.debug(this, "sending: %s", outLinkPacketHeader.represent(true));
		
		data.prepare(linkPacketHeaderLength + channelDataLength);
		
		// TODO make this more efficient (i.e. re-use DataByteBuf)
		ByteBuf byteBuf = new DataByteBuf(data);
		ByteBuf headerWriteByteBuf;
		
		if(outHeaderTransparentByteBuf == null) {
			headerWriteByteBuf = byteBuf;
		} else {
			outHeaderTransparentByteBuf.setByteBuf(byteBuf);
			headerWriteByteBuf = outHeaderTransparentByteBuf;
		}
		
		outLinkPacketHeader.write(headerWriteByteBuf);
		
		sendLock.unlock();
		
		return linkPacketHeaderLength;
	
	}
	
	/**
	 * Queues the given {@link PacketBackup} for transmission
	 * @param packetBackup the {@link PacketBackup} to queue for transmission
	 */
	// no need for synchronization
	public void send(PacketBackup packetBackup) {
		
		send(packetBackup, false);
		
	}
	
	/**
	 * Queues the given {@link PacketBackup} for transmission
	 * @param packetBackup the {@link PacketBackup} to queue for transmission
	 * @param waitTilSent pass true if this operation should block until the {@link PacketBackup} is actually sent, pass false otherwise
	 */
	public void send(PacketBackup packetBackup, boolean waitTilSent) {
		
		flowControl.queue(packetBackup, waitTilSent);
		
	}
	
	//
	
	/**
	 * connects this link
	 */
	// locks receiveLock
	public void connect() {
		
		receiveLock.lock();
		
		setStatus(Status.ConnectingActiveConnectRequested);
		
		int channelId = (int)(Math.random() * Integer.MAX_VALUE);
		
		BMCPManagementChannel bmcpManagementChannel = new BMCPManagementChannel(this, channelId, -1, "mgmt");
		managementChannel = bmcpManagementChannel;
		channelCollection.put(managementChannel);
		// connect() starts the channel
		
		bmcpManagementChannel.connect();
		
		receiveLock.unlock();
		
	}
	
	/**
	 * returns a new {@link DataChannel} object for the given protocol identifier and channel id
	 * @param channelId the channel id of the new {@link DataChannel}
	 * @param protocol the protocol identifier of the protocol in the new {@link DataChannel}
	 * @return the new {@link DataChannel}
	 */
	// no synchronization needed
	private DataChannel newChannel(long channelId, String protocol) {
		return onOpenChannelRequestListener.onOpenChannelRequest(this, channelId, protocol);
	}
	
	/**
	 * adds a new channel to this link
	 * @param channel the channel to add
	 */
	// no synchronization needed; called from receiveLinkPacket, onOpenChannelRequest and openChannel, which all lock receiveLock, only
	private void putChannel(Channel channel) {
		if(outTransparentByteBufGenerator != null) {
			channel.setNewOutBodyTransparentByteBuf(outTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
			channel.applyNewOutBodyTransparentByteBuf();
		}
		if(inTransparentByteBufGenerator != null) {
			channel.setNewInBodyTransparentByteBuf(inTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
			channel.applyNewInBodyTransparentByteBuf();
		}
		if(channel instanceof ApplicationDataChannel) {
			applicationChannelMap.put(channel.getChannelName(), (ApplicationDataChannel) channel);
		}
		channelCollection.put(channel);
		channel.start();
	}
	
	/**
	 * additionally reassigns the given channel to the channel collection using its unreliable channel id
	 * @param channel the {@link Channel} to reassign using its unreliable channel id
	 */
	public void addUnreliableChannelId(Channel channel) {
		channelCollection.assign(channel.getUnreliableChannelId(), channel);
	}
	
	/**
	 * opens a new {@link DataChannel} on this link
	 * @param protocol the protocol identifier of the protocol to use on the new channel
	 * @return the newly added channel
	 */
	// locks receiveLock
	public DataChannel openChannel(String protocol) {
		
		long channelId;
		
		receiveLock.lock();
		
		do {
			channelId = (long)(Math.random() * Integer.MAX_VALUE);
		} while(channelCollection.get(channelId) != null);
		
		DataChannel channel = newChannel(channelId, protocol);
		
		if(channel != null) {
			
			Log.debug(this, "opening new channel: channelId %d, protocol: %s", channelId, protocol);
			putChannel(channel);
			managementChannel.requestOpenChannel(channelId, protocol);
			
		} else {
			
			Log.debug(this, "can't open new channel for protocol: %s", protocol);
			
		}
		
		receiveLock.unlock();
		
		return channel;
		
	}
	
	/**
	 * returns the channel on this link matching the given name
	 * @param channelName the name of the channel to return
	 * @return the channel with a matching name
	 */
	public ApplicationDataChannel getChannel(String channelName) {
		return applicationChannelMap.get(channelName);
	}
}
