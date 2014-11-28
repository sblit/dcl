package org.dclayer.net.interservice;

import java.util.List;

import org.dclayer.DCLService;
import org.dclayer.crypto.challenge.CryptoChallenge;
import org.dclayer.crypto.challenge.Fixed128ByteCryptoChallenge;
import org.dclayer.crypto.key.Key;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.crypto.key.RSAKey;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.address.Address;
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
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.RemoteNetworkNode;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.slot.AddressSlot;
import org.dclayer.net.network.slot.AddressSlotMap;
import org.dclayer.net.network.slot.NetworkSlot;
import org.dclayer.net.network.slot.NetworkSlotMap;

public class InterserviceChannel extends ThreadDataChannel implements NetworkPacketProvider {
	
	public static long VERSION = 0;
	
	public static byte CONNECTIONBASE_STRANGER = 0;
	public static byte CONNECTIONBASE_TRUSTED = 1;
	
	//
	
	private boolean initiator = false;
	private long version = -1;
	
	private boolean ready = false;
	
	private InterservicePacket inInterservicePacket = new InterservicePacket(this);
	private InterservicePacket outInterservicePacket = new InterservicePacket(this);
	
	private DCLService dclService;
	private InterserviceChannelActionListener interserviceChannelActionListener;
	private CachedLLA cachedLLA;
	
	private AddressSlotMap remoteAddressSlotMap = new AddressSlotMap(this, true);
	private AddressSlotMap localAddressSlotMap = new AddressSlotMap(this, false);
	
	private NetworkSlotMap remoteNetworkSlotMap = new NetworkSlotMap();
	private NetworkSlotMap localNetworkSlotMap = new NetworkSlotMap();

	public InterserviceChannel(DCLService dclService, InterserviceChannelActionListener interserviceChannelActionListener, CachedLLA cachedLLA, long channelId, String channelName) {
		super(cachedLLA.getLink(), channelId, channelName);
		this.dclService = dclService;
		this.interserviceChannelActionListener = interserviceChannelActionListener;
		this.cachedLLA = cachedLLA;
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
	
	private void removeRemoteAddressSlot(AddressSlot remoteAddressSlot) {
		
		for(NetworkSlot remoteNetworkSlot : remoteAddressSlot.getNetworkSlots()) {
			
			NetworkNode remoteNetworkNode = remoteNetworkSlot.removeNetworkNode(remoteAddressSlot.getAsymmetricKeyPairAddress());
			interserviceChannelActionListener.onRemoveRemoteNetworkNode(this, remoteNetworkNode);
			
			checkRemoteNetworkSlot(remoteNetworkSlot);
			
		}
		
		remoteAddressSlotMap.remove(remoteAddressSlot.getSlot());
		
	}
	
	private void removeRemoteNetworkSlot(NetworkSlot remoteNetworkSlot) {
		
		for(NetworkNode remoteNetworkNode : remoteNetworkSlot.getNetworkNodes()) {
			
			AddressSlot remoteAddressSlot = remoteAddressSlotMap.find(remoteNetworkNode.getAddress());
			remoteAddressSlot.removeNetworkSlot(remoteNetworkSlot);
			
			interserviceChannelActionListener.onRemoveRemoteNetworkNode(this, remoteNetworkNode);
			
		}
		
		NetworkSlot localNetworkSlot = remoteNetworkSlot.getRemoteEquivalent();
		if(localNetworkSlot != null) {
			remoteNetworkSlot.setRemoteEquivalent(null);
			localNetworkSlot.setRemoteEquivalent(null);
		}
		
		remoteNetworkSlotMap.remove(remoteNetworkSlot.getSlot());
		
	}
	
	private void checkRemoteNetworkSlot(NetworkSlot remoteNetworkSlot) {
		
		if(remoteNetworkSlot.getNetworkNodes().size() <= 0) {
			
			Log.msg(this, "remote network slot %s is empty, removing", remoteNetworkSlot);
			
			NetworkSlot localNetworkSlot = remoteNetworkSlot.getRemoteEquivalent();
			if(localNetworkSlot != null) {
				remoteNetworkSlot.setRemoteEquivalent(null);
				localNetworkSlot.setRemoteEquivalent(null);
			}
			
			remoteNetworkSlotMap.remove(remoteNetworkSlot.getSlot());
			
		}
		
	}
	
	public void startTrustedSwitchOnAllAddressSlots() {
		Log.debug(this, "starting trusted switch on all (%d) address slots", localAddressSlotMap.size());
		for(AddressSlot addressSlot : localAddressSlotMap) {
			startTrustedSwitch(addressSlot);
		}
	}
	
	public void startTrustedSwitch(AddressSlot addressSlot) {
		Log.debug(this, "starting trusted switch on address slot: %s", addressSlot);
		addressSlot.setMaxAllowedOutConnectionBase(CONNECTIONBASE_TRUSTED);
		KeyPair keyPair = addressSlot.getAsymmetricKeyPairAddress().getKeyPair();
		addressSlot.setInCryptoChallenge(new Fixed128ByteCryptoChallenge(keyPair.getPrivateKey()));
		sendTrustedSwitch(addressSlot, (RSAKey) keyPair.getPublicKey());
	}
	
	private void cancelTrustedSwitch(AddressSlot addressSlot) {
		Log.debug(this, "cancelling trusted switch on address slot: %s", addressSlot);
		addressSlot.setMaxAllowedOutConnectionBase(CONNECTIONBASE_STRANGER);
		addressSlot.setInCryptoChallenge(null);
	}
	
	private void finishTrustedSwitch(AddressSlot addressSlot, boolean success) {
		if(success) {
			Log.msg(this, "remote successfully completed crypto challenge for address slot: %s", addressSlot);
			addressSlot.setTrustedSwitchOutCryptoChallenge(null);
			addressSlot.setConnectionBase(CONNECTIONBASE_TRUSTED);
		} else {
			Log.msg(this, "remote failed crypto challenge for address slot, removing: %s", addressSlot);
			removeRemoteAddressSlot(addressSlot);
		}
	}
	
	private void setVersion(long version) {
		Log.msg(this, "setting version from %d to %d", this.version, version);
		this.version = version;
	}
	
	private synchronized void setReady() {
		Log.msg(this, "InterserviceChannel ready%s, version %d", this.ready ? " (was ready before)" : "", this.version);
		if(this.ready) return;
		this.ready = true;
		this.interserviceChannelActionListener.onReadyChange(this, ready);
		for(AddressSlot addressSlot : localAddressSlotMap) {
			startTrustedSwitch(addressSlot);
		}
	}
	
	public boolean isReady() {
		return ready;
	}
	
	/**
	 * called when the remote notifies us of its incoming connection base
	 * @param connectionBase the incoming connection base of the remote
	 */
	private void setOutConnectionBase(AddressSlot addressSlot, byte outConnectionBase) {
		byte oldOutConnectionBase = addressSlot.getConnectionBase();
		addressSlot.setOutConnectionBase(outConnectionBase);
		
		if(oldOutConnectionBase < CONNECTIONBASE_TRUSTED && addressSlot.getConnectionBase() >= CONNECTIONBASE_TRUSTED) {
			joinNetworks(addressSlot);
		}
	}
	
	private synchronized void joinNetworks(AddressSlot addressSlot) {
		List<NetworkNode> networkNodes = addressSlot.popNetworkNodesToJoin();
		Log.msg(this, "initially joining %d networks for address slot: %s", networkNodes.size(), addressSlot);
		for(NetworkNode networkNode : networkNodes) {
			joinNetwork(addressSlot, networkNode);
		}
	}
	
	public synchronized void joinNetwork(NetworkNode networkNode) {
		
		AddressSlot addressSlot = localAddressSlotMap.find(networkNode.getAddress());
		
		if(addressSlot == null) {
			addressSlot = localAddressSlotMap.add(networkNode.getAddress());
			if(isReady()) {
				startTrustedSwitch(addressSlot);
			}
		}
		
		// if this address slot is not ready for joining networks yet, remember
		// this node for joining it later
		if(addressSlot.getConnectionBase() < CONNECTIONBASE_TRUSTED) {
			addressSlot.addNetworkNodeToJoin(networkNode);
			return;
		}
		
		// else, just join it
		joinNetwork(addressSlot, networkNode);
		
	}
	
	private synchronized void joinNetwork(AddressSlot addressSlot, NetworkNode networkNode) {
		
		if(addressSlot.getConnectionBase() < CONNECTIONBASE_TRUSTED) {
			
			Log.debug(this, "saving network node %s for later joining with address slot: %s", networkNode, addressSlot);
			addressSlot.addNetworkNodeToJoin(networkNode);
			return;
			
		}
		
		NetworkSlot networkSlot = localNetworkSlotMap.find(networkNode.getNetworkType());
		
		boolean newSlot = (networkSlot == null);
		
		if(newSlot) {
			
			networkSlot = localNetworkSlotMap.add(networkNode.getNetworkType());
			
			NetworkSlot remoteNetworkSlot = remoteNetworkSlotMap.find(networkNode.getNetworkType());
			networkSlot.setRemoteEquivalent(remoteNetworkSlot);
			if(remoteNetworkSlot != null) remoteNetworkSlot.setRemoteEquivalent(networkSlot);
			
		}
		
		networkSlot.addNetworkNode(networkNode);
		
		Log.debug(this, "joining %s network slot %s: %s", newSlot ? "new" : "existing", networkSlot, addressSlot);
		
		sendNetworkJoinNotice(addressSlot.getSlot(), newSlot ? networkSlot.getNetworkType() : null, networkSlot.getSlot());
		
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
	
	private synchronized void sendTrustedSwitch(AddressSlot addressSlot, RSAKey rsaKey) {
		Log.debug(this, "sending trusted switch message for address slot: %s", addressSlot);
		TrustedSwitchInterserviceMessage trustedSwitchInterserviceMessage = outInterservicePacket.setTrustedSwitchInterserviceMessage();
		trustedSwitchInterserviceMessage.getKeyComponent().setRSAKeyComponent().setKey(rsaKey);
		trustedSwitchInterserviceMessage.setAddressSlot(addressSlot.getSlot());
		sendOutInterservicePacket();
	}
	
	private synchronized void sendCryptoChallengeRequest(AddressSlot addressSlot, Data plainData) {
		Log.debug(this, "sending crypto challenge request");
		CryptoChallengeRequestInterserviceMessage cryptoChallengeRequestInterserviceMessage = outInterservicePacket.setCryptoChallengeRequestInterserviceMessage();
		cryptoChallengeRequestInterserviceMessage.setAddressSlot(addressSlot.getSlot());
		cryptoChallengeRequestInterserviceMessage.getDataComponent().setData(plainData);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendCryptoChallengeReply(AddressSlot addressSlot, Data cipherData) {
		Log.debug(this, "sending crypto challenge reply");
		CryptoChallengeReplyInterserviceMessage cryptoChallengeReplyInterserviceMessage = outInterservicePacket.setCryptoChallengeReplyInterserviceMessage();
		cryptoChallengeReplyInterserviceMessage.setAddressSlot(addressSlot.getSlot());
		cryptoChallengeReplyInterserviceMessage.getDataComponent().setData(cipherData);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendConnectionbaseNotice(AddressSlot addressSlot) {
		Log.debug(this, "sending connection base notice for connection base %d on address slot: %s", addressSlot.getConnectionBase(), addressSlot);
		ConnectionbaseNoticeInterserviceMessage connectionbaseNoticeInterserviceMessage = outInterservicePacket.setConnectionbaseNoticeInterserviceMessage();
		connectionbaseNoticeInterserviceMessage.setAddressSlot(addressSlot.getSlot());
		connectionbaseNoticeInterserviceMessage.setConnectionBase(addressSlot.getConnectionBase());
		sendOutInterservicePacket();
	}
	
	private synchronized void sendNetworkJoinNotice(int addressSlotId, NetworkType networkType, int networkSlotId) {
		Log.debug(this, "sending network join notice for address slot id %d, network slot id %d and network type %s", addressSlotId, networkSlotId, networkType);
		NetworkJoinNoticeInterserviceMessage networkJoinNoticeInterserviceMessage = outInterservicePacket.setNetworkJoinNoticeInterserviceMessage();
		networkJoinNoticeInterserviceMessage.setAddressSlot(addressSlotId);
		networkJoinNoticeInterserviceMessage.setNetworkSlot(networkSlotId);
		networkJoinNoticeInterserviceMessage.getNetworkTypeComponent().setNetworkType(networkType);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendNetworkLeaveNotice(int slot) {
		Log.debug(this, "sending network leave notice for slot %d", slot);
		outInterservicePacket
		.setNetworkLeaveNoticeInterserviceMessage()
		.setNetworkSlot(slot);
		sendOutInterservicePacket();
	}
	
	private synchronized void sendNetworkPacket(NetworkPacket networkPacket, int slot) {
		Log.debug(this, "sending network packet on slot %d: %s", slot, networkPacket);
		NetworkPacketInterserviceMessage networkPacketInterserviceMessage = outInterservicePacket.setNetworkPacketInterserviceMessage();
		networkPacketInterserviceMessage.setSlot(slot);
		networkPacketInterserviceMessage.setNetworkPacket(networkPacket);
		sendOutInterservicePacket();
	}
	
	private void onNetworkJoinNotice(AddressSlot remoteAddressSlot, final int networkSlotId, NetworkType networkType) {
		
		if(remoteAddressSlot.getConnectionBase() < CONNECTIONBASE_TRUSTED) {
			Log.msg(this, "ignoring network join notice on address slot (incoming connection base insufficient): %s", remoteAddressSlot);
			return;
		}
		
		NetworkSlot remoteNetworkSlot = remoteNetworkSlotMap.get(networkSlotId);
		
		boolean existing = remoteNetworkSlot != null;
		
		if(existing) {
			
			if(networkType == null) {
				
				networkType = remoteNetworkSlot.getNetworkType();
				
			} else if(!remoteNetworkSlot.getNetworkType().equals(networkType)) {
				
				Log.warning(this, "received network join notice for address slot %s joining network slot %s where network slot's network type (%s) does not equal (surprisingly) given network type of network join notice (%s), emptying existing network slot first", remoteAddressSlot, remoteNetworkSlot, remoteNetworkSlot.getNetworkType(), networkType);
				removeRemoteNetworkSlot(remoteNetworkSlot);
				remoteNetworkSlot = remoteNetworkSlotMap.put(networkSlotId, networkType);
				
			}
			
		} else {
			
			if(networkType == null) {
				Log.msg(this, "ignoring network join notice for empty network slot id %d, network type is not given", networkSlotId);
				return;
			}
			
			remoteNetworkSlot = remoteNetworkSlotMap.put(networkSlotId, networkType);
			
		}
		
		NetworkNode remoteNetworkNode = new RemoteNetworkNode(networkType, remoteAddressSlot.getAsymmetricKeyPairAddress(), remoteNetworkSlot) {
			@Override
			public boolean onForward(NetworkPacket networkPacket) {
				sendNetworkPacket(networkPacket, networkSlotId);
				return true;
			}
		};
		
		remoteNetworkSlot.addNetworkNode(remoteNetworkNode);
		
		remoteAddressSlot.addNetworkSlot(remoteNetworkSlot);
		
		Log.msg(this, "remote joined network %s with address slot %s, network node: %s", remoteNetworkSlot, remoteAddressSlot, remoteNetworkNode);
		
		NetworkSlot localNetworkSlot = localNetworkSlotMap.find(remoteNetworkSlot.getNetworkType());
		
		interserviceChannelActionListener.onNewRemoteNetworkNode(this, remoteNetworkNode, localNetworkSlot);
		
		remoteNetworkSlot.setRemoteEquivalent(localNetworkSlot);
		if(localNetworkSlot != null) localNetworkSlot.setRemoteEquivalent(remoteNetworkSlot);
		
	}
	
	private void onNetworkLeaveNotice(AddressSlot remoteAddressSlot, int remoteNetworkSlotId) {
		
		NetworkSlot remoteNetworkSlot = remoteNetworkSlotMap.get(remoteNetworkSlotId);
		
		if(remoteNetworkSlot == null) {
			Log.warning(this, "remote left nonexistent network type slot %d with address slot: %s", remoteNetworkSlotId, remoteAddressSlot);
			return;
		}
		
		NetworkNode remoteNetworkNode = remoteNetworkSlot.removeNetworkNode(remoteAddressSlot.getAsymmetricKeyPairAddress());
		
		if(remoteNetworkNode == null) {
			Log.warning(this, "remote left network slot %s which it did not join with address slot: %s", remoteNetworkSlot, remoteAddressSlot);
			return;
		}
		
		remoteAddressSlot.removeNetworkSlot(remoteNetworkSlot);
		
		Log.msg(this, "remote left network %s, network slot %s with address slot: %s", remoteNetworkNode, remoteNetworkSlot, remoteAddressSlot);
		
		checkRemoteNetworkSlot(remoteNetworkSlot);
		
		interserviceChannelActionListener.onRemoveRemoteNetworkNode(this, remoteNetworkNode);
		
	}
	
	private void onReceiveNetworkPacket(NetworkPacket networkPacket) {
		
		NetworkSlot localNetworkSlot = networkPacket.getNetworkSlot();
		if(localNetworkSlot.getRemoteEquivalent() == null) {
			// remote peer sends to our network slot even though itself didn't join that network
			Log.warning(this, "ignoring network packet on network slot %s, remote did not join that network", localNetworkSlot);
			return;
		}
		
		// just forward this to the first node as their routing tables are connected anyways
		if(!localNetworkSlot.getNetworkNodes().get(0).forward(networkPacket)) {
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
		
		int addressSlotId = trustedSwitchInterserviceMessage.getAddressSlot();
		
		Key key;
		try {
			key = trustedSwitchInterserviceMessage.getKeyComponent().getKeyComponent().getKey();
		} catch (InsufficientKeySizeException e) {
			Log.exception(this, e, "could not parse trusted switch message for address slot %d, insufficient key size", addressSlotId);
			// TODO notify remote of failure
			return;
		}
		
		Address remoteAddress = new Address<>(KeyPair.fromPublicKey(key));
		
		AddressSlot addressSlot = remoteAddressSlotMap.get(addressSlotId);
		
		if(addressSlot != null) {
			if(addressSlot.getAsymmetricKeyPairAddress().equals(remoteAddress)) {
				Log.msg(this, "ignoring trusted switch message for address slot (slot already exists with same address): %s", addressSlot);
				return;
			} else {
				Log.msg(this, "received trusted switch message for existing slot with different key, overwriting slot: %s", addressSlot);
				removeRemoteAddressSlot(addressSlot);
			}
		}
		
		Log.debug(this, "received trusted switch message for address slot id %d with address: %s", addressSlotId, remoteAddress);
		
		addressSlot = remoteAddressSlotMap.put(addressSlotId, remoteAddress);
		
		CryptoChallenge trustedSwitchOutCryptoChallenge = new Fixed128ByteCryptoChallenge(key);
		addressSlot.setTrustedSwitchOutCryptoChallenge(trustedSwitchOutCryptoChallenge);
		
		Data plainData = trustedSwitchOutCryptoChallenge.makeRandomPlainData();
		sendCryptoChallengeRequest(addressSlot, plainData);
		
	}
	
	public synchronized void onReceiveCryptoChallengeRequestInterserviceMessage(CryptoChallengeRequestInterserviceMessage cryptoChallengeRequestInterserviceMessage) {
		
		int addressSlotId = cryptoChallengeRequestInterserviceMessage.getAddressSlot();
		AddressSlot addressSlot = localAddressSlotMap.get(addressSlotId);
		
		if(addressSlot == null) {
			Log.msg(this, "received crypto challenge request for empty address slot %d, ignoring", addressSlotId);
			return;
		}
		
		CryptoChallenge inCryptoChallenge = addressSlot.getInCryptoChallenge();
		
		if(inCryptoChallenge == null) {
			Log.msg(this, "ignoring crypto challenge request for address slot (not performing trusted switch): %s", addressSlot);
			return;
		}
		
		addressSlot.setInCryptoChallenge(null);
		
		Log.debug(this, "received crypto challenge request for address slot: %s", addressSlot);
		
		Data plainData = cryptoChallengeRequestInterserviceMessage.getDataComponent().getData();
		
		Data cipherData;
		try {
			cipherData = inCryptoChallenge.solveCryptoChallenge(plainData);
		} catch (CryptoException e) {
			Log.exception(this, e, "could not solve crypto challenge for address slot: %s", addressSlot);
			cancelTrustedSwitch(addressSlot);
			// TODO notify remote of failure
			return;
		}
		
		sendCryptoChallengeReply(addressSlot, cipherData);
		
	}
	
	public synchronized void onReceiveCryptoChallengeReplyInterserviceMessage(CryptoChallengeReplyInterserviceMessage cryptoChallengeReplyInterserviceMessage) {
		
		int addressSlotId = cryptoChallengeReplyInterserviceMessage.getAddressSlot();
		AddressSlot addressSlot = remoteAddressSlotMap.get(addressSlotId);
		
		if(addressSlot == null) {
			Log.msg(this, "received crypto challenge reply for empty address slot %d, ignoring", addressSlotId);
			return;
		}
		
		CryptoChallenge trustedSwitchOutCryptoChallenge = addressSlot.getTrustedSwitchOutCryptoChallenge();
		
		if(trustedSwitchOutCryptoChallenge == null) {
			Log.msg(this, "ignoring crypto challenge reply for address slot (didn't challenge the remote): %s", addressSlot);
			return;
		}
		
		Log.debug(this, "received crypto challenge reply for address slot: %s", addressSlot);
		
		Data cipherData = cryptoChallengeReplyInterserviceMessage.getDataComponent().getData();
		
		boolean success;
		try {
			success = trustedSwitchOutCryptoChallenge.verifyCipherData(cipherData);
		} catch (CryptoException e) {
			Log.exception(this, e, "could not verify crypto challenge reply for address slot: %s", addressSlot);
			// TODO notify remote of failure
			return;
		}
		
		finishTrustedSwitch(addressSlot, success);
		
		sendConnectionbaseNotice(addressSlot);
		
	}
	
	public void onReceiveConnectionbaseNoticeInterserviceMessage(ConnectionbaseNoticeInterserviceMessage connectionbaseNoticeInterserviceMessage) {

		byte outConnectionBase = connectionbaseNoticeInterserviceMessage.getConnectionBase();
		
		int addressSlotId = connectionbaseNoticeInterserviceMessage.getAddressSlot();
		AddressSlot addressSlot = localAddressSlotMap.get(addressSlotId);
		
		if(addressSlot == null) {
			Log.msg(this, "ignoring connection base notice message for empty address slot %d (connection base %d)", addressSlotId, outConnectionBase);
			return;
		}
		
		Log.debug(this, "received connection base notice message from remote, connection base %d for address slot: %s", outConnectionBase, addressSlot);
		setOutConnectionBase(addressSlot, outConnectionBase);
		
	}
	
	public void onReceiveNetworkJoinNoticeInterserviceMessage(NetworkJoinNoticeInterserviceMessage networkJoinNoticeInterserviceMessage) {

		int addressSlotId = networkJoinNoticeInterserviceMessage.getAddressSlot();
		int networkSlotId = networkJoinNoticeInterserviceMessage.getNetworkSlot();
		NetworkType networkType = networkJoinNoticeInterserviceMessage.getNetworkTypeComponent().getNetworkType();
		
		AddressSlot addressSlot = remoteAddressSlotMap.get(addressSlotId);
		
		if(addressSlot == null) {
			Log.msg(this, "ignoring network join notice for empty address slot id %d, network slot id is %d, network type is: %s", addressSlotId, networkSlotId, networkType);
			return;
		}
		
		if(addressSlot.getConnectionBase() < CONNECTIONBASE_TRUSTED) {
			Log.msg(this, "ignoring network join notice for network slot id %d, network type %s and address slot (incoming connection base insufficient): %s", networkSlotId, networkType, addressSlot);
			return;
		}
		
		Log.debug(this, "received network join notice for network type %s and network slot id %d on address slot: %s", networkType, networkSlotId, addressSlot);

		onNetworkJoinNotice(addressSlot, networkSlotId, networkType);
		
	}
	
	public void onReceiveNetworkLeaveNoticeInterserviceMessage(NetworkLeaveNoticeInterserviceMessage networkLeaveNoticeInterserviceMessage) {
		
		int addressSlotId = networkLeaveNoticeInterserviceMessage.getAddressSlot();
		int networkSlotId = networkLeaveNoticeInterserviceMessage.getNetworkSlot();
		
		AddressSlot addressSlot = remoteAddressSlotMap.get(addressSlotId);
		
		if(addressSlot == null) {
			Log.msg(this, "ignoring network leave notice for empty address slot id %d, network slot id is %d", addressSlotId, networkSlotId);
			return;
		}
		
		if(addressSlot.getConnectionBase() < CONNECTIONBASE_TRUSTED) {
			Log.msg(this, "ignoring network leave notice for address slot (incoming connection base insufficient): %s", addressSlot);
			return;
		}
		
		Log.debug(this, "received network leave notice for address slot %s, network slot id %d", addressSlot, networkSlotId);
		
		onNetworkLeaveNotice(addressSlot, networkSlotId);
		
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

		NetworkPacket networkPacket = networkPacketInterserviceMessage.getNetworkPacket();
		
		if(networkPacket.getNetworkSlot().getNetworkNodes().size() <= 0) {
			Log.msg(this, "ignoring network packet, no network nodes on network slot %s", networkPacket.getNetworkSlot());
			return;
		}
		
		Log.debug(this, "received network packet on slot %s: %s", networkPacket.getNetworkSlot(), networkPacket);
		onReceiveNetworkPacket(networkPacket);
		
	}

	@Override
	public NetworkPacket getNetworkPacket(int slot) {
		NetworkSlot networkSlot = localNetworkSlotMap.get(slot);
		return networkSlot == null ? null : networkSlot.getNetworkPacket();
	}
	
}
