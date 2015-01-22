package org.dclayer.net.link.channel;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.Link.CloseReason;
import org.dclayer.net.link.control.FlowControl;
import org.dclayer.net.link.control.idcollection.IdCollection;
import org.dclayer.net.link.control.packetbackup.FlowControlProperties;
import org.dclayer.net.link.control.packetbackup.PacketBackup;
import org.dclayer.net.link.control.packetbackup.PacketBackupCollection;

/**
 * a channel of a link
 */
public abstract class Channel implements HierarchicalLevel {
	
	/**
	 * the {@link Link} this channel belongs to
	 */
	private Link link;
	/**
	 * the channel id that identifies this channel
	 */
	private final long channelId; // TODO if this becomes dynamic some time in the future, care for synchronization
	/**
	 * the name of this channel
	 */
	private String channelName;
	
	private boolean open = false;
	
	public Channel(Link link, long channelId, String channelName) {
		this.link = link;
		this.channelId = channelId;
		this.channelName = channelName;
	}
	
	/**
	 * @return the {@link Link} this channel belongs to
	 */
	public Link getLink() {
		return link;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public void open(boolean initiator) {
		this.open = true;
		onOpen(initiator);
	}
	
	@Override
	public String toString() {
		return String.format("Channel %d (%s)", channelId, channelName);
	}
	
	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		return link;
	}
	
	/**
	 * @return the channel name
	 */
	public String getChannelName() {
		return channelName;
	}
	
	/**
	 * @return the channel id
	 */
	public long getChannelId() {
		return channelId;
	}
	
	/**
	 * @return the unreliable channel id of this {@link Channel} or -1 if nonexistent
	 */
	public long getUnreliableChannelId() {
		return -1;
	}
	
	/**
	 * resends a {@link PacketBackup}
	 * @param packetBackup the {@link PacketBackup} to resend
	 */
	public void resend(PacketBackup packetBackup) {
		FlowControlProperties flowControlProperties = packetBackup.getFlowControlProperties();
		synchronized(flowControlProperties) {
			if(FlowControl.PRIO_RESEND < flowControlProperties.priority) flowControlProperties.priority = FlowControl.PRIO_RESEND; // set resend priority
		}
		Log.debug(this, "resending %s", packetBackup);
		getLink().send(packetBackup);
	}
	
	/**
	 * called when this channel is actually opened (either requested by the
	 * remote and accepted or requested locally and confirmed by the remote)
	 * @param initiator true if we requested this channel, false if the remote requested it
	 */
	protected abstract void onOpen(boolean initiator);
	/**
	 * called when this channel is closed
	 */
	public abstract void onClose();
	/**
	 * called upon receipt of a link packet destined on this channel
	 * @param dataId the data id of the link packet
	 * @param channelId the channel id of the link packet
	 * @param byteBuf the {@link ByteBuf} holding the link packet body data
	 * @param length the length of the link packet body data
	 * @throws BufException
	 */
	public abstract void receiveLinkPacketBody(long dataId, long channelId, ByteBuf byteBuf, int length) throws BufException;
	
	/**
	 * @return the {@link PacketBackupCollection} for sent packets of this channel
	 */
	public abstract PacketBackupCollection getSentPacketBackupCollection();
	/**
	 * @return the {@link IdCollection} of sent data ids on this channel
	 */
	public abstract IdCollection getSentDataIdCollection();
	/**
	 * @return the {@link IdCollection} of received data ids on this channel
	 */
	public abstract IdCollection getReceivedDataIdCollection();
	
	/**
	 * sets the next {@link TransparentByteBuf} for inbound packet bodies
	 * @param inTransparentByteBufGenerator the {@link TransparentByteBuf} for inbound packet bodies
	 */
	public abstract void setNewInBodyTransparentByteBuf(TransparentByteBuf newInBodyTransparentByteBuf);
	/**
	 * sets the next {@link TransparentByteBuf} for outbound packet bodies
	 * @param newOutBodyTransparentByteBuf the next {@link TransparentByteBuf} for outbound packet bodies
	 */
	public abstract void setNewOutBodyTransparentByteBuf(TransparentByteBuf newOutBodyTransparentByteBuf);
	/**
	 * applies the new {@link TransparentByteBuf} for inbound packet bodies
	 */
	public abstract void applyNewInBodyTransparentByteBuf();
	/**
	 * applies the new {@link TransparentByteBuf} for outbound packet bodies
	 */
	public abstract void applyNewOutBodyTransparentByteBuf();
	
}
