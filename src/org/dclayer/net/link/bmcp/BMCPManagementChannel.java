package org.dclayer.net.link.bmcp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.FlexNum;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.Link.CloseReason;
import org.dclayer.net.link.Link.Status;
import org.dclayer.net.link.bmcp.component.AckBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.BMCPChannelDataComponent;
import org.dclayer.net.link.bmcp.component.BMCPChannelReport;
import org.dclayer.net.link.bmcp.component.BMCPCryptoCommandComponent;
import org.dclayer.net.link.bmcp.component.BMCPIdBlock;
import org.dclayer.net.link.bmcp.component.ChannelBlockStatusReportBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.ChannelBlockStatusReqBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.ConnectConfirmationBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.ConnectCryptoBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.ConnectCryptoEchoReqBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.ConnectEchoReplyBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.ConnectFullEncryptionReqBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.DisconnectBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.KillLinkBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.OpenChannelConfirmationBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.OpenChannelRequestBMCPCommandComponent;
import org.dclayer.net.link.bmcp.component.ThrottleBMCPCommandComponent;
import org.dclayer.net.link.bmcp.crypto.TransparentByteBufGenerator;
import org.dclayer.net.link.bmcp.crypto.component.CryptoDataComponent;
import org.dclayer.net.link.channel.Channel;
import org.dclayer.net.link.channel.ChannelCollection;
import org.dclayer.net.link.channel.management.ManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;
import org.dclayer.net.link.control.idcollection.IdBoundary;
import org.dclayer.net.link.control.idcollection.IdCollection;
import org.dclayer.net.link.control.packetbackup.PacketBackup;
import org.dclayer.net.link.control.packetbackup.PacketBackupCollection;

/**
 * a {@link ManagementChannel} implementation using
 * the basic management channel protocol 
 */
public class BMCPManagementChannel extends ManagementChannel {
	
	/**
	 * {@link ReentrantLock} locked while receiving
	 */
	private ReentrantLock receiveLock = new ReentrantLock();
	/**
	 * {@link ReentrantLock} locked while sending
	 */
	private ReentrantLock sendLock = new ReentrantLock();
	
	/**
	 * the {@link BMCPChannelDataComponent} for writing outbound channel data
	 */
	private BMCPChannelDataComponent outBMCPChannelDataComponent = new BMCPChannelDataComponent();
	/**
	 * the {@link BMCPChannelDataComponent} for reading inbound channel data
	 */
	private BMCPChannelDataComponent inBMCPChannelDataComponent = new BMCPChannelDataComponent();
	
	/**
	 * the currently pending data id
	 */
	private long currentPendingDataId = -1;
	/**
	 * the data id of the current block status request
	 */
	private long channelBlockStatusRequestDataId = -1;
	/**
	 * the data id of the current disconnect command
	 */
	private long disconnectDataId = -1;
	
	/**
	 * the {@link System#nanoTime()} value at the point in time the last throttle message was sent 
	 */
	private long lastThrottleSent = 0;
	/**
	 * the amount of total bytes received on the {@link Link} when the last throttle message was sent
	 */
	private long lastNumBytesReceived = 0;
	/**
	 * the minimum time in nanoseconds to wait after sending a throttle message before sending the next throttle message 
	 */
	private long throttleSendDelayNanos = 250000000L;
	
	private long lastThrottleReceived = 0;
	private long lastNumBytesSent = 0;
	
	/**
	 * if set to true, will disconnect as soon as connected.
	 * (used if disconnect() is called during connection initiation)
	 */
	private boolean disconnect = false;
	
	/**
	 * a Thread for periodically requesting the channel block status from the peer
	 */
	private Thread blockStatusThread = new Thread() {
		@Override
		public void run() {
			for(;;) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Log.exception(BMCPManagementChannel.this, e, "exception in BlockStatusThread");
				}
				Log.debug(BMCPManagementChannel.this, "BlockStatusThread: ChannelCollection: %s", getLink().getChannelCollection().represent(true));
				if(getLink().getStatus() == Link.Status.Connected && channelBlockStatusRequestDataId == -1) {
					requestChannelBlockStatus();
				} else if(getLink().getStatus() == Link.Status.Disconnected) {
					Log.debug(BMCPManagementChannel.this, "BlockStatusThread: exiting");
					return;
				}
			}
		}
	};
	
	/**
	 * creates a new {@link BMCPChannelDataComponent}
	 * @param link the {@link Link} to create this channel on
	 * @param channelId the channel id for this channel
	 * @param channelName the name for this channel
	 */
	public BMCPManagementChannel(Link link, long channelId, long unreliableChannelId, String channelName) {
		super(link, channelId, unreliableChannelId, channelName);
	}
	
	@Override
	public void onOpenChannel(boolean initiator) {
		blockStatusThread.start();
		if(initiator) connect();
	}
	
	// locks receiveLock (theoretically, as long as ManegementChannel synchronizes calls to its read() function, no synchronization is needed here)
	// this is where messages arrive, called from the ManagementChannel superclass
	@Override
	protected void readCommand(DiscontinuousBlock discontinuousBlock, long dataId, ByteBuf byteBuf, int length) throws BufException {
		
		receiveLock.lock();
		
		try {
			inBMCPChannelDataComponent.read(byteBuf);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		process(discontinuousBlock, dataId);
		
		receiveLock.unlock();
		
	}
	
	/**
	 * processes the inbound {@link BMCPChannelDataComponent}
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of this inbound channel data
	 * @param dataId the dataId of the current inbound {@link BMCPChannelDataComponent}
	 */
	// called from within readCommand(), where receiveLock is locked
	private void process(DiscontinuousBlock discontinuousBlock, long dataId) {
		Log.debug(this, "received: %s", inBMCPChannelDataComponent.represent(true));
		inBMCPChannelDataComponent.getBMCPCommandComponent().callOnReceiveMethod(discontinuousBlock, dataId, this);
	}
	
	@Override
	public void onGapReceive() {
		
		long now = System.nanoTime();
		long relativeNanos = now - lastThrottleSent;
		
		if(lastThrottleSent == 0 || relativeNanos > throttleSendDelayNanos) {
			
			long totalBytes = getLink().getNumBytesReceived();
			
			if(lastThrottleSent == 0) {
				
				Log.debug(this, "starting receive rate measuring, sending throttle with maximum transmission rate 0");
				sendThrottle(0);
				
			} else {
				
				long relativeBytes = totalBytes - lastNumBytesReceived;
				long bytesPerSecond_ = (1000000000L*relativeBytes)/relativeNanos;
				long bytesPerSecond = Math.max(10, bytesPerSecond_); // less makes no sense
				
				Log.debug(this, "sending throttle, maximum transmission rate (bytes per second): %d (calculated %d)", bytesPerSecond, bytesPerSecond_);
				
				sendThrottle(bytesPerSecond);
				
			}
			
			lastThrottleSent = now;
			lastNumBytesReceived = totalBytes;
			
		}
		
	}
	
	// --- ONRECEIVE METHODS ---
	
	/**
	 * on receive method for the connect crypto bmcp message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param connectCryptoBMCPCommandComponent the {@link ConnectCryptoBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveConnectCrypto(DiscontinuousBlock discontinuousBlock, long dataId, ConnectCryptoBMCPCommandComponent connectCryptoBMCPCommandComponent) {
		
		Log.msg(this, "onReceiveConnect, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.None) {
			Log.msg(this, "ignoring connect request, link status != %s", Link.Status.None);
			return;
		}
		
		getLink().setStatus(Link.Status.ConnectingPassiveEchoRequested);
		
		// get the cryptoDataComponent that was read from the packet
		BMCPCryptoCommandComponent bmcpCryptoCommandComponent = connectCryptoBMCPCommandComponent.getBMCPCryptoCommandComponent();
		CryptoDataComponent cryptoDataComponent = bmcpCryptoCommandComponent.getCryptoDataComponent();
		
		Log.debug(this, "cryptoMethodIdentifier=%s, applying out-header- and out-body-TransparentByteBufs", cryptoDataComponent.getCryptoMethodIdentifier());
		
		TransparentByteBufGenerator outTransparentByteBufGenerator = cryptoDataComponent.makeTransparentByteBufGenerator();
		getLink().setOutTransparentByteBufGenerator(outTransparentByteBufGenerator);
		
		// generate, set & apply TransparentByteBufs for encryption of sent messages
		getLink().setNewOutHeaderTransparentByteBuf(outTransparentByteBufGenerator.makeLinkPacketHeaderTransparentByteBuf());
		getLink().applyNewOutHeaderTransparentByteBuf();
		// for normal, numbered messages:
		setNewOutBodyTransparentByteBuf(outTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewOutBodyTransparentByteBuf();
		// for unnumbered, unreliably sent messages:
		setNewOutUnreliableBodyTransparentByteBuf(outTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewOutUnreliableBodyTransparentByteBuf();
		
		// generate new parameters for the same crypto method for decryption of received messages
		Log.debug(this, "generating new random parameters...");
		cryptoDataComponent.generateRandomParameters();
		
		TransparentByteBufGenerator inTransparentByteBufGenerator = cryptoDataComponent.makeTransparentByteBufGenerator();
		getLink().setInTransparentByteBufGenerator(inTransparentByteBufGenerator);
		
		// SET new in-header-TransparentByteBuf, but do NOT apply it yet; apply new in-body-TransparentByteBuf
		Log.debug(this, "setting new in-header-TransparentByteBuf and applying new in-body-TransparentByteBuf");
		getLink().setNewInHeaderTransparentByteBuf(inTransparentByteBufGenerator.makeLinkPacketHeaderTransparentByteBuf());
		// for normal, numbered messages:
		setNewInBodyTransparentByteBuf(inTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewInBodyTransparentByteBuf();
		// for unnumbered, unreliably sent messages:
		setNewInUnreliableBodyTransparentByteBuf(inTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewInUnreliableBodyTransparentByteBuf();
		
		int unreliableChannelId;
		do { unreliableChannelId = (int)(Math.random() * Integer.MAX_VALUE); } while(unreliableChannelId == getChannelId());
		this.setUnreliableChannelId(unreliableChannelId);
		getLink().addUnreliableChannelId(this);
		
		sendConnectEchoRequest(unreliableChannelId, bmcpCryptoCommandComponent, discontinuousBlock);
		
	}

	/**
	 * on receive method for the connect crypto echo request message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param connectCryptoEchoReqBMCPCommandComponent the {@link ConnectCryptoEchoReqBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveConnectCryptoEchoReq(DiscontinuousBlock discontinuousBlock, long dataId, ConnectCryptoEchoReqBMCPCommandComponent connectCryptoEchoReqBMCPCommandComponent) {
		
		Log.msg(this, "onReceiveConnectCryptoEchoReq, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.ConnectingActiveConnectRequested) {
			Log.msg(this, "ignoring connect echo request, link status != %s", Link.Status.ConnectingActiveConnectRequested);
			return;
		}
		
		// remove the ConnectCrypto packet that is currently being actively resent from the sent-PacketBackupCollection
		clear(currentPendingDataId, 0);
		
		getLink().setStatus(Link.Status.ConnectingActiveEchoReplied);
		
		long unreliableChannelId = connectCryptoEchoReqBMCPCommandComponent.getUnreliableChannelId();
		this.setUnreliableChannelId(unreliableChannelId);
		Log.debug(this, "unreliableChannelId=%d", unreliableChannelId);
		getLink().addUnreliableChannelId(this);
		
		// get the cryptoDataComponent that was read from the packet
		BMCPCryptoCommandComponent bmcpCryptoCommandComponent = connectCryptoEchoReqBMCPCommandComponent.getBMCPCryptoCommandComponent();
		CryptoDataComponent cryptoDataComponent = bmcpCryptoCommandComponent.getCryptoDataComponent();
		
		Log.debug(this, "cryptoMethodIdentifier=%s, applying out-header and out-body-TransparentByteBufs", cryptoDataComponent.getCryptoMethodIdentifier());
		
		TransparentByteBufGenerator outTransparentByteBufGenerator = cryptoDataComponent.makeTransparentByteBufGenerator();
		getLink().setOutTransparentByteBufGenerator(outTransparentByteBufGenerator);
		
		// set (but do not apply) TransparentByteBuf for encryption of sent message headers
		getLink().setNewOutHeaderTransparentByteBuf(outTransparentByteBufGenerator.makeLinkPacketHeaderTransparentByteBuf());
		
		// set & apply TransparentByteBuf for encryption of sent message bodies
		// for normal, numbered messages:
		setNewOutBodyTransparentByteBuf(outTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewOutBodyTransparentByteBuf();
		// for unnumbered, unreliably sent messages:
		setNewOutUnreliableBodyTransparentByteBuf(outTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewOutUnreliableBodyTransparentByteBuf();
		
		sendConnectEchoReply(connectCryptoEchoReqBMCPCommandComponent.getEchoDataComponent());
		
	}
	
	/**
	 * on receive method for the connect echo reply message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param connectEchoReplyBMCPCommandComponent the {@link ConnectEchoReplyBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveConnectEchoReply(DiscontinuousBlock discontinuousBlock, long dataId, ConnectEchoReplyBMCPCommandComponent connectEchoReplyBMCPCommandComponent) {
		
		Log.msg(this, "onReceiveConnectEchoReply, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.ConnectingPassiveEchoRequested) {
			Log.msg(this, "ignoring connect echo reply, link status != %s", Link.Status.ConnectingPassiveEchoRequested);
			return;
		}
		
		// TODO VERIFY ECHO DATA
		
		getLink().setStatus(Link.Status.ConnectingPassiveFullEncryptionRequested);
		
		// apply the previously prepared in-header-TransparentByteBuf
		getLink().applyNewInHeaderTransparentByteBuf();
		
		sendConnectFullEncryptionRequest();
		
	}
	
	/**
	 * on receive method for the full encryption request message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param connectFullEncryptionReqBMCPCommandComponent the {@link ConnectFullEncryptionReqBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveFullEncryptionRequest(DiscontinuousBlock discontinuousBlock, long dataId, ConnectFullEncryptionReqBMCPCommandComponent connectFullEncryptionReqBMCPCommandComponent) {
		
		Log.msg(this, "onReceiveFullEncryptionRequest, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.ConnectingActiveEchoReplied) {
			Log.msg(this, "ignoring full encryption request, link status != %s", Link.Status.ConnectingActiveEchoReplied);
			return;
		}
		
		// remove the ConnectEchoReply packet that is currently being actively resent from the sent-PacketBackupCollection
		clear(currentPendingDataId, 0);
		
		getLink().applyNewOutHeaderTransparentByteBuf();
		
		sendConnectConfirmation(discontinuousBlock);
		
		onConnected();
		
	}
	
	/**
	 * on receive method for the connect confirmation message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param connectConfirmationBMCPCommandComponent the {@link ConnectConfirmationBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveConnectConfirmation(DiscontinuousBlock discontinuousBlock, long dataId, ConnectConfirmationBMCPCommandComponent connectConfirmationBMCPCommandComponent) {
		
		Log.msg(this, "onReceiveConnectConfirmation, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.ConnectingPassiveFullEncryptionRequested) {
			Log.msg(this, "ignoring connect echo request, link status != %s", Link.Status.ConnectingPassiveFullEncryptionRequested);
			return;
		}
		
		// remove the FullEncryptionRequest packet that is currently being actively resent from the sent-PacketBackupCollection
		clear(currentPendingDataId, 0);
		
		onConnected();
		
	}
	
	/**
	 * on receive method for the channel block status request message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param channelBlockStatusReqBMCPCommandComponent the {@link ChannelBlockStatusReqBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveChannelBlockStatusRequest(DiscontinuousBlock discontinuousBlock, long dataId, ChannelBlockStatusReqBMCPCommandComponent channelBlockStatusReqBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveChannelBlockStatusRequest, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.Connected) {
			Log.debug(this, "ignoring channel block status request, link status != %s", Link.Status.Connected);
			return;
		}
		
		// TODO reply with reports for the channels requested only, instead of all channels
		
		sendChannelBlockStatusReport(discontinuousBlock);
		
	}
	
	/**
	 * on receive method for the channel block status report message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param channelBlockStatusReportBMCPCommandComponent the {@link ChannelBlockStatusReportBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveChannelBlockStatusReport(DiscontinuousBlock discontinuousBlock, long dataId, ChannelBlockStatusReportBMCPCommandComponent channelBlockStatusReportBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveChannelBlockStatusReport, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.Connected) {
			Log.debug(this, "ignoring channel block status report, link status != %s", Link.Status.Connected);
			return;
		}
		
		if(channelBlockStatusRequestDataId != -1) {
			clear(channelBlockStatusRequestDataId, 0);
			channelBlockStatusRequestDataId = -1;
		}
		
		final long numFollowingChannels = channelBlockStatusReportBMCPCommandComponent.getNumFollowingChannels();
		long i = 0;
		
		for(BMCPChannelReport channelReport : channelBlockStatusReportBMCPCommandComponent.getChannelReports()) {
			
			if(i++ >= numFollowingChannels) break;
			
			long channelId = channelReport.getChannelId();
			
			Channel channel = getLink().getChannelCollection().get(channelId);
			if(channel == null) continue;
			
			IdCollection idCollection = channel.getSentDataIdCollection();
			if(idCollection == null) continue;
			
			PacketBackupCollection packetBackupCollection = channel.getSentPacketBackupCollection();
			if(packetBackupCollection == null) continue;
			
			PacketBackup packetBackup;
			
			long lowestDataId = channelReport.getLowestDataId();
			long highestDataId = channelReport.getHighestDataId();
			final long numDataIds = channelReport.getNumDataIds();
			
			final long numMissingSingleIds = channelReport.getNumMissingSingleIds();
			final long numMissingIdBlocks = channelReport.getNumMissingIdBlocks();
			
			final long realLowestDataId = idCollection.getLowestId();
			final long realHighestDataId = idCollection.getHighestId();
			
			boolean clear = numDataIds > 0; // whether or not we can clear anything from PacketBackupCollection
			long clearUpTo = highestDataId; // what id to clear PacketBackupCollection up to
			
			if(numDataIds > 0) {
				
				// normal case: peer has received some packets, but not all.
				
				if(lowestDataId > realLowestDataId && lowestDataId <= realHighestDataId) {
					clear = false; // we can not clear anything since peer is missing ids at the very beginning
					do {
						lowestDataId--;
						if((packetBackup = packetBackupCollection.get(lowestDataId)) != null) {
							channel.resend(packetBackup);
						}
					} while(lowestDataId > realLowestDataId);
				}
				
				while(highestDataId < realHighestDataId && highestDataId >= realLowestDataId) {
					highestDataId++;
					if((packetBackup = packetBackupCollection.get(highestDataId)) != null) {
						channel.resend(packetBackup);
					}
				}
				
			} else if(numDataIds < idCollection.getNumIds()) {
				
				// special case: we have sent packets, but peer did not receive any of them
				
				for(IdBoundary idBoundary : idCollection) {
					for(long curDataId = idBoundary.boundaryStart; curDataId < idBoundary.boundaryPostEnd; curDataId++) {
						packetBackup = packetBackupCollection.get(curDataId);
						channel.resend(packetBackup);
					}
				}
				
				
			}
			
			long j = 0;
			for(FlexNum singleId : channelReport.getMissingSingleIds()) {
				if(j++ >= numMissingSingleIds) break;
				final long missingDataId = singleId.getNum();
				if(missingDataId >= realLowestDataId && missingDataId <= realHighestDataId && (packetBackup = packetBackupCollection.get(missingDataId)) != null) {
					if(missingDataId <= clearUpTo) clearUpTo = (missingDataId-1); // can not clear beyond this id
					channel.resend(packetBackup);
				}
			}
			
			j = 0;
			for(BMCPIdBlock idBlock : channelReport.getMissingIdBlocks()) {
				if(j++ >= numMissingIdBlocks) break;
				long missingDataId = idBlock.getStartId();
				final long maxDataId = missingDataId + idBlock.getInnerSize() + 1;
				if(missingDataId >= realLowestDataId && maxDataId <= realHighestDataId) {
					if(missingDataId <= clearUpTo) clearUpTo = (missingDataId-1); // can not clear beyond this id
					while(missingDataId <= maxDataId) {
						packetBackup = packetBackupCollection.get(missingDataId);
						channel.resend(packetBackup);
						missingDataId++;
					}
				}
			}
			
			Log.debug(this, "CHANNEL %d: CAN (clear=%s) CLEAR UP TO DATAID %d", channelId, clear, clearUpTo);
			
			if(clear) {
				packetBackupCollection.clearUpTo(clearUpTo);
			}
			
		}
		
	}
	
	/**
	 * on receive method for the open channel request message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param openChannelRequestBMCPCommandComponent the {@link OpenChannelRequestBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveOpenChannelRequest(DiscontinuousBlock discontinuousBlock, long dataId, OpenChannelRequestBMCPCommandComponent openChannelRequestBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveOpenChannelRequest, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.Connected) {
			Log.debug(this, "ignoring open channel request, link status != %s", Link.Status.Connected);
			return;
		}
		
		// TODO optimize (directly copy over PacketComponents to confirmation message)
		long channelId = openChannelRequestBMCPCommandComponent.getChannelId();
		String protocol = openChannelRequestBMCPCommandComponent.getProtocol();
		
		Log.msg(this, "peer wants to open channelId %d with protocol: %s", channelId, protocol);
		
		if(getLink().onOpenChannelRequest(channelId, protocol)) {
			
			// channel can be opened
			Log.msg(this, "confirming open channel request for channelId %d with protocol: %s", channelId, protocol);
			sendOpenChannelConfirmation(channelId, protocol, dataId, discontinuousBlock);
			
		} else {
			
			// channel can not be opened
			Log.msg(this, "ignoring open channel request for channelId %d with protocol: %s", channelId, protocol);
			
		}
		
	}
	
	/**
	 * on receive method for the open channel confirmation message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param openChannelConfirmationBMCPCommandComponent the {@link OpenChannelConfirmationBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveOpenChannelConfirmation(DiscontinuousBlock discontinuousBlock, long dataId, OpenChannelConfirmationBMCPCommandComponent openChannelConfirmationBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveOpenChannelConfirmation, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.Connected) {
			Log.debug(this, "ignoring open channel confirmation, link status != %s", Link.Status.Connected);
			return;
		}
		
		// TODO check if we really requested a new channel
		
		long channelId = openChannelConfirmationBMCPCommandComponent.getChannelId();
		
		Channel channel = getLink().getChannelCollection().get(channelId);
		
		Log.msg(this, "opening of channel id %d (%s) confirmed", channelId, channel);
		
		if(channel != null) {
			
			if(channel.isOpen()) {
				Log.msg(this, "channel %s is already open", channel);
			} else {
				channel.open(true);
			}
			
		}

		clear(openChannelConfirmationBMCPCommandComponent.getAckDataId(), 0);
		
	}
	
	/**
	 * on receive method for the throttle message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param throttleBMCPCommandComponent the {@link ThrottleBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveThrottle(DiscontinuousBlock discontinuousBlock, long dataId, ThrottleBMCPCommandComponent throttleBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveThrottle, link status: %s", getLink().getStatus());
		if(getLink().getStatus() != Link.Status.Connected) {
			Log.debug(this, "ignoring throttle message, link status != %s", Link.Status.Connected);
			return;
		}
		
		long now = System.nanoTime();
		long numBytesSent = getLink().getNumBytesSent();
		
		long originalBytesPerSecond = throttleBMCPCommandComponent.getBytesPerSecond();
		
		if(originalBytesPerSecond == 0) {
			
			Log.debug(this, "throttle message with maximum transmission rate 0 received, disabling transmission rate limitation");
			getLink().setFlowControlBytesPerSecond(0);
			
		} else {
			
			if(lastThrottleReceived == 0) {
				lastThrottleReceived = getLink().getStartedReceiving();
			}
			
			long relativeBytesSent = numBytesSent - lastNumBytesSent;
			long relativeNanos = now - lastThrottleReceived;
			long bytesPerSecondSent = (1000000000L*relativeBytesSent)/relativeNanos;
			long currentBytesPerSecond = getLink().getFlowControlBytesPerSecond();
			
			if(originalBytesPerSecond >= bytesPerSecondSent && (originalBytesPerSecond < currentBytesPerSecond || currentBytesPerSecond == 0)) {
				// don't apply a maximum transmission rate bigger than what we were sending
				Log.debug(this, "throttle message received (bytes per second requested by remote: %d), ignoring because bigger than sent rate (%d) and smaller than current maximum transmission rate (%d)", originalBytesPerSecond, bytesPerSecondSent, currentBytesPerSecond);
			} else {
				long bytesPerSecond = Math.max(originalBytesPerSecond, 10);
				Log.debug(this, "throttle message received, applying maximum transmission rate (bytes per second): %d (requested by remote: %d)", bytesPerSecond, originalBytesPerSecond);
				getLink().setFlowControlBytesPerSecond(bytesPerSecond);
			}
		}
		
		lastThrottleReceived = now;
		lastNumBytesSent = numBytesSent;
		
	}
	
	/**
	 * on receive method for the acknowledgement message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message
	 * @param dataId the data id of this message
	 * @param ackBMCPCommandComponent the {@link AckBMCPCommandComponent} that was received
	 */
	// no synchronization needed, this is called from process() which is already synchronized
	public void onReceiveAck(DiscontinuousBlock discontinuousBlock, long dataId, AckBMCPCommandComponent ackBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveAck, link status (not relevant): %s", getLink().getStatus());
		
		long ackDataId = ackBMCPCommandComponent.getAckDataId();
		Log.debug(this, "received acknowledgement for data id %d", ackDataId);
		
		tryClear(ackDataId);
		
	}
	
	public void onReceiveDisconnect(DiscontinuousBlock discontinuousBlock, long dataId, DisconnectBMCPCommandComponent disconnectBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveDisconnect, link status: %s", getLink().getStatus());
		
		onDisconnected();
		sendKillLink(discontinuousBlock);
		
	}
	
	public void onReceiveKillLink(DiscontinuousBlock discontinuousBlock, long dataId, KillLinkBMCPCommandComponent killLinkBMCPCommandComponent) {
		
		Log.debug(this, "onReceiveKillLink, link status: %s", getLink().getStatus());
		
		onRemoteKill();
		
	}
	
	// --- ACTION INITIATION METHODS, CALLED FROM OUTSIDE ---
	
	/**
	 * starts this {@link BMCPManagementChannel} and connects to the peer
	 */
	private void connect() {
		
		sendConnect();
		
	}
	
	@Override
	public void disconnect() {
		
		getLink().lockReceive();
		
		if(getLink().getStatus().connected()) {
			
			disconnectNow();
			
		} else if(getLink().getStatus().connecting()) {
			
			disconnect = true;
			
		}
		
		getLink().unlockReceive();
		
	}
	
	private void disconnectNow() {
		getLink().setStatus(Status.Disconnecting);
		sendDisconnect();
	}
	
	private void onConnected() {

		if(disconnect) {
			Log.debug(this, "connected, but disconnect is true, disconnecting");
			disconnectNow();
		} else {
			Log.debug(this, "connected");
			getLink().onConnected();
		}
		
	}
	
	private void onExit() {
		exitResendThread();
	}
	
	private void onDisconnected() {
		
		onExit();
		getLink().onDisconnected();
		
	}
	
	private void onRemoteKill() {
		
		onExit();
		if(getLink().getStatus().disconnecting()) {
			onDisconnected();
		} else {
			onKilled(CloseReason.RemoteKill);
		}
		
	}
	
	private void onKilled(CloseReason closeReason) {
		Log.debug(this, "killed, CloseReason: %s", closeReason);
		getLink().kill(closeReason);
	}
	
	@Override
	public void requestOpenChannel(long channelId, String protocol) {

		sendOpenChannelRequest(channelId, protocol);
		
	}
	
	/**
	 * requests the channel block status for all channels from the peer
	 */
	public void requestChannelBlockStatus() {
		
		sendChannelBlockStatusRequest();
		
	}
	
	// --- SEND METHODS ---
	
	/**
	 * sends a connect crypto message
	 */
	// locks sendLock
	private void sendConnect() {
		
		CryptoDataComponent cryptoDataComponent;
		
		sendLock.lock();
		
		(cryptoDataComponent =
		outBMCPChannelDataComponent
		.setConnectCryptoBMCPCommandComponent() // CONNECT_CRYPTO command
		.getBMCPCryptoCommandComponent() // gets the CryptoCommandComponent (containing crypto information)
		.setNoCryptoDataComponent()) // enables CRYPTO_METHOD_NONE
//		.setRotCryptoDataComponent()) // enables CRYPTO_METHOD_ROT
		.generateRandomParameters(); // initializes crypto method
		
		Log.debug(this, "connecting, applying initial in-header-TransparentByteBuf and in-body-TransparentByteBuf");
		TransparentByteBufGenerator inTransparentByteBufGenerator = cryptoDataComponent.makeTransparentByteBufGenerator();
		getLink().setInTransparentByteBufGenerator(inTransparentByteBufGenerator);
		// immediately apply both
		getLink().setNewInHeaderTransparentByteBuf(inTransparentByteBufGenerator.makeLinkPacketHeaderTransparentByteBuf());
		getLink().applyNewInHeaderTransparentByteBuf();
		// for normal, numbered messages:
		setNewInBodyTransparentByteBuf(inTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewInBodyTransparentByteBuf();
		// for unnumbered, unreliably sent messages:
		setNewInUnreliableBodyTransparentByteBuf(inTransparentByteBufGenerator.makeLinkPacketBodyTransparentByteBuf());
		applyNewInUnreliableBodyTransparentByteBuf();
		
		currentPendingDataId = send(outBMCPChannelDataComponent);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends a connect echo request message
	 * @param unreliableChannelId the channel id to use when sending unreliable messages over this {@link BMCPManagementChannel}
	 * @param bmcpCryptoCommandComponent  the {@link BMCPCryptoCommandComponent} to include in the message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message this is a response to
	 */
	// locks sendLock
	private void sendConnectEchoRequest(long unreliableChannelId, BMCPCryptoCommandComponent bmcpCryptoCommandComponent, DiscontinuousBlock discontinuousBlock) {
		
		ConnectCryptoEchoReqBMCPCommandComponent connectCryptoEchoReqBMCPCommandComponent;
		
		sendLock.lock();
		
		(connectCryptoEchoReqBMCPCommandComponent = 
		outBMCPChannelDataComponent
		.setConnectCryptoEchoReqBMCPCommandComponent()) // CONNECT_CRYPTO_ECHO_REQ command
		.setBMCPCryptoCommandComponent(bmcpCryptoCommandComponent); // sets the CryptoCommandComponent to the given one
		
		connectCryptoEchoReqBMCPCommandComponent.setUnreliableChannelId(unreliableChannelId);
		
		connectCryptoEchoReqBMCPCommandComponent.getEchoDataComponent().setData(new Data(new byte[] {
				(byte)1, (byte)3, (byte)3, (byte)7
		}));
		
		send(outBMCPChannelDataComponent, discontinuousBlock);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends a connect echo reply message
	 * @param echoDataComponent the {@link DataComponent} to echo
	 */
	// locks sendLock
	private void sendConnectEchoReply(DataComponent echoDataComponent) {
		
		sendLock.lock();
		
		outBMCPChannelDataComponent
		.setConnectEchoReplyBMCPCommandComponent()
		.setEchoDataComponent(echoDataComponent);
		
		currentPendingDataId = send(outBMCPChannelDataComponent);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends a connect full encryption request message
	 */
	// locks sendLock
	private void sendConnectFullEncryptionRequest() {
		
		sendLock.lock();
		
		outBMCPChannelDataComponent
		.setConnectFullEncryptionReqBMCPCommandComponent();
		
		currentPendingDataId = send(outBMCPChannelDataComponent);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends a connect confirmation message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message this is a response to
	 */
	// locks sendLock
	private void sendConnectConfirmation(DiscontinuousBlock discontinuousBlock) {
		
		sendLock.lock();
		
		outBMCPChannelDataComponent
		.setConnectConfirmationBMCPCommandComponent();
		
		// TODO verify out-comment
		/*currentPendingDataId = */send(outBMCPChannelDataComponent, discontinuousBlock);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends a channel block status request for all channels
	 */
	// locks sendLock
	private void sendChannelBlockStatusRequest() {
		
		sendLock.lock();
		
		outBMCPChannelDataComponent
		.setChannelBlockStatusReqBMCPCommandComponent()
		.setNumFollowingChannelIds(0);
		
		channelBlockStatusRequestDataId = send(outBMCPChannelDataComponent);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends a channel block status report for all channels
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message this is a response to
	 */
	// locks sendLock
	private void sendChannelBlockStatusReport(DiscontinuousBlock discontinuousBlock) {
		
		sendLock.lock();
		
		ChannelBlockStatusReportBMCPCommandComponent channelBlockStatusReportBMCPCommandComponent =
		
		outBMCPChannelDataComponent
		.setChannelBlockStatusReportBMCPCommandComponent();
		
		// LOCK LINK'S RECEIVELOCK
		Log.debug(this, "sendChannelBlockStatusReport: locking Link's receiveLock...");
		// lock the link's receiveLock, so no changes occur to any channels.
		getLink().lockReceive();
		Log.debug(this, "sendChannelBlockStatusReport: locked Link's receiveLock, building channel block status report...");
		
		ChannelCollection channelCollection = getLink().getChannelCollection();
		List<Channel> channels = channelCollection.getChannels();
		
		LinkedList<BMCPChannelReport> reports = channelBlockStatusReportBMCPCommandComponent.getChannelReports();

		final int numReports = reports.size();
		Iterator<BMCPChannelReport> reportIterator = reports.iterator();
		int i = 0;
		
		long numChannels = 0;

		for(Channel channel : channels) {

			IdCollection idCollection = channel.getReceivedDataIdCollection();
			if(idCollection == null) continue;
			
			numChannels++;
			
			BMCPChannelReport report;
			if(i++ < numReports) {
				report = reportIterator.next();
			} else {
				report = new BMCPChannelReport();
				reports.add(report);
			}

			report.setChannelId(channel.getChannelId());

			LinkedList<FlexNum> missingSingleIds = report.getMissingSingleIds();
			LinkedList<BMCPIdBlock> missingIdBlocks = report.getMissingIdBlocks();

			Iterator<FlexNum> singleIdIterator = missingSingleIds.iterator();
			Iterator<BMCPIdBlock> idBlockIterator = missingIdBlocks.iterator();

			final int missingSingleIdsSize = missingSingleIds.size();
			final int missingIdBlocksSize = missingIdBlocks.size();

			long numMissingSingleIds = 0;
			long numMissingIdBlocks = 0;
			
			synchronized(idCollection) {

				long lowestId = idCollection.getLowestId();
				report.setLowestDataId(lowestId < 0 ? 0 : lowestId);
				long highestId = idCollection.getHighestId();
				report.setHighestDataId(highestId < 0 ? 0 : highestId);
				
				report.setNumDataIds(idCollection.getNumIds());

				Iterator<IdBoundary> boundaryIterator = idCollection.iterator();
				IdBoundary lastBoundary = boundaryIterator.hasNext() ? boundaryIterator.next() : null;
				while(boundaryIterator.hasNext()) {

					IdBoundary curBoundary = boundaryIterator.next();
					if(curBoundary.boundaryStart > lastBoundary.boundaryPostEnd) {

						if((curBoundary.boundaryStart - lastBoundary.boundaryPostEnd) > 1) {

							// block
							BMCPIdBlock idBlock;
							numMissingIdBlocks++;
							if(numMissingIdBlocks > missingIdBlocksSize) {
								// append a new block to the linked list
								idBlock = new BMCPIdBlock();
								missingIdBlocks.add(idBlock);
							} else {
								// use an existing block from the linked list
								idBlock = idBlockIterator.next();
							}

							idBlock.setStartId(lastBoundary.boundaryPostEnd);
							idBlock.setInnerSize(curBoundary.boundaryStart - lastBoundary.boundaryPostEnd - 2);

						} else {

							// single id
							FlexNum singleId;
							numMissingSingleIds++;
							if(numMissingSingleIds > missingSingleIdsSize) {
								// append a new single id to the linked list
								singleId = new FlexNum();
								missingSingleIds.add(singleId);
							} else {
								// use an existing single id from the linked list
								singleId = singleIdIterator.next();
							}

							singleId.setNum(lastBoundary.boundaryPostEnd);

						}

					}

					lastBoundary = curBoundary;
				}

			}

			report.setNumMissingIdBlocks(numMissingIdBlocks);
			report.setNumMissingSingleIds(numMissingSingleIds);

		}
		
		channelBlockStatusReportBMCPCommandComponent.setNumFollowingChannels(numChannels);

		// UNLOCK LINK'S RECEIVELOCK
		Log.debug(this, "sendChannelBlockStatusReport: built channel block status report, unlocking Link's receiveLock...");
		getLink().unlockReceive();
		Log.debug(this, "sendChannelBlockStatusReport: unlocked Link's receiveLock.");
		
		send(outBMCPChannelDataComponent, discontinuousBlock);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends an open channel request message for a new channel with the given channel id and protocol
	 * @param channelId the channel id of the new channel
	 * @param protocol the protocol for the new channel
	 */
	// locks sendLock
	private void sendOpenChannelRequest(long channelId, String protocol) {
		
		sendLock.lock();
		
		OpenChannelRequestBMCPCommandComponent openChannelRequestBMCPCommandComponent =

		outBMCPChannelDataComponent
		.setOpenChannelRequestBMCPCommandComponent();

		openChannelRequestBMCPCommandComponent.setChannelId(channelId);
		openChannelRequestBMCPCommandComponent.setProtocol(protocol);

		send(outBMCPChannelDataComponent);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends an open channel confirmation message for a new channel
	 * @param channelId the channel id of the new channel
	 * @param protocol the protocol of the new channel
	 * @param inResponseToDataId the data id of the preceding open channel request message
	 * @param discontinuousBlock the {@link DiscontinuousBlock} of the message this is a response to
	 */
	// locks sendLock
	private void sendOpenChannelConfirmation(long channelId, String protocol, long inResponseToDataId, DiscontinuousBlock discontinuousBlock) {
		
		sendLock.lock();
		
		OpenChannelConfirmationBMCPCommandComponent openChannelConfirmationBMCPCommandComponent =
		
		outBMCPChannelDataComponent
		.setOpenChannelConfirmationBMCPCommandComponent();
		
		openChannelConfirmationBMCPCommandComponent.setAckDataId(inResponseToDataId);
		openChannelConfirmationBMCPCommandComponent.setChannelId(channelId);
		openChannelConfirmationBMCPCommandComponent.setProtocol(protocol);
		
		send(outBMCPChannelDataComponent, discontinuousBlock);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends an acknowledgement message with the given data id
	 * @param ackDataId the data id to acknowledge
	 */
	// locks sendLock
	private void sendAck(long ackDataId, DiscontinuousBlock discontinuousBlock) {
		
		sendLock.lock();
		
		AckBMCPCommandComponent ackBMCPCommandComponent =
		
		outBMCPChannelDataComponent
		.setAckBMCPCommandComponent();
		
		ackBMCPCommandComponent.setAckDataId(ackDataId);
		
		send(outBMCPChannelDataComponent, discontinuousBlock);
		
		sendLock.unlock();
		
	}
	
	/**
	 * sends a throttle message with the given bytes per second value
	 * @param bytesPerSecond the bytes per second value to include in the throttle message
	 */
	// locks sendLock
	private void sendThrottle(long bytesPerSecond) {
		
		sendLock.lock();
		
		ThrottleBMCPCommandComponent throttleBMCPCommandComponent =
		
		outBMCPChannelDataComponent
		.setThrottleBMCPCommandComponent();
		
		throttleBMCPCommandComponent.setBytesPerSecond(bytesPerSecond);
		
		sendUnreliable(outBMCPChannelDataComponent);
		
		sendLock.unlock();
		
	}
	
	// locks sendLock
	private void sendDisconnect() {
		
		sendLock.lock();
		
		outBMCPChannelDataComponent
		.setDisconnectBMCPCommandComponent();
		
		disconnectDataId = send(outBMCPChannelDataComponent);
		
		sendLock.unlock();
		
	}
	
	// locks sendLock
	private void sendKillLink(DiscontinuousBlock discontinuousBlock) {
		
		sendLock.lock();
		
		outBMCPChannelDataComponent
		.setKillLinkBMCPCommandComponent();
		
		send(outBMCPChannelDataComponent, discontinuousBlock);
		
		sendLock.unlock();
		
	}

	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub
		
	}
	
}
