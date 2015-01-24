package org.dclayer.net.link.bmcp.component;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.link.bmcp.BMCPManagementChannel;
import org.dclayer.net.link.control.discontinuousblock.DiscontinuousBlock;

/**
 * abstract class for all basic management channel protocol command types
 */
public abstract class BMCPCommandComponent extends PacketComponent {
	
	/**
	 * BMCP command type of the change management channel protocol request command
	 */
	public static final byte CHANGE_MGMTCHANNEL_PROTOCOL_REQUEST = 0;
	/**
	 * BMCP command type of the change management channel protocol confirmation command
	 */
	public static final byte CHANGE_MGMTCHANNEL_PROTOCOL_CONFIRMATION = 1;
	/**
	 * BMCP command type of the connect crypto command
	 */
	public static final byte CONNECT_CRYPTO = 2;
	/**
	 * BMCP command type of the connect crypto echo request command
	 */
	public static final byte CONNECT_CRYPTO_ECHOREQUEST = 3;
	/**
	 * BMCP command type of the connect echo reply command
	 */
	public static final byte CONNECT_ECHOREPLY = 4;
	/**
	 * BMCP command type of the connect full encryption request command
	 */
	public static final byte CONNECT_FULL_ENCRYPTION_REQUEST = 5;
	/**
	 * BMCP command type of the connect confirmation command
	 */
	public static final byte CONNECT_CONFIRMATION = 6;
	/**
	 * BMCP command type of the channel block status request command
	 */
	public static final byte CHANNEL_BLOCK_STATUS_REQUEST = 7;
	/**
	 * BMCP command type of the channel block status report command
	 */
	public static final byte CHANNEL_BLOCK_STATUS_REPORT = 8;
	/**
	 * BMCP command type of the open channel request command
	 */
	public static final byte OPEN_CHANNEL_REQUEST = 9;
	/**
	 * BMCP command type of the open channel confirmation command
	 */
	public static final byte OPEN_CHANNEL_CONFIRMATION = 10;
	/**
	 * BMCP command type of the throttle command
	 */
	public static final byte THROTTLE = 11;
	/**
	 * BMCP command type of the acknowledgement command
	 */
	public static final byte ACK = 12;
	/**
	 * BMCP command type of the disconnect command
	 */
	public static final byte DISCONNECT = 13;
	/**
	 * BMCP command type of the kill link command
	 */
	public static final byte KILL_LINK = 14;
	
	/**
	 * @return the type of this BMCP command
	 */
	public abstract byte getType();
	/**
	 * calls the corresponding on receive method of the given {@link BMCPManagementChannel} instance
	 * @param discontinuousBlock the {@link DiscontinuousBlock} to pass through to the on receive method
	 * @param dataId the data id to pass through to the on receive method
	 * @param bmcpManagementChannel the {@link BMCPManagementChannel} instance to call the on receive method on
	 */
	public abstract void callOnReceiveMethod(DiscontinuousBlock discontinuousBlock, long dataId, BMCPManagementChannel bmcpManagementChannel);
	
}
