package org.dclayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

import org.dclayer.PreLinkCommunicationManager.Result;
import org.dclayer.crypto.Crypto;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.ApplicationConnectionActionListener;
import org.dclayer.net.address.APBRAddress;
import org.dclayer.net.address.Address;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.interservice.InterserviceChannel;
import org.dclayer.net.interservice.InterserviceChannelActionListener;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.Link.Status;
import org.dclayer.net.link.LinkSendInterface;
import org.dclayer.net.link.OnLinkActionListener;
import org.dclayer.net.link.channel.data.DataChannel;
import org.dclayer.net.llacache.CachedLLA;
import org.dclayer.net.llacache.LLA;
import org.dclayer.net.llacache.LLACache;
import org.dclayer.net.lladatabase.LLADatabase;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkInstanceCollection;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.routing.RoutingTable;
import org.dclayer.net.socket.TCPSocket;
import org.dclayer.net.socket.UDPSocket;

public class DCLService implements OnReceiveListener, ApplicationConnectionActionListener, LinkSendInterface<CachedLLA>, OnLinkActionListener<CachedLLA>, InterserviceChannelActionListener, HierarchicalLevel {
	
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
	
	private APBRAddress localAddress;
	
	private NetworkInstanceCollection networkInstanceCollection = new NetworkInstanceCollection();
	
	private PreLinkCommunicationManager preLinkCommunicationManager = new PreLinkCommunicationManager(this);
	
	private ConnectionInitiationManager connectionInitiationManager;
	
	public DCLService(int s2sPort, int a2sPort, LLADatabase llaDatabase) throws IOException {
		
		this.llaDatabase = llaDatabase;
		
		Log.debug(this, "generating APBR address RSA keypair...");
		KeyPair addressKeyPair = Crypto.generateAPBRAddressRSAKeyPair();
		Log.debug(this, "done, public key: %s (%d bits)", addressKeyPair.getPublicKey().toString(), addressKeyPair.getPublicKey().getNumBits());
		this.localAddress = new APBRAddress(addressKeyPair, new NetworkInstanceCollection());
		
		udpSocket = new UDPSocket(this, s2sPort, this);
		tcpSocket = new TCPSocket(a2sPort, this);
		
		this.connectionInitiationManager = new ConnectionInitiationManager(this);
		
//		// TODO REMOVE
//		try {
//			addressCache.addServiceAddress(new ServiceAddressIPv4((Inet4Address) Inet4Address.getByAddress(new byte[] { 127, 0, 0, 1 }), s2sPort == 1337 ? 2337 : 1337), 0);
////			addressCache.addServiceAddress(new ServiceAddressIPv4((Inet4Address) Inet4Address.getByAddress(new byte[] { 10, (byte)0, 0, 3 }), 1337), 0);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
		
//		this.addProcess(new PingReceiveProcess()); // TODO move somewhere more special
//		this.addProcess(new PingRedirectReceiveProcess());
//		this.addProcess(new PongRedirectReceiveProcess());
//		this.addProcess(new KnownAddressesRequestReceiveProcess());
//		this.addProcess(new PingProcess());
//		
//		this.addProcess(new A2SBindReceiveProcess(this, this));
//		this.addProcess(new A2SKnownAddressesRequestReceiveProcess());
	}
	
	public APBRAddress getServiceAPBRAddress() {
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
	
	public void join(NetworkType networkType) {
		
		NetworkInstance networkInstance = new NetworkInstance(this, networkType, localAddress) {
			@Override
			public boolean onForward(NetworkPacket networkPacket) {
				Log.msg(this, "received NetworkPacket: %s", networkPacket.represent(true));
				return true;
			}
		};
		
		localAddress.getNetworkInstanceCollection().addNetworkInstance(networkInstance);
		networkInstanceCollection.addNetworkInstance(networkInstance);
		
		Log.msg(this, "joined network: %s", networkInstance);
		
	}
	
	@Override
	public void onReadyChange(InterserviceChannel interserviceChannel, boolean ready) {
		if(ready) {
			CachedLLA cachedLLA = interserviceChannel.getCachedLLA();
			cachedLLA.setStatus(CachedLLA.CONNECTED);
			Log.debug(this, "interservice channel to LLA %s ready", cachedLLA);
			
			// TODO decide how to continue (just leave it here, prove an address or even request integration?)
			
			// for now, if we actively connected to that LLA, let's prove our address
			if(interserviceChannel.isInitiator()) {
				interserviceChannel.startTrustedSwitch();
			}
		}
	}
	
	@Override
	public void onInConnectionBaseChange(InterserviceChannel interserviceChannel, byte oldInConnectionBase, byte newInConnectionBase) {
		if(newInConnectionBase >= InterserviceChannel.CONNECTIONBASE_TRUSTED) {
			// TODO carry out appropriate actions (i.e. begin routing, etc.)
		} else if(newInConnectionBase <= InterserviceChannel.CONNECTIONBASE_STRANGER) {
			// TODO carry out appropriate actions (i.e. stop routing, etc.)
		}
	}
	
	@Override
	public NetworkInstance onRemoteNetworkJoin(InterserviceChannel interserviceChannel, NetworkNode remoteNetworkNode) {
		
		Address localAddress = interserviceChannel.getLocalAddress();
		
		// TODO also check if we should maybe join that network
		
		NetworkInstance localNetworkInstance = localAddress.getNetworkInstanceCollection().findLocal(remoteNetworkNode.getNetworkType());
		
		if(localNetworkInstance != null) {
			
			RoutingTable routingTable = localNetworkInstance.getRoutingTable();
			boolean added = routingTable.add(remoteNetworkNode);
			
			if(added) {
				Log.debug(this, "added %s to routing table for %s", remoteNetworkNode, localNetworkInstance);
			}
			
			return added ? localNetworkInstance : null;
			
		}
		
		return null;
		
	}
	
	@Override
	public void onRemoteNetworkLeave(InterserviceChannel interserviceChannel, NetworkNode networkNode) {
		// TODO remove route for that network and the remote's address
	}
	
	@Override
	public ApplicationConnection onApplicationConnection(Socket socket) {
		return new ApplicationConnection(this, this, socket);
	}
	
	@Override
	public void onReceiveS2S(InetSocketAddress inetSocketAddress, DataByteBuf dataByteBuf) {
		
		InetAddress inetAddress = inetSocketAddress.getAddress();
		int port = inetSocketAddress.getPort();
		
		Link link;
		
		CachedLLA cachedLLA = llaCache.getIPPortCachedLLA(inetAddress, port, false);
		
		if(cachedLLA == null) {
			
			// we're being connected to
			
			Result result = preLinkCommunicationManager.permit(inetAddress, port, dataByteBuf);
			send(inetSocketAddress, result.echoData);
			
			if(result.done) {
				cachedLLA = llaCache.getIPPortCachedLLA(inetAddress, port, true);
				cachedLLA.setFirstLinkPacketPrefixData(result.firstLinkPacketPrefixData);
			}
			
			return;
			
		}
		
		link = cachedLLA.getLink();
		if(link == null) {
			
			if(cachedLLA.getFirstLinkPacketPrefixData() == null) {
			
				// we're connecting via pre-link communication
				Result result = preLinkCommunicationManager.echo(dataByteBuf);
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
				if(prefixData.equals(0, dataByteBuf.getData(), 0, prefixData.length())) {
				
					// the first link packet is prefixed with the expected data
					// -> create the link and feed it the rest (the latter happens at the bottom of this method)
					link = new Link<CachedLLA>(this, this, cachedLLA);
					cachedLLA.setLink(link);
					cachedLLA.setFirstLinkPacketPrefixData(null);
					dataByteBuf.seek(prefixData.length());
					
				} else {
					
					// the remote most likely didn't get our last confirmation packet.
					// -> repeat the pre-link communication
					Result result = preLinkCommunicationManager.permit(inetAddress, port, dataByteBuf);
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
			InterserviceChannel interserviceChannel = new InterserviceChannel(this, this, cachedLLA, localAddress, channelId, protocol);
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
