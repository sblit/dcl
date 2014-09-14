package org.dclayer.net.link.channel.management;
import java.util.concurrent.locks.ReentrantLock;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.buf.SubByteBuf;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.channel.Channel;
import org.dclayer.net.link.channel.component.ChannelDataComponent;
import org.dclayer.net.link.control.FlowControl;
import org.dclayer.net.link.control.ResendPacketQueue;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlockCollection;
import org.dclayer.net.link.control.idcollection.IdCollection;
import org.dclayer.net.link.control.packetbackup.PacketBackup;
import org.dclayer.net.link.control.packetbackup.PacketBackupCollection;
import org.dclayer.net.link.control.packetbackup.UnreliablePacketBackupCollection;

/**
 * an abstract base class for management channel implementations
 */
public abstract class ManagementChannel extends Channel {
	
	/**
	 * {@link SubByteBuf} used to securely pass the decrypted channel data on to the {@link ManagementChannel#readCommand(DiscontinuousBlock, long, ByteBuf, int)} method
	 */
	private SubByteBuf subByteBuf = new SubByteBuf();
	/**
	 * {@link DataByteBuf} used for reading from {@link DiscontinuousBlock}s
	 */
	private DataByteBuf dataByteBuf = new DataByteBuf();
	
	/**
	 * {@link IdCollection} of all sent data ids
	 */
	private IdCollection sentDataIdCollection = new IdCollection();
	/**
	 * {@link PacketBackupCollection} of sent packets
	 */
	private PacketBackupCollection sentPacketBackupCollection = new PacketBackupCollection(this, 1024);
	/**
	 * {@link UnreliablePacketBackupCollection} of {@link PacketBackup}s used for unreliable messages
	 */
	private UnreliablePacketBackupCollection unreliablePacketBackupCollection = new UnreliablePacketBackupCollection(this, 1024);
	/**
	 * {@link IdCollection} of all received data ids
	 */
	private IdCollection receivedDataIdCollection = new IdCollection();
	/**
	 * {@link DiscontinuousBlockCollection} of received packets
	 */
	private DiscontinuousBlockCollection receivedDiscontinuousBlockCollection = new DiscontinuousBlockCollection(1024);
	
	/**
	 * {@link TransparentByteBuf} for inbound packet bodies
	 */
	private TransparentByteBuf inBodyTransparentByteBuf = null;
	/**
	 * {@link TransparentByteBuf} for outbound packet bodies
	 */
	private TransparentByteBuf outBodyTransparentByteBuf = null;
	
	/**
	 * next {@link TransparentByteBuf} for inbound packet bodies
	 */
	private TransparentByteBuf newInBodyTransparentByteBuf = null;
	/**
	 * next {@link TransparentByteBuf} for outbound packet bodies
	 */
	private TransparentByteBuf newOutBodyTransparentByteBuf = null;
	
	/**
	 * {@link TransparentByteBuf} for inbound, unreliable packet bodies
	 */
	private TransparentByteBuf inUnreliableBodyTransparentByteBuf = null;
	/**
	 * {@link TransparentByteBuf} for outbound, unreliable packet bodies
	 */
	private TransparentByteBuf outUnreliableBodyTransparentByteBuf = null;
	
	/**
	 * next {@link TransparentByteBuf} for inbound, unreliable packet bodies
	 */
	private TransparentByteBuf newInUnreliableBodyTransparentByteBuf = null;
	/**
	 * next {@link TransparentByteBuf} for outbound, unreliable packet bodies
	 */
	private TransparentByteBuf newOutUnreliableBodyTransparentByteBuf = null;
	
	/**
	 * {@link ReentrantLock} locked while receiving
	 */
	private ReentrantLock receiveLock = new ReentrantLock();
	/**
	 * {@link ReentrantLock} locked while sending
	 */
	private ReentrantLock sendLock = new ReentrantLock();
	
	/**
	 * the data id of the last sent packet
	 */
	private long dataId = -1;
	
	/**
	 * the channel id to use when sending unreliable channel messages over this channel
	 */
	private long unreliableChannelId;
	
	/**
	 * {@link ResendPacketQueue} for automatically resending lost packets
	 */
	private ResendPacketQueue resendPacketQueue = new ResendPacketQueue(this);
	/**
	 * Thread that resends queued packet from the {@link ResendPacketQueue}
	 */
	private Thread resendThread = new Thread() {
		@Override
		public void run() {
			for(;;) {
				// wait until the next PacketBackup is due for resend
				PacketBackup packetBackup = resendPacketQueue.waitAndPopNext();
				boolean resend = packetBackup.getResendPacketQueueProperties().resend(ManagementChannel.this);
				Log.debug(ManagementChannel.this, "%s, resend=%s", packetBackup, resend);
				if(resend) {
					// resend this packet
					resend(packetBackup);
					resendPacketQueue.queue(packetBackup); // requeue the packet
				} else {
					// declare this packet as failed to send
					// TODO
					Log.debug(ManagementChannel.this, "FAILED TO TRANSMIT PACKET %s", packetBackup);
				}
			}
		}
	};
	
	public ManagementChannel(Link link, long channelId, long unreliableChannelId, String channelName) {
		super(link, channelId, channelName);
		this.unreliableChannelId = unreliableChannelId;
	}
	
	@Override
	public long getUnreliableChannelId() {
		return unreliableChannelId;
	}
	
	public void setUnreliableChannelId(long unreliableChannelId) {
		this.unreliableChannelId = unreliableChannelId;
	}
	
	@Override
	public IdCollection getSentDataIdCollection() {
		return null;
	}
	
	@Override
	public PacketBackupCollection getSentPacketBackupCollection() {
		return null;
	}
	
	@Override
	public IdCollection getReceivedDataIdCollection() {
		return null;
	}
	
	// no synchronization needed, this does not apply anything
	@Override
	public void setNewInBodyTransparentByteBuf(TransparentByteBuf newInBodyTransparentByteBuf) {
		this.newInBodyTransparentByteBuf = newInBodyTransparentByteBuf;
	}
	
	// no synchronization needed, this does not apply anything
	@Override
	public void setNewOutBodyTransparentByteBuf(TransparentByteBuf newOutBodyTransparentByteBuf) {
		this.newOutBodyTransparentByteBuf = newOutBodyTransparentByteBuf;
	}
	
	// no synchronization needed, this does not apply anything
	public void setNewInUnreliableBodyTransparentByteBuf(TransparentByteBuf newInUnreliableBodyTransparentByteBuf) {
		this.newInUnreliableBodyTransparentByteBuf = newInUnreliableBodyTransparentByteBuf;
	}

	// no synchronization needed, this does not apply anything
	public void setNewOutUnreliableBodyTransparentByteBuf(TransparentByteBuf newOutUnreliableBodyTransparentByteBuf) {
		this.newOutUnreliableBodyTransparentByteBuf = newOutUnreliableBodyTransparentByteBuf;
	}
	
	// locks receiveLock
	@Override
	public void applyNewInBodyTransparentByteBuf() {
		receiveLock.lock();
		this.inBodyTransparentByteBuf = newInBodyTransparentByteBuf;
		receiveLock.unlock();
		this.newInBodyTransparentByteBuf = null;
	}
	
	// locks sendLock
	@Override
	public void applyNewOutBodyTransparentByteBuf() {
		sendLock.lock();
		this.outBodyTransparentByteBuf = newOutBodyTransparentByteBuf;
		sendLock.unlock();
		this.newOutBodyTransparentByteBuf = null;
	}
	
	// locks receiveLock
	public void applyNewInUnreliableBodyTransparentByteBuf() {
		receiveLock.lock();
		this.inUnreliableBodyTransparentByteBuf = newInUnreliableBodyTransparentByteBuf;
		receiveLock.unlock();
		this.newInUnreliableBodyTransparentByteBuf = null;
	}
	
	// locks sendLock
	public void applyNewOutUnreliableBodyTransparentByteBuf() {
		sendLock.lock();
		this.outUnreliableBodyTransparentByteBuf = newOutUnreliableBodyTransparentByteBuf;
		sendLock.unlock();
		this.newOutUnreliableBodyTransparentByteBuf = null;
	}
	
	// locks receiveLock
	@Override
	public void receiveLinkPacketBody(long dataId, long channelId, ByteBuf byteBuf, int length) throws BufException {

		receiveLock.lock();
		
		if(channelId == unreliableChannelId) {
			receiveUnreliableLinkPacketBody(dataId, byteBuf, length);
		} else {
			receiveReliableLinkPacketBody(dataId, byteBuf, length);
		}

		receiveLock.unlock();
		
	}
	
	// no need for synchronization here, this is called inside receiveLinkPacketBody
	private void receiveReliableLinkPacketBody(long dataId, ByteBuf byteBuf, int length) throws BufException {
		
		long dataIdRead = receivedDiscontinuousBlockCollection.getDataIdRead();

		if(dataId < dataIdRead) {

			DiscontinuousBlock block = receivedDiscontinuousBlockCollection.get(dataId);

			if(block != null && block.getReplyPacketBackup() != null) {

				// this block was received already and has a stored response, reply with the same packet
				Log.debug(this, "already replied to dataId %d, resending reply", dataId);
				resend(block.getReplyPacketBackup());
				
			} else {
				
				Log.debug(this, "received a dataId (%d) below this channel's dataIdRead (%d) and found no linked reply (discontinuousBlock=%s), ignoring", dataId, dataIdRead, block);
				
			}

		} else {

			receivedDataIdCollection.add(dataId);

			if(receivedDiscontinuousBlockCollection.put(dataId, byteBuf, length)) {

				// put() returned true, we've just written the next block to read
				do {

					long curDataId = receivedDiscontinuousBlockCollection.getDataIdRead();
					DiscontinuousBlock discontinuousBlock = receivedDiscontinuousBlockCollection.readFirst();
					DiscontinuousBlock discontinuousBlock2 = receivedDiscontinuousBlockCollection.get(curDataId);

					Log.debug(this, "reading dataId %d (discontinuousBlock=%s, discontinuousBlock2=%s)...", curDataId, discontinuousBlock, discontinuousBlock2);

					readEncrypted(curDataId, discontinuousBlock);

				} while(receivedDiscontinuousBlockCollection.available());

			} else {

				Log.debug(this, "saved dataId %d, missing block(s) inbetween (dataIdRead=%d)", dataId, receivedDiscontinuousBlockCollection.getDataIdRead());
				
				getLink().onGapReceive();
				
			}

		}
		
	}
	
	// no need for synchronization here, this is called inside receiveLinkPacketBody
	private void receiveUnreliableLinkPacketBody(long dataId, ByteBuf byteBuf, int length) throws BufException {

		subByteBuf.setByteBuf(byteBuf, length);
		ByteBuf readByteBuf = subByteBuf;
		if(inUnreliableBodyTransparentByteBuf != null) {
			inUnreliableBodyTransparentByteBuf.setByteBuf(readByteBuf);
			readByteBuf = inUnreliableBodyTransparentByteBuf;
		}
		
		readCommand(null, dataId, readByteBuf, length);
		
	}
	
	/**
	 * reads the (encrypted) packet body, decrypting it
	 * @param byteBuf the {@link ByteBuf} holding packet body data
	 * @param length the length of the packet body data
	 * @throws BufException
	 */
	// no need for synchronization, this is called by receiveReliableLinkPacketBody, which is called by receiveLinkPacketBody, which locks receiveLock
	private void readEncrypted(long dataId, DiscontinuousBlock discontinuousBlock) throws BufException {
		
		final Data data = discontinuousBlock.getData();
		final int length = data.length();
		
		dataByteBuf.setData(data);
		
		ByteBuf byteBuf;
		if(inBodyTransparentByteBuf == null) {
			byteBuf = dataByteBuf;
		} else {
			inBodyTransparentByteBuf.setByteBuf(dataByteBuf);
			byteBuf = inBodyTransparentByteBuf;
		}
		
		subByteBuf.setByteBuf(byteBuf, length);
		
		readCommand(discontinuousBlock, dataId, subByteBuf, length);
		
	}
	
	/**
	 * sends the given {@link ChannelDataComponent}
	 * @param channelDataComponent the {@link ChannelDataComponent} to send
	 */
	protected long send(ChannelDataComponent channelDataComponent) {
		return send(channelDataComponent, null);
	}
	
	/**
	 * sends the given {@link ChannelDataComponent}
	 * @param channelDataComponent the {@link ChannelDataComponent} to send
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message this message is a reply to
	 */
	// locks sendLock
	protected long send(ChannelDataComponent channelDataComponent, DiscontinuousBlock discontinuousBlock) {
		
		int channelDataComponentLength = channelDataComponent.length();
		
		sendLock.lock();
		
		// TODO optimize (remove if, call initDataId() where appropriate)
		if(this.dataId < 0) this.dataId = (int)(Math.random() * Integer.MAX_VALUE);
		else this.dataId++;
		long dataId = this.dataId;
		
		sentDataIdCollection.add(dataId);
		
		PacketBackup packetBackup = sentPacketBackupCollection.put(dataId, getChannelId(), FlowControl.PRIO_MGMT);
		Data data = packetBackup.getPacketProperties().data;
		
		Log.debug(this, "sending (discontinuousBlock=%s): %s", discontinuousBlock, channelDataComponent.represent(true));
		
		// this will prepare the Data, write the LinkPacketHeader and return the length of the LinkPacketHeader
		int offset;
		try {
			offset = getLink().writeHeader(dataId, getChannelId(), channelDataComponentLength, data);
		} catch (BufException e) {
			e.printStackTrace();
			sendLock.unlock();
			return 0;
		}
		
		ByteBuf linkPacketBodyByteBuf = new DataByteBuf(data, offset);
		
		if(outBodyTransparentByteBuf != null) {
			outBodyTransparentByteBuf.setByteBuf(linkPacketBodyByteBuf);
			linkPacketBodyByteBuf = outBodyTransparentByteBuf;
		}
		
		try {
			channelDataComponent.write(linkPacketBodyByteBuf);
		} catch (BufException e) {
			e.printStackTrace();
			sendLock.unlock();
			return 0;
		}
		
		packetBackup.getPacketProperties().ready = true;
		
		sendLock.unlock();
		
		getLink().send(packetBackup);
		
		if(discontinuousBlock == null) {
			packetBackup.getResendPacketQueueProperties().setResend(System.nanoTime()/1000000L, 1000, 1.0, 0, resendPacketQueue);
		} else {
			discontinuousBlock.setReplyPacketBackup(packetBackup);
		}
		
		return dataId;
		
	}
	
	/**
	 * unreliably sends the given {@link ChannelDataComponent}
	 * @param channelDataComponent the {@link ChannelDataComponent} to send
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message this message is a reply to
	 */
	// locks sendLock
	protected long sendUnreliable(ChannelDataComponent channelDataComponent) {
		
		int channelDataComponentLength = channelDataComponent.length();
		
		sendLock.lock();
		
		long dataId = (int)(Math.random() * Integer.MAX_VALUE);
		
		PacketBackup packetBackup = unreliablePacketBackupCollection.get(dataId, getUnreliableChannelId(), FlowControl.PRIO_MGMT);
		Data data = packetBackup.getPacketProperties().data;
		
		Log.debug(this, "sending unreliably: %s", channelDataComponent.represent(true));
		
		// this will prepare the Data, write the LinkPacketHeader and return the length of the LinkPacketHeader
		int offset;
		try {
			offset = getLink().writeHeader(dataId, getUnreliableChannelId(), channelDataComponentLength, data);
		} catch (BufException e) {
			e.printStackTrace();
			sendLock.unlock();
			return 0;
		}
		
		ByteBuf linkPacketBodyByteBuf = new DataByteBuf(data, offset);
		
		if(outUnreliableBodyTransparentByteBuf != null) {
			outUnreliableBodyTransparentByteBuf.setByteBuf(linkPacketBodyByteBuf);
			linkPacketBodyByteBuf = outUnreliableBodyTransparentByteBuf;
		}
		
		try {
			channelDataComponent.write(linkPacketBodyByteBuf);
		} catch (BufException e) {
			e.printStackTrace();
			sendLock.unlock();
			return 0;
		}
		
		packetBackup.getPacketProperties().ready = true;
		
		sendLock.unlock();
		
		getLink().send(packetBackup);
		
		return dataId;
		
	}
	
	/**
	 * clears the specified {@link PacketBackup}s from this channel's {@link PacketBackupCollection}
	 * @param startDataId
	 * @param numFollowing
	 */
	protected void clear(long startDataId, int numFollowing) {
		sentPacketBackupCollection.clear(startDataId, numFollowing);
	}
	
	/**
	 * tries to clears the specified {@link PacketBackup}s from this channel's {@link PacketBackupCollection}
	 * @param dataId the data id to try to clear
	 */
	protected void tryClear(long dataId) {
		sentPacketBackupCollection.tryClear(dataId);
	}
	
	@Override
	public final void start() {
		resendThread.start();
		startChannel();
	}
	
	/**
	 * called when the channel is started
	 */
	protected abstract void startChannel();
	/**
	 * called upon receipt of a command
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of this message
	 * @param dataId the data id of this message
	 * @param byteBuf the ByteBuf holding the command
	 * @param length the length of the command
	 * @throws BufException
	 */
	protected abstract void readCommand(DiscontinuousBlock discontinuousBlock, long dataId, ByteBuf byteBuf, int length) throws BufException;
	/**
	 * requests a channel opening from the peer
	 * @param channelId the id of the new channel
	 * @param protocol the protocol of the new channel
	 */
	public abstract void requestOpenChannel(long channelId, String protocol);
	
	/**
	 * reports the existence of a gap in the {@link DiscontinuousBlockCollection} of one of the channels upon packet receipt<br />
	 * called every time a packet is received and a gap exists
	 */
	public abstract void onGapReceive();
	
}
