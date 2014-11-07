package org.dclayer.net.link.channel.data;
import java.util.concurrent.locks.ReentrantLock;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.AsyncPipeByteBuf;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.buf.ListenerByteBuf;
import org.dclayer.net.buf.ListenerByteBufInterface;
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
public abstract class DataChannel extends Channel implements ListenerByteBufInterface {
	
	/**
	 * the {@link AsyncPipeByteBuf} that is used to pipe received data from the {@link DataChannel#read(ByteBuf, int)} method
	 * (which is invoked on the {@link Link} thread) to the {@link DataChannel#readConstantly(ByteBuf)} method
	 * (which is invoked on this {@link DataChannel}'s Thread {@link DataChannel#thread}). 
	 */
	private AsyncPipeByteBuf asyncPipeByteBuf = new AsyncPipeByteBuf(1024);
	
	private int sendDataByteBufSize = 256; // TODO
	private DataByteBuf sendDataByteBuf = new DataByteBuf(sendDataByteBufSize);
	
	private ByteBuf writeByteBuf = new ListenerByteBuf(this);
	
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
	private DiscontinuousBlockCollection receivedDiscontinuousBlockCollection = new DiscontinuousBlockCollection(this, 1024);
	
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
		try {
			asyncPipeByteBuf.write(byteBuf, length);
		} catch(BufException e) {
			Log.exception(this, e);
		}
		Log.debug(this, "... read(length %d) done", length);
	}
	
	/**
	 * sends the given {@link ChannelDataComponent}
	 * @param channelData the {@link Data} to send
	 */
	// locks sendLock
	protected void send(Data channelData) {

		int channelDataComponentLength = channelData.length();

		sendLock.lock();

		// TODO optimize (remove if, call initDataId() where appropriate)
		if(this.dataId < 0) this.dataId = (int)(Math.random() * Integer.MAX_VALUE);
		else this.dataId++;
		long dataId = this.dataId;

		sentDataIdCollection.add(dataId);

		PacketBackup packetBackup = sentPacketBackupCollection.put(dataId, getChannelId(), FlowControl.PRIO_DATA);
		Data data = packetBackup.getPacketProperties().data;

		Log.debug(this, "sending %d bytes", channelDataComponentLength);

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
			linkPacketBodyByteBuf.write(channelData);
		} catch (BufException e) {
			e.printStackTrace();
			sendLock.unlock();
			return;
		}
		
		packetBackup.getPacketProperties().ready = true;

		sendLock.unlock();

		getLink().send(packetBackup, true);

	}
	
	public synchronized void flush() {
		Data data = sendDataByteBuf.getData();
		data.reset(0, sendDataByteBuf.getPosition());
		send(data);
		sendDataByteBuf.prepare(sendDataByteBufSize);
	}
	
	@Override
	public synchronized void onWrite(byte b) throws BufException {
		sendDataByteBuf.write(b);
		if(sendDataByteBuf.getPosition() >= sendDataByteBufSize) {
			flush();
		}
	}
	
	@Override
	public byte onRead() throws BufException {
		return 0;
	}
	
	public ByteBuf getReadByteBuf() {
		return asyncPipeByteBuf;
	}
	
	public ByteBuf getWriteByteBuf() {
		return writeByteBuf;
	}
	
}
