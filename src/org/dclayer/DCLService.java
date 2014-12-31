package org.dclayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.dclayer.PreLinkCommunicationManager.Result;
import org.dclayer.crypto.Crypto;
import org.dclayer.crypto.key.Key;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.listener.net.NetworkInstanceListener;
import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.ApplicationConnectionActionListener;
import org.dclayer.net.address.Address;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.crisp.CrispMessageReceiver;
import org.dclayer.net.crisp.CrispPacket;
import org.dclayer.net.crisp.message.NeighborRequestCrispMessageI;
import org.dclayer.net.interservice.InterserviceChannel;
import org.dclayer.net.interservice.InterserviceChannelActionListener;
import org.dclayer.net.interservice.InterservicePolicy;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.Link.Status;
import org.dclayer.net.link.LinkSendInterface;
import org.dclayer.net.link.OnLinkActionListener;
import org.dclayer.net.link.channel.data.DataChannel;
import org.dclayer.net.llacache.CachedLLA;
import org.dclayer.net.llacache.InetSocketLLA;
import org.dclayer.net.llacache.LLA;
import org.dclayer.net.llacache.LLACache;
import org.dclayer.net.lladatabase.LLADatabase;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkInstanceCollection;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.component.NetworkPayload;
import org.dclayer.net.network.routing.RoutingTable;
import org.dclayer.net.network.slot.NetworkSlot;
import org.dclayer.net.socket.TCPSocket;
import org.dclayer.net.socket.UDPSocket;

public class DCLService implements CrispMessageReceiver<NetworkInstance>, OnReceiveListener, NetworkInstanceListener, ApplicationConnectionActionListener, LinkSendInterface<CachedLLA>, OnLinkActionListener<CachedLLA>, InterserviceChannelActionListener, HierarchicalLevel {
	
	/**
	 * local UDPSocket, used for Service-to-Service communication
	 */
	private UDPSocket udpSocket;
	/**
	 * local TCPSocket, used for Application-to-Service communication
	 */
	private TCPSocket tcpSocket;
	
	private LLACache llaCache = new LLACache();
	private LLADatabase llaDatabase;
	
	private Address localAddress;
	
	private NetworkInstanceCollection networkInstanceCollection = new NetworkInstanceCollection();
	
	private PreLinkCommunicationManager preLinkCommunicationManager = new PreLinkCommunicationManager(this);
	
	private ConnectionInitiationManager connectionInitiationManager;
	
	private List<InterserviceChannel> interserviceChannels = new LinkedList<>();
	private List<NetworkNode> networkNodes = new LinkedList<>();
	
	private DataByteBuf networkPayloadDataByteBuf = new DataByteBuf();
	private CrispPacket inCrispPacket = new CrispPacket();
	
	private LLA localLLA; // TODO remove
	
	public DCLService(int s2sPort, int a2sPort, LLADatabase llaDatabase) throws IOException {
		
		this.llaDatabase = llaDatabase;
		
		Log.debug(this, "generating address RSA keypair...");
		KeyPair addressKeyPair = Crypto.generateAddressRSAKeyPair();
		Log.debug(this, "done, public key: %s (%d bits)", addressKeyPair.getPublicKey().toString(), addressKeyPair.getPublicKey().getNumBits());
		this.localAddress = new Address<>(addressKeyPair, new NetworkInstanceCollection());
		
		onAddress(localAddress);
		
		udpSocket = new UDPSocket(this, s2sPort, this);
		tcpSocket = new TCPSocket(a2sPort, this);
		
		this.connectionInitiationManager = new ConnectionInitiationManager(this);
		
	}
	
	public Address getServiceAddress() {
		return localAddress;
	}
	
	public LLACache getLLACache() {
		return llaCache;
	}
	
	// TODO remove!
	public List<LLA> getLLAs() {
		return llaDatabase.getLLAs();
	}
	
	public void storeLLAs(List<LLA> llas) {
		llaDatabase.store(llas);
	}

	@Override
	public LLA getLocalLLA() {
		// TODO
		return localLLA;
	}
	
	// TODO remove
	public void setLocalLLA(LLA localLLA) {
		this.localLLA = localLLA;
	}
	
	@Override
	public Data getServiceIgnoreData() {
		return preLinkCommunicationManager.getCurrentIgnoreData();
	}
	
	public void join(NetworkType networkType) {
		
		final NetworkPayload inNetworkPayload = networkType.makeInNetworkPayload(null);
		
		NetworkInstance networkInstance = new NetworkInstance(this, networkType, localAddress, true) {
			@Override
			public synchronized boolean onForward(NetworkPacket networkPacket) {
				Log.msg(this, "received NetworkPacket: %s", networkPacket.represent(true));
				inNetworkPayload.setReadDataComponent(networkPacket.getDataComponent());
				DCLService.this.onForward(inNetworkPayload, this);
				return true;
			}

			@Override
			public void neighborRequest(Key senderPublicKey, String actionIdentifier, LLA senderLLA, boolean response, Data ignoreData) {
				onNeighborRequest(this, senderPublicKey, actionIdentifier, senderLLA, response);
			}
		};
		
		localAddress.getNetworkInstanceCollection().addNetworkInstance(networkInstance);
		onNetworkInstance(networkInstance);
		
		Log.msg(this, "joined network: %s", networkInstance);
		
	}
	
	private void onForward(NetworkPayload networkPayload, NetworkInstance networkInstance) {
		
		try {
			
			networkPayload.read();
			
		} catch (ParseException e) {
			
			Log.exception(this, e);
			return;
			
		} catch (BufException e) {
			
			Log.exception(this, e);
			return;
			
		}
		
		if(networkPayload.isDestinedForService()) {
			
			Log.debug(this, "received network payload destined for service: %s", networkPayload.represent(true));
			onServiceNetworkPayload(networkPayload, networkInstance);
			
		} else {
			
			Log.debug(this, "service received network payload that is not destined for service, ignoring: ", networkPayload.represent(true));
			// TODO
			
		}
		
	}
	
	@Override
	public void onReadyChange(InterserviceChannel interserviceChannel, boolean ready) {
		if(ready) {
			CachedLLA cachedLLA = interserviceChannel.getCachedLLA();
			cachedLLA.setStatus(CachedLLA.CONNECTED);
			Log.debug(this, "interservice channel to LLA %s ready", cachedLLA);
		}
	}
	
	@Override
	public void onNewRemoteNetworkNode(InterserviceChannel interserviceChannel, NetworkNode remoteNetworkNode, NetworkSlot localNetworkSlot) {
		
		// TODO also check if we should maybe join that network
		
		// just use the first network instance with the same network type (as they are all connected anyways)
		NetworkInstance localNetworkInstance = networkInstanceCollection.findLocal(remoteNetworkNode.getNetworkType());
		
		if(localNetworkInstance != null) {
			
			RoutingTable routingTable = localNetworkInstance.getRoutingTable();
			boolean added = routingTable.add(remoteNetworkNode);
			
			if(added) {
				
				Log.debug(this, "added %s to routing table for %s", remoteNetworkNode, localNetworkInstance);
				
			}
			
		}
		
	}
	
	@Override
	public void onRemoveRemoteNetworkNode(InterserviceChannel interserviceChannel, NetworkNode networkNode) {
		// TODO remove route for that network and the remote's address
	}
	
	@Override
	public ApplicationConnection onApplicationConnection(Socket socket) {
		return new ApplicationConnection(this, this, socket);
	}
	
	@Override
	public synchronized void onAddress(Address asymmetricKeyPairAddress) {
		// do nothing (add AddressSlots when adding NetworkInstances)
	}
	
	@Override
	public synchronized void onNetworkInstance(NetworkInstance networkInstance) {
		Log.msg(this, "onNetworkInstance: %s", networkInstance);
		networkInstanceCollection.addNetworkInstance(networkInstance);
		for(InterserviceChannel interserviceChannel : interserviceChannels) {
			interserviceChannel.joinNetwork(networkInstance);
		}
	}

	@Override
	public synchronized void onServiceNetworkPayload(NetworkPayload networkPayload, NetworkInstance networkInstance) {
		
		networkPayloadDataByteBuf.setData(networkPayload.getPayloadData());
		
		try {
			
			inCrispPacket.read(networkPayloadDataByteBuf);
			
		} catch (ParseException e) {
			Log.exception(this, e);
			return;
		} catch (BufException e) {
			Log.exception(this, e);
			return;
		}
		
		Log.debug(this, "crisp packet received: %s", inCrispPacket.represent(true));
		
		inCrispPacket.getCrispMessage().callOnReceiveMethod(this, networkInstance);
		
	}

	@Override
	public void onReceiveNeighborRequestCrispMessage(NeighborRequestCrispMessageI neighborRequestCrispMessage, NetworkInstance networkInstance) {
		
		// TODO verify senderPublicKey!
		
		Key senderPublicKey = neighborRequestCrispMessage.getPublicKey();
		String actionIdentifier = neighborRequestCrispMessage.getActionIdentifier();
		LLA senderLLA = neighborRequestCrispMessage.getSenderLLA();
		boolean response = neighborRequestCrispMessage.isResponse();
		Data ignoreData = response ? null : neighborRequestCrispMessage.getIgnoreDataComponent().getData();
		
		networkInstance.neighborRequest(senderPublicKey, actionIdentifier, senderLLA, response, ignoreData);
		
	}
	
	//
	
	@Override
	public synchronized void connect(LLA lla, InterservicePolicy interservicePolicy) {
		CachedLLA cachedLLA = getLLACache().getCachedLLA(lla, true);
		cachedLLA.setInterservicePolicy(interservicePolicy);
		connectionInitiationManager.connect(cachedLLA);
	}
	
	/**
	 * sends a random packet to the given LLA in order to punch a hole in a local NAT
	 */
	@Override
	public void prepareForIncomingConnection(LLA lla, InterservicePolicy interservicePolicy, Data ignoreData) {
		CachedLLA cachedLLA = getLLACache().getCachedLLA(lla, true);
		cachedLLA.setInterservicePolicy(interservicePolicy);
		connectionInitiationManager.punch(cachedLLA, ignoreData);
	}
	
	//
	
	public void onNeighborRequest(NetworkInstance networkInstance, Key senderPublicKey, String actionIdentifier, LLA senderLLA, boolean response) {
		
		Log.debug(this, "ignoring neighbor request for service's address: actionIdentifier=%s senderLLA=%s", actionIdentifier, senderLLA);
		
	}
	
	@Override
	public void onReceiveS2S(InetSocketAddress inetSocketAddress, DataByteBuf dataByteBuf, Data data) {
		
		InetAddress inetAddress = inetSocketAddress.getAddress();
		int port = inetSocketAddress.getPort();
		
		Link link;
		
		CachedLLA cachedLLA = llaCache.getIPPortCachedLLA(inetAddress, port, false);
		
		if(cachedLLA == null || cachedLLA.disconnected()) {
			
			// we're being connected to
			
			Result result = preLinkCommunicationManager.permit(inetAddress, port, data);
			
			if(result != null) {
			
				send(inetSocketAddress, result.echoData);
				
				if(result.done) {
					cachedLLA = llaCache.getIPPortCachedLLA(inetAddress, port, true);
					cachedLLA.setStatus(CachedLLA.CONNECTING_PRELINK);
					cachedLLA.setFirstLinkPacketPrefixData(result.firstLinkPacketPrefixData);
				}
				
			}
				
			return;
			
		}
		
		link = cachedLLA.getLink();
		if(link == null) {
			
			if(cachedLLA.getFirstLinkPacketPrefixData() == null) {
				
				if(cachedLLA.disconnected()) {
					return;
				}
			
				// we're connecting via pre-link communication
				Result result = preLinkCommunicationManager.echo(data);
				if(result.done) {
					cachedLLA.setLink(link = new Link<CachedLLA>(this, this, cachedLLA));
					cachedLLA.setStatus(CachedLLA.CONNECTING_LINK);
					link.connect(result.firstLinkPacketPrefixData);
					return;
				}
				
				send(inetSocketAddress, result.echoData);
				return;
				
			} else {
				
				// we're being connected to and pre-link communication is already completed
				
				Data prefixData = cachedLLA.getFirstLinkPacketPrefixData();
				if(prefixData.equals(0, data, 0, prefixData.length())) {
				
					// the first link packet is prefixed with the expected data
					// -> create the link and feed it the rest (the latter happens at the bottom of this method)
					link = new Link<CachedLLA>(this, this, cachedLLA);
					cachedLLA.setLink(link);
					cachedLLA.setFirstLinkPacketPrefixData(null);
					cachedLLA.setStatus(CachedLLA.CONNECTING_LINK);
					dataByteBuf.skip(prefixData.length());
					
				} else {
					
					// the remote most likely didn't get our last confirmation packet.
					// -> repeat the pre-link communication
					Result result = preLinkCommunicationManager.permit(inetAddress, port, data);
					send(inetSocketAddress, result.echoData);
					return;
					
				}
				
			}
			
		}
		
		// normal operation
		link.onReceive(dataByteBuf);
		
	}

	@Override
	public void sendLinkPacket(CachedLLA cachedLLA, Data data) {
		send(cachedLLA, data);
	}
	
	public void send(CachedLLA cachedLLA, Data data) {
		send(cachedLLA.getLLA().getSocketAddress(), data);
	}
	
	private void send(SocketAddress inetSocketAddress, Data data) {
		try {
			udpSocket.send(inetSocketAddress, data);
		} catch (IOException e) {
			Log.exception(this, e, "Exception while sending link packet to %s", inetSocketAddress);
			return;
		}
	}

	@Override
	public DataChannel onOpenChannelRequest(CachedLLA cachedLLA, long channelId, String protocol) {
		switch(protocol) {
		case "org.dclayer.interservice": {
			cachedLLA.setStatus(CachedLLA.CONNECTING_CHANNEL);
			InterserviceChannel interserviceChannel = new InterserviceChannel(this, this, cachedLLA, channelId, protocol);
			synchronized(this) {
				interserviceChannels.add(interserviceChannel);
				for(NetworkNode networkNode : networkInstanceCollection) {
					interserviceChannel.joinNetwork(networkNode);
				}
			}
			cachedLLA.setInterserviceChannel(interserviceChannel);
			return interserviceChannel;
		}
		}
		return null;
	}

	@Override
	public void onLinkStatusChange(CachedLLA cachedLLA, Status oldStatus, Status newStatus) {
		Link link = cachedLLA.getLink();
		if(newStatus == Link.Status.Connected && link.isInitiator()) {
			Log.debug(this, "link %s is connected, opening interservice channel", link);
			link.openChannel("org.dclayer.interservice");
		}
	}

	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		return null;
	}
	
	@Override
	public String toString() {
		return "DCLService";
	}

}
