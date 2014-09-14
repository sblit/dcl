package org.dclayer.net.link.channel.data;
import java.util.concurrent.locks.ReentrantLock;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.buf.SyncPipeByteBuf;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.channel.Channel;
import org.dclayer.net.link.channel.component.ChannelDataComponent;
import org.dclayer.net.link.channel.management.ManagementChannel;
import org.dclayer.net.link.control.FlowControl;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlockCollection;
import org.dclayer.net.link.control.idcollection.IdCollection;
import org.dclayer.net.link.control.packetbackup.PacketBackup;
import org.dclayer.net.link.control.packetbackup.PacketBackupCollection;

/**
 * abstract base class for all {@link Channel} implementations that are no management channels
 * (i.e. all {@link Channel}s on a {@link Link} except its {@link ManagementChannel})
 */
public abstract class DataChannel extends Channel implements Runnable {
	
	/**
	 * Thread that is calling the abstract {@link DataChannel#readConstantly(ByteBuf)} function
	 */
	private Thread thread = new Thread(this);
	/**
	 * the {@link SyncPipeByteBuf} that is used to pipe received data from the {@link DataChannel#read(ByteBuf, int)} method
	 * (which is invoked on the {@link Link} thread) to the {@link DataChannel#readConstantly(ByteBuf)} method
	 * (which is invoked on this {@link DataChannel}'s Thread {@link DataChannel#thread}). 
	 */
	private SyncPipeByteBuf syncPipeByteBuf = new SyncPipeByteBuf();
	
	/**
	 * the data id of the last packet
	 */
	private long dataId = -1;
	
	/**
	 * {@link IdCollection} of all sent data ids
	 */
	private IdCollection sentDataIdCollection = new IdCollection();
	/**
	 * {@link PacketBackupCollection} of sent packets
	 */
	private PacketBackupCollection sentPacketBackupCollection = new PacketBackupCollection(this, 1024);
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
	 * {@link ReentrantLock} locked while receiving
	 */
	private ReentrantLock receiveLock = new ReentrantLock();
	/**
	 * {@link ReentrantLock} locked while sending
	 */
	private ReentrantLock sendLock = new ReentrantLock();
	
	/**
	 * {@link DataByteBuf} used for reading from {@link DiscontinuousBlock}s
	 */
	private DataByteBuf dataByteBuf = new DataByteBuf();
	
	public DataChannel(Link link, long channelId, String channelName) {
		super(link, channelId, channelName);
	}

	@Override
	public void start() {
		thread.start();
	}
	
	@Override
	public IdCollection getSentDataIdCollection() {
		return sentDataIdCollection;
	}
	
	@Override
	public PacketBackupCollection getSentPacketBackupCollection() {
		return sentPacketBackupCollection;
	}
	
	@Override
	public IdCollection getReceivedDataIdCollection() {
		return receivedDataIdCollection;
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
	@Override
	public void receiveLinkPacketBody(long dataId, long channelId, ByteBuf byteBuf, int length) throws BufException {

		receiveLock.lock();

		long dataIdOffset = receivedDiscontinuousBlockCollection.getDataIdOffset();

		if(dataId < dataIdOffset) {

			Log.debug(this, "received a dataId (%d) below this channel's dataIdOffset (%d), ignoring", dataId, dataIdOffset);

		} else {

			receivedDataIdCollection.add(dataId);

			if(receivedDiscontinuousBlockCollection.isEmpty() && (dataIdOffset < 0 || dataId == dataIdOffset)) {

				// the received block is in order, read directly & set dataIdOffset+1
				readEncrypted(byteBuf, length);
				receivedDiscontinuousBlockCollection.setDataIdOffset(dataId + 1);

			} else {

				// one or more blocks are missing between the last and this one, buffer this one until we receive the missing one(s)
				if(receivedDiscontinuousBlockCollection.put(dataId, byteBuf, length)) {
					
					// put() returned true, there is no gap anymore after the last successfully read block -> read
					do {

						Data data = receivedDiscontinuousBlockCollection.clearFirst().getData();
						dataByteBuf.setData(data);
						readEncrypted(dataByteBuf, data.length());

					} while(receivedDiscontinuousBlockCollection.available());
					
				} else {
					
					// the gap exists, report
					getLink().onGapReceive();
					
				}

			}

		}

		receiveLock.unlock();
		
	}

	/**
	 * reads the (encrypted) packet body, encrypting it
	 * @param byteBuf the {@link ByteBuf} holding packet body data
	 * @param length the length of the packet body data
	 * @throws BufException
	 */
	// no need for synchronization, this is called by receivedLinkPacketBody, which locks receiveLock
	protected void readEncrypted(ByteBuf byteBuf, int length) throws BufException {
		if(inBodyTransparentByteBuf != null) {
			inBodyTransparentByteBuf.setByteBuf(byteBuf);
			this.read(inBodyTransparentByteBuf, length);
		} else {
			this.read(byteBuf, length);
		}
	}
	
	/**
	 * processes the decrypted packet body data, passing it on to {@link DataChannel#syncPipeByteBuf}
	 * @param byteBuf the {@link ByteBuf} holding the decrypted packet body data
	 * @param length the length of the decrypted packet body data
	 */
	private void read(ByteBuf byteBuf, int length) {
		Log.debug(this, "read(length %d) ...", length);
		// TODO
		// DO NOT REMOVE THE SYNC-PIPE-BYTEBUF! synchronization in Link can not handle both Sync- and AsyncPipeByteBuf!
		// EDIT 2014-06-09: It should be able to now, since using Locks
		syncPipeByteBuf.write(byteBuf, length);
		Log.debug(this, "... read(length %d) done", length);
	}
	
	/**
	 * called from {@link DataChannel#thread}
	 */
	@Override
	public void run() {
		readConstantly(syncPipeByteBuf);
		// TODO if this returns, close the channel
	}
	
	/**
	 * sends the given {@link ChannelDataComponent}
	 * @param channelDataComponent the {@link ChannelDataComponent} to send
	 */
	// locks sendLock
	protected void send(ChannelDataComponent channelDataComponent) {

		int channelDataComponentLength = channelDataComponent.length();

		sendLock.lock();

		// TODO optimize (remove if, call initDataId() where appropriate)
		if(this.dataId < 0) this.dataId = (int)(Math.random() * Integer.MAX_VALUE);
		else this.dataId++;
		long dataId = this.dataId;

		sentDataIdCollection.add(dataId);

		PacketBackup packetBackup = sentPacketBackupCollection.put(dataId, getChannelId(), FlowControl.PRIO_DATA);
		Data data = packetBackup.getPacketProperties().data;

		Log.debug(this, "sending: %s", channelDataComponent.represent(true));

		// this will prepare the Data, write the LinkPacketHeader and return the length of the LinkPacketHeader
		int offset;
		try {
			offset = getLink().writeHeader(dataId, getChannelId(), channelDataComponentLength, data);
		} catch (BufException e) {
			e.printStackTrace();
			sendLock.unlock();
			return;
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
			return;
		}
		
		packetBackup.getPacketProperties().ready = true;

		sendLock.unlock();

		getLink().send(packetBackup, true);

	}
	
	/**
	 * called by this {@link DataChannel}'s Thread {@link DataChannel#thread}.<br />
	 * only return from this method if this channel should be closed.
	 * @param byteBuf
	 */
	public abstract void readConstantly(ByteBuf byteBuf);
	
}
