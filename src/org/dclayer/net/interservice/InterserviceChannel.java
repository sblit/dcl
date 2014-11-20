package org.dclayer.net.interservice;

import java.util.List;

import org.dclayer.DCLService;
import org.dclayer.crypto.challenge.CryptoChallenge;
import org.dclayer.crypto.challenge.Fixed128ByteCryptoChallenge;
import org.dclayer.crypto.key.Key;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.crypto.key.RSAKey;
import org.dclayer.datastructure.map.Int2Map;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.address.AsymmetricKeyPairAddress;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.interservice.message.ConnectionbaseNoticeInterserviceMessage;
import org.dclayer.net.interservice.message.CryptoChallengeReplyInterserviceMessage;
import org.dclayer.net.interservice.message.CryptoChallengeRequestInterserviceMessage;
import org.dclayer.net.interservice.message.GroupMemberLLAReplyInterserviceMessage;
import org.dclayer.net.interservice.message.GroupMemberLLARequestInterserviceMessage;
import org.dclayer.net.interservice.message.IntegrationConnectRequestInterserviceMessage;
import org.dclayer.net.interservice.message.IntegrationRequestInterserviceMessage;
import org.dclayer.net.interservice.message.LLAReplyInterserviceMessage;
import org.dclayer.net.interservice.message.LLARequestInterserviceMessage;
import org.dclayer.net.interservice.message.NetworkJoinNoticeInterserviceMessage;
import org.dclayer.net.interservice.message.NetworkLeaveNoticeInterserviceMessage;
import org.dclayer.net.interservice.message.NetworkPacketInterserviceMessage;
import org.dclayer.net.interservice.message.TrustedSwitchInterserviceMessage;
import org.dclayer.net.interservice.message.VersionInterserviceMessage;
import org.dclayer.net.link.channel.data.ThreadDataChannel;
import org.dclayer.net.llacache.CachedLLA;
import org.dclayer.net.llacache.LLA;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.slot.NetworkSlot;
import org.dclayer.net.network.slot.NetworkSlotMap;

public class InterserviceChannel extends ThreadDataChannel implements NetworkPacketProvider {
	
	public static long VERSION = 0;
	
	public static int ACTION_IDLE = 0;
	/**
	 * action code for applying for the trusted connection base
	 * (i.e. having sent a {@link TrustedSwitchInterserviceMessage} but not yet having received a {@link ConnectionbaseNoticeInterserviceMessage})
	 */
	public static int ACTION_TRUSTED_SWITCH = 0;
	
	public static byte CONNECTIONBASE_STRANGER = 0;
	public static byte CONNECTIONBASE_TRUSTED = 1;
	
	//
	
	private int action = ACTION_IDLE;
	
	private boolean initiator = false;
	private long version = -1;
	
	private boolean ready = false;
	
	/**
	 * connection base for incoming messages, i.e. how much we trust the remote
	 */
	private byte inConnectionBase = CONNECTIONBASE_STRANGER;
	
	/**
	 * connection base for outgoing messages, i.e. how much the remote trusts us
	 */
	private byte outConnectionBase = CONNECTIONBASE_STRANGER;
	
	/**
	 * connection base limit for outgoing messages, don't perform any actions caused by
	 * a connection base notice received from the remote reporting a higher connection base
	 */
	private byte maxAllowedOutConnectionBase = CONNECTIONBASE_STRANGER;
	
	private InterservicePacket inInterservicePacket = new InterservicePacket(this);
	private InterservicePacket outInterservicePacket = new InterservicePacket(this);
	
	private DCLService dclService;
	private InterserviceChannelActionListener interserviceChannelActionListener;
	private CachedLLA cachedLLA;
	private AsymmetricKeyPairAddress<RSAKey> asymmetricKeyPairAddress;
	
	private AsymmetricKeyPairAddress trustedRemoteAddress;
	
	private NetworkSlotMap remoteNetworkSlotMap = new NetworkSlotMap();
	private NetworkSlotMap localNetworkSlotMap = new NetworkSlotMap();
	
	private Int2Map remoteAPBRSlotMap = new Int2Map();
	
	/**
	 * {@link CryptoChallenge} object used to solve crypto challenges requested by the remote
	 */
	private CryptoChallenge inCryptoChallenge;
	
	/**
	 * {@link CryptoChallenge} object used to challenge the remote in order to confirm address possession
	 */
	private CryptoChallenge trustedSwitchOutCryptoChallenge;

	public InterserviceChannel(DCLService dclService, InterserviceChannelActionListener interserviceChannelActionListener, CachedLLA cachedLLA, AsymmetricKeyPairAddress<RSAKey> asymmetricKeyPairAddress, long channelId, String channelName) {
		super(cachedLLA.getLink(), channelId, channelName);
		this.dclService = dclService;
		this.interserviceChannelActionListener = interserviceChannelActionListener;
		this.cachedLLA = cachedLLA;
		this.asymmetricKeyPairAddress = asymmetricKeyPairAddress;
		
		this.inCryptoChallenge = new Fixed128ByteCryptoChallenge(asymmetricKeyPairAddress.getKeyPair().getPrivateKey());
	}
	
	public CachedLLA getCachedLLA() {
		return cachedLLA;
	}
	
	public boolean isInitiator() {
		return initiator;
	}
	
	@Override
	public synchronized void onOpenChannel(boolean initiator) {
		this.initiator = initiator;
		if(initiator) {
			this.version = VERSION;
			sendVersion(this.version);
		}
	}
	
	private void setMaxAllowedOutConnectionBase(byte maxAllowedOutConnectionBase) {
		Log.debug(this, "updating maximum allowed outgoing connection base from %d to %d", this.maxAllowedOutConnectionBase, maxAllowedOutConnectionBase);
		this.maxAllowedOutConnectionBase = maxAllowedOutConnectionBase;
	}
	
	public void startTrustedSwitch() {
		setAction(ACTION_TRUSTED_SWITCH);
		setMaxAllowedOutConnectionBase(CONNECTIONBASE_TRUSTED);
		sendTrustedSwitch(asymmetricKeyPairAddress.getKeyPair().getPublicKey());
	}
	
	private void setVersion(long version) {
		Log.msg(this, "setting version from %d to %d", this.version, version);
		this.version = version;
	}
	
	private void setReady() {
		Log.msg(this, "InterserviceChannel ready%s, version %d", this.ready ? " (was ready before)" : "", this.version);
		if(this.ready) return;
		this.ready = true;
		this.interserviceChannelActionListener.onReadyChange(this, ready);
	}
	
	public boolean isReady() {
		return ready;
	}
	
	private void setAction(int action) {
		Log.debug(this, "setting action from %d to %d", this.action, action);
		this.action = action;
	}
	
	private void setInConnectionBase(byte inConnectionBase) {
		Log.msg(this, "setting incoming connection base from %d to %d", this.inConnectionBase, inConnectionBase);
		byte oldInConnectionBase = this.inConnectionBase;
		this.inConnectionBase = inConnectionBase;
		if(oldInConnectionBase != inConnectionBase) {
			this.interserviceChannelActionListener.onInConnectionBaseChange(this, oldInConnectionBase, inConnectionBase);
		}
	}
	
	/**
	 * called when the remote notifies us of its incoming connection base
	 * @param connectionBase the incoming connection base of the remote
	 */
	private void setOutConnectionBase(byte outConnectionBase) {
		byte oldOutConnectionBase = this.outConnectionBase;
		Log.msg(this, "setting outgoing connection base from %d to %d", oldOutConnectionBase, outConnectionBase);
		this.outConnectionBase = outConnectionBase;
		
		if(outConnectionBase > this.maxAllowedOutConnectionBase) {
			Log.msg(this, "limiting new outgoing connection base %d down to maximum allowed outgoing connection base %d", outConnectionBase, this.maxAllowedOutConnectionBase);
			outConnectionBase = this.maxAllowedOutConnectionBase;
		}
		
		if(oldOutConnectionBase < CONNECTIONBASE_TRUSTED && outConnectionBase >= CONNECTIONBASE_TRUSTED) {
			sendInitialNetworkJoinNotices();
		}
	}
	
	private synchronized void sendInitialNetworkJoinNotices() {
		NetworkNode[] networkNodes = this.asymmetricKeyPairAddress.getNetworkInstanceCollection().copyArray();
		Log.msg(this, "sending initial network join notices (%d)", networkNodes.length);
		for(NetworkNode networkNode : networkNodes) {
			joinNetwork(networkNode);
		}
	}
	
	private void joinedNetwork(NetworkSlot networkSlot) {
		Log.msg(this, "joining network %s: slot %s", networkSlot.getNetworkNode().getNetworkType(), networkSlot);
		sendNetworkJoinNotice(networkSlot.getNetworkNode().getNetworkType(), networkSlot.getSlot());
	}
	
	public synchronized void joinNetwork(NetworkNode networkNode) {
		
		NetworkSlot networkSlot = localNetworkSlotMap.add(networkNode);
		
		NetworkSlot remoteNetworkSlot = remoteNetworkSlotMap.find(networkNode.getNetworkType());
		
		networkSlot.setRemoteEquivalent(remoteNetworkSlot);
		if(remoteNetworkSlot != null) remoteNetworkSlot.setRemoteEquivalent(networkSlot);
		
		joinedNetwork(networkSlot);
		
	}
	
	private void setTrustedRemoteAddress(Key publicKey) {
		// TODO type safety
		AsymmetricKeyPairAddress address = new AsymmetricKeyPairAddress<>(KeyPair.fromPublicKey(publicKey));
		Log.msg(this, "setting trusted remote address: %s", address);
		this.trustedRemoteAddress = address;
	}
	
	public AsymmetricKeyPairAddress getTrustedRemoteAddress() {
		return trustedRemoteAddress;
	}
	
	public AsymmetricKeyPairAddress<RSAKey> getLocalAddress() {
		return asymmetricKeyPairAddress;
	}

	@Override
	public void readConstantly(ByteBuf byteBuf) {
		
		for(;;) {
			
			try {
				
				inInterservicePacket.read(byteBuf);
				
			} catch(BufException e) {
				Log.exception(this, e);
			} catch (ParseException e) {
				Log.exception(this, e);
			}
			
			processPacket();
			
		}
		
	}
	
	private void processPacket() {
		
		InterserviceMessage interserviceMessage = inInterservicePacket.getInterserviceMessage();
		interserviceMessage.callOnReceiveMethod(this);
		
	}
	
	private void sendOutInterservicePacket() {
		try {
			outInterservicePacket.write(getWriteByteBuf());
			flush();
		} catch (BufException e) {
			Log.exception(this, e);
		}
	}
	
	// send methods
	
	private synchronized void sendVersion(long version) {
		Log.debug(this, "sending version: %d", version);
		outInterservicePacket.setVersionInterserviceMessage().setVersion(version);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendLLAReply(List<LLA> llas) {
		Log.debug(this, "sending LLA reply with %d LLAs", llas.size());
		outInterservicePacket
		.setLLAReplyInterserviceMessage()
		.getLowerLevelAddressListComponent()
		.setAddresses(llas);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendTrustedSwitch(RSAKey rsaKey) {
		Log.debug(this, "sending trusted switch message");
		outInterservicePacket
		.setTrustedSwitchInterserviceMessage()
		.getKeyComponent()
		.setRSAKeyComponent()
		.setKey(rsaKey);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendCryptoChallengeRequest(Data plainData) {
		Log.debug(this, "sending crypto challenge request");
		outInterservicePacket
		.setCryptoChallengeRequestInterserviceMessage()
		.getDataComponent()
		.setData(plainData);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendCryptoChallengeReply(Data cipherData) {
		Log.debug(this, "sending crypto challenge reply");
		outInterservicePacket
		.setCryptoChallengeReplyInterserviceMessage()
		.getDataComponent()
		.setData(cipherData);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendConnectionbaseNotice() {
		Log.debug(this, "sending connection base notice: %d", inConnectionBase);
		outInterservicePacket
		.setConnectionbaseNoticeInterserviceMessage()
		.setConnectionBase(inConnectionBase);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendNetworkJoinNotice(NetworkType networkType, int slot) {
		Log.debug(this, "sending network join notice for network type %s, slot %d", networkType, slot);
		NetworkJoinNoticeInterserviceMessage networkJoinNoticeInterserviceMessage = outInterservicePacket.setNetworkJoinNoticeInterserviceMessage();
		networkJoinNoticeInterserviceMessage.setNetworkType(networkType);
		networkJoinNoticeInterserviceMessage.setSlot(slot);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendNetworkLeaveNotice(int slot) {
		Log.debug(this, "sending network leave notice for slot %d", slot);
		outInterservicePacket
		.setNetworkLeaveNoticeInterserviceMessage()
		.setSlot(slot);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendNetworkPacket(NetworkPacket networkPacket, int slot) {
		Log.debug(this, "sending network packet on slot %d: %s", slot, networkPacket);
		NetworkPacketInterserviceMessage networkPacketInterserviceMessage = outInterservicePacket.setNetworkPacketInterserviceMessage();
		networkPacketInterserviceMessage.setSlot(slot);
		networkPacketInterserviceMessage.setNetworkPacket(networkPacket);
		sendOutInterservicePacket();
	}
	
	private void onRemoteNetworkJoin(final NetworkSlot remoteNetworkSlot) {
		
		NetworkNode remoteNetworkNode = remoteNetworkSlot.getNetworkNode();

		int slot = remoteNetworkSlot.getSlot();
		Data addressData = remoteNetworkNode.getScaledAddress();
		
		Log.msg(this, "remote joined network %s, slot %d", remoteNetworkNode, slot);
		
		NetworkSlot localNetworkSlot = localNetworkSlotMap.find(remoteNetworkNode.getNetworkType());
		
		NetworkInstance localNetworkInstance = interserviceChannelActionListener.onRemoteNetworkJoin(this, remoteNetworkNode);
		if(localNetworkSlot == null && localNetworkInstance != null) {
			// let's join that network
			synchronized(this) {
				if(outConnectionBase >= CONNECTIONBASE_TRUSTED) {
					localNetworkSlot = localNetworkSlotMap.add(localNetworkInstance);
					joinedNetwork(localNetworkSlot);
				} else {
					startTrustedSwitch();
				}
			}
		}
		
		remoteNetworkSlot.setRemoteEquivalent(localNetworkSlot);
		if(localNetworkSlot != null) localNetworkSlot.setRemoteEquivalent(remoteNetworkSlot);
		
	}
	
	private void onRemoteNetworkLeave(NetworkSlot remoteNetworkSlot) {
		
		NetworkNode remoteNetworkNode = remoteNetworkSlot.getNetworkNode();
		Data addressData = remoteNetworkNode.getScaledAddress();
		int slot = remoteNetworkSlot.getSlot();
		
		Log.msg(this, "remote left network %s (slot %d, address %s)", remoteNetworkNode, slot, addressData);
		
		NetworkSlot localNetworkSlot = remoteNetworkSlot.getRemoteEquivalent();
		if(localNetworkSlot != null) {
			remoteNetworkSlot.setRemoteEquivalent(null);
			localNetworkSlot.setRemoteEquivalent(null);
		}
		
		interserviceChannelActionListener.onRemoteNetworkLeave(this, remoteNetworkNode);
		
	}
	
	private void onNetworkJoinNotice(NetworkType networkType, final int slot) {
		
		NetworkSlot remoteNetworkSlot = remoteNetworkSlotMap.get(slot);
		if(remoteNetworkSlot != null) {
			onRemoteNetworkLeave(remoteNetworkSlot);
		}
		
		NetworkNode remoteNetworkNode = new NetworkNode(networkType, trustedRemoteAddress) {
			@Override
			public boolean onForward(NetworkPacket networkPacket) {
				sendNetworkPacket(networkPacket, slot);
				return true;
			}
		};
		
		remoteNetworkSlot = remoteNetworkSlotMap.put(slot, remoteNetworkNode);
		
		onRemoteNetworkJoin(remoteNetworkSlot);
		
	}
	
	private void onNetworkLeaveNotice(int slot) {
		NetworkSlot remoteNetworkSlot = remoteNetworkSlotMap.remove(slot);
		
		if(remoteNetworkSlot == null) {
			
			Log.warning(this, "remote left empty network type slot %d", slot);
			
		} else {
			
			onRemoteNetworkLeave(remoteNetworkSlot);
			
		}
	}
	
	private void onReceiveNetworkPacket(NetworkPacket networkPacket) {
		
		NetworkSlot localNetworkSlot = networkPacket.getNetworkSlot();
		if(localNetworkSlot.getRemoteEquivalent() == null) {
			// remote peer sends to our network slot even though itself didn't join that network
			Log.warning(this, "ignoring network packet on network slot %s, remote did not join that network", localNetworkSlot);
			return;
		}
		
		if(!localNetworkSlot.getNetworkNode().forward(networkPacket)) {
			// TODO: could not route, maybe notify remote?
		}
		
	}
	
	// onReceive methods
	
	public synchronized void onReceiveVersionInterserviceMessage(VersionInterserviceMessage versionInterserviceMessage) {
		
		// TODO probably don't let the remote change version during normal operation
		long version = versionInterserviceMessage.getVersion();
		Log.debug(this, "received version: %d", version);
		
		if(version == this.version) {
			
			Log.debug(this, "versions match, not replying");
			setReady();
			
		} else {
			
			if(isReady()) {
				
				Log.debug(this, "ignoring version message");
				
			} else {
				
				setVersion(Math.min(VERSION, version));
				sendVersion(this.version);
				if(this.version == version) {
					// agreeing with remote's proposal
					setReady();
				}
				
			}
			
		}
	}
	
	public void onReceiveLLARequestInterserviceMessage(LLARequestInterserviceMessage llaRequestInterserviceMessage) {
		// TODO don't let the remote request infinite LLAs infinite times
		int limit = (int) Math.min(Integer.MAX_VALUE, llaRequestInterserviceMessage.getLimit());
		Log.debug(this, "received LLA request for at most %d LLAs", limit);
		List<LLA> llas = this.dclService.getLLACache().getRandomLLAs(limit);
		sendLLAReply(llas);
	}
	
	public void onReceiveLLAReplyInterserviceMessage(LLAReplyInterserviceMessage llaReplyInterserviceMessage) {
		// TODO don't let the remote flood you with this
		List<LLA> llas = llaReplyInterserviceMessage.getLowerLevelAddressListComponent().getNewAddresses();
		Log.debug(this, "received %d LLAs", llas.size());
		dclService.storeLLAs(llas);
	}
	
	public synchronized void onReceiveTrustedSwitchInterserviceMessage(TrustedSwitchInterserviceMessage trustedSwitchInterserviceMessage) {
		
		Log.debug(this, "received trusted switch");
		
		if(trustedSwitchOutCryptoChallenge != null) {
			Log.msg(this, "ignoring trusted switch, already challenging the remote");
			// TODO notify remote of failure
			return;
		}
		
		Key key;
		try {
			key = trustedSwitchInterserviceMessage.getKeyComponent().getKeyComponent().getKey();
		} catch (InsufficientKeySizeException e) {
			Log.exception(this, e);
			// TODO notify remote of failure
			return;
		}
		
		this.trustedSwitchOutCryptoChallenge = new Fixed128ByteCryptoChallenge(key);
		
		Data plainData = trustedSwitchOutCryptoChallenge.makeRandomPlainData();
		
		sendCryptoChallengeRequest(plainData);
		
	}
	
	public synchronized void onReceiveCryptoChallengeRequestInterserviceMessage(CryptoChallengeRequestInterserviceMessage cryptoChallengeRequestInterserviceMessage) {
		
		Log.debug(this, "received crypto challenge request");
		
		if(action != ACTION_TRUSTED_SWITCH) {
			Log.msg(this, "ignoring crypto challenge request, not performing trusted switch");
			// didn't request this
			return;
		}
		
		Data plainData = cryptoChallengeRequestInterserviceMessage.getDataComponent().getData();
		
		Data cipherData;
		try {
			cipherData = inCryptoChallenge.solveCryptoChallenge(plainData);
		} catch (CryptoException e) {
			Log.exception(this, e);
			setAction(ACTION_IDLE);
			// TODO notify remote of failure
			return;
		}
		
		sendCryptoChallengeReply(cipherData);
		
	}
	
	public synchronized void onReceiveCryptoChallengeReplyInterserviceMessage(CryptoChallengeReplyInterserviceMessage cryptoChallengeReplyInterserviceMessage) {
		
		Log.debug(this, "received crypto challenge reply");
		
		if(trustedSwitchOutCryptoChallenge == null) {
			Log.msg(this, "ignoring crypto challenge reply, didn't challenge the remote");
			// didn't see a trusted switch message before
			return;
		}
		
		Data cipherData = cryptoChallengeReplyInterserviceMessage.getDataComponent().getData();
		
		boolean success;
		try {
			success = trustedSwitchOutCryptoChallenge.verifyCipherData(cipherData);
		} catch (CryptoException e) {
			Log.exception(this, e);
			// TODO notify remote of failure
			return;
		}
		
		if(success) {
			setTrustedRemoteAddress(this.trustedSwitchOutCryptoChallenge.getKey());
			setInConnectionBase(CONNECTIONBASE_TRUSTED);
		}
		
		this.trustedSwitchOutCryptoChallenge = null;
		
		sendConnectionbaseNotice();
		
	}
	
	public void onReceiveConnectionbaseNoticeInterserviceMessage(ConnectionbaseNoticeInterserviceMessage connectionbaseNoticeInterserviceMessage) {
		
		byte outConnectionBase = connectionbaseNoticeInterserviceMessage.getConnectionBase();
		Log.debug(this, "received connection base notice from remote, connection base: %d", outConnectionBase);
		setOutConnectionBase(outConnectionBase);
		
	}
	
	public void onReceiveNetworkJoinNoticeInterserviceMessage(NetworkJoinNoticeInterserviceMessage networkJoinNoticeInterserviceMessage) {
		
		NetworkType networkType = networkJoinNoticeInterserviceMessage.getNetworkType();
		int slot = networkJoinNoticeInterserviceMessage.getSlot();
		Log.debug(this, "received network join notice for network: %s", networkType);
		
		if(inConnectionBase < CONNECTIONBASE_TRUSTED) {
			Log.msg(this, "ignoring network join notice, incoming connection base insufficient");
			return;
		}
		
		onNetworkJoinNotice(networkType, slot);
		
	}
	
	public void onReceiveNetworkLeaveNoticeInterserviceMessage(NetworkLeaveNoticeInterserviceMessage networkLeaveNoticeInterserviceMessage) {
		
		int slot = networkLeaveNoticeInterserviceMessage.getSlot();
		Log.debug(this, "received network leave notice for slot: %d", slot);
		
		if(inConnectionBase < CONNECTIONBASE_TRUSTED) {
			Log.msg(this, "ignoring network leave notice, incoming connection base insufficient");
			return;
		}
		
		onNetworkLeaveNotice(slot);
		
	}
	
	public void onReceiveIntegrationRequestInterserviceMessage(IntegrationRequestInterserviceMessage integrationRequestInterserviceMessage) {
		
	}
	
	public void onReceiveIntegrationConnectRequestInterserviceMessage(IntegrationConnectRequestInterserviceMessage integrationConnectRequestInterserviceMessage) {
		
	}
	
	public void onReceiveGroupMemberLLARequestInterserviceMessage(GroupMemberLLARequestInterserviceMessage groupMemberLLARequestInterserviceMessage) {
		
	}
	
	public void onReceiveGroupMemberLLAReplyInterserviceMessage(GroupMemberLLAReplyInterserviceMessage groupMemberLLAReplyInterserviceMessage) {
		
	}
	
	public void onReceiveNetworkPacketInterserviceMessage(NetworkPacketInterserviceMessage networkPacketInterserviceMessage) {
		
		if(inConnectionBase < CONNECTIONBASE_TRUSTED) {
			Log.msg(this, "ignoring network packet, incoming connection base insufficient");
			return;
		}
		
		NetworkPacket networkPacket = networkPacketInterserviceMessage.getNetworkPacket();
		Log.debug(this, "received network packet on slot %d: %s", networkPacketInterserviceMessage.getSlot(), networkPacket);
		onReceiveNetworkPacket(networkPacket);
	}

	@Override
	public NetworkPacket getNetworkPacket(int slot) {
		NetworkSlot networkSlot = localNetworkSlotMap.get(slot);
		return networkSlot == null ? null : networkSlot.getNetworkPacket();
	}
	
}
