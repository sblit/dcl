package org.dclayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import org.dclayer.PreLinkCommunicationManager.Result;
import org.dclayer.apbr.APBRPacket;
import org.dclayer.apbr.APBRPacketForwardDestination;
import org.dclayer.crypto.Crypto;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.listener.net.FollowUpProcessSpawnInterface;
import org.dclayer.listener.net.OnConnectionErrorListener;
import org.dclayer.listener.net.OnProcessTimeoutListener;
import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.listener.net.ProcessRemoveInterface;
import org.dclayer.listener.net.ReceiveFollowUpProcessSpawnInterface;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.ConnectedPacket;
import org.dclayer.net.address.APBRAddress;
import org.dclayer.net.address.Address;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.interservice.InterserviceChannel;
import org.dclayer.net.interservice.InterserviceChannelActionListener;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.Link.Status;
import org.dclayer.net.link.LinkSendInterface;
import org.dclayer.net.link.OnLinkActionListener;
import org.dclayer.net.link.channel.data.DataChannel;
import org.dclayer.net.llacache.AddressCache;
import org.dclayer.net.llacache.CachedLLA;
import org.dclayer.net.llacache.CachedServiceAddress;
import org.dclayer.net.llacache.LLA;
import org.dclayer.net.llacache.LLACache;
import org.dclayer.net.lladatabase.LLADatabase;
import org.dclayer.net.network.NetworkPacket;
import org.dclayer.net.network.NetworkSlot;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.NetworkTypeCollection;
import org.dclayer.net.process.deliveryagent.A2SProcessDeliveryAgent;
import org.dclayer.net.process.deliveryagent.ProcessReceiveQueue;
import org.dclayer.net.process.queue.ProcessTimeoutQueue;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.routing.Nexthops;
import org.dclayer.net.routing.RoutingTable;
import org.dclayer.net.s2s.AddressedPacket;
import org.dclayer.net.socket.TCPSocket;
import org.dclayer.net.socket.TCPSocketConnection;
import org.dclayer.net.socket.UDPSocket;

public class DCLService implements APBRPacketForwardDestination, OnReceiveListener, LinkSendInterface<CachedLLA>, OnLinkActionListener<CachedLLA>, InterserviceChannelActionListener, HierarchicalLevel, OnProcessTimeoutListener, FollowUpProcessSpawnInterface, ProcessRemoveInterface, ReceiveFollowUpProcessSpawnInterface {
	
	/**
	 * local ProcessTimeoutQueue
	 */
	private ProcessTimeoutQueue processTimeoutQueue;
	
	/**
	 * local ProcessReceiveQueue
	 */
	private ProcessReceiveQueue processReceiveQueue;
	/**
	 * local A2SProcessDeliveryAgent
	 */
	private A2SProcessDeliveryAgent a2sProcessDeliveryAgent;
	
	/**
	 * local UDPSocket, used for Service-to-Service communication
	 */
	private UDPSocket udpSocket;
	/**
	 * local TCPSocket, used for Application-to-Service communication
	 */
	private TCPSocket tcpSocket;
	
	/**
	 * local AddressCache
	 */
	private AddressCache addressCache;
	
	private LLACache llaCache = new LLACache();
	private LLADatabase llaDatabase;
	
	private APBRAddress localAddress;
	
	private PreLinkCommunicationManager preLinkCommunicationManager = new PreLinkCommunicationManager(this);
	
	private ConnectionInitiationManager connectionInitiationManager;
	
	public DCLService(int s2sPort, int a2sPort, LLADatabase llaDatabase) throws IOException {
		
		this.llaDatabase = llaDatabase;
		
		Log.debug(this, "generating APBR address RSA keypair...");
		KeyPair addressKeyPair = Crypto.generateAPBRAddressRSAKeyPair();
		Log.debug(this, "done, public key: %s (%d bits)", addressKeyPair.getPublicKey().toString(), addressKeyPair.getPublicKey().getNumBits());
		this.localAddress = new APBRAddress(addressKeyPair, new NetworkTypeCollection());
		
		processTimeoutQueue = new ProcessTimeoutQueue(this);
		
		processReceiveQueue = new ProcessReceiveQueue(this, addressCache);
		a2sProcessDeliveryAgent = new A2SProcessDeliveryAgent(this);
		
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
	
	/**
	 * returns the {@link ProcessReceiveQueue} of this {@link DCLService} instance
	 * @return returns the {@link ProcessReceiveQueue} of this {@link DCLService} instance
	 */
	public ProcessReceiveQueue getProcessReceiveQueue() {
		return processReceiveQueue;
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
	public void onRemoteNetworkJoin(InterserviceChannel interserviceChannel, NetworkType networkType, Data remoteAddressData) {
		
		Address localAddress = interserviceChannel.getLocalAddress();
		
		// TODO also check if we should maybe join that network
		
		NetworkType localNetworkType = localAddress.getNetworkTypeCollection().findLocal(networkType);
		
		if(localNetworkType != null) {
			
			RoutingTable routingTable = localNetworkType.getRoutingTable();
			boolean added = routingTable.add(remoteAddressData, interserviceChannel);
			
			if(added) {
				Log.debug(this, "added scaled address %s (%s) to routing table for %s", remoteAddressData, localNetworkType, networkType.getScaledAddress());
			}
			
		}
		
	}
	
	@Override
	public void onRemoteNetworkLeave(InterserviceChannel interserviceChannel, NetworkType networkType, Data addressData) {
		// TODO remove route for that network and the remote's address
	}

	@Override
	public boolean onNetworkPacket(InterserviceChannel interserviceChannel, NetworkPacket networkPacket) {
		
		NetworkSlot networkSlot = networkPacket.getNetworkSlot();
		
		Nexthops nexthops = networkSlot.getNetworkType().getRoutingTable().lookup(networkPacket.getDestinationAddressData(), networkSlot.getAddressData(), 0);
		if(nexthops != null) {
			return nexthops.forward(networkPacket);
		}
		
		return false;
		
	}

	@Override
	public boolean onForward(APBRPacket apbrPacket) {
		Log.warning(this, "TODO: implement onForward(): %s", apbrPacket);
		return true;
	}

	@Override
	public Address getAddress() {
		return localAddress;
	}
	
	/**
	 * sends the given AddressedPacket's content to the AddressedPacket's ServiceAddress
	 * @param packet the AddressedPacket to send
	 */
	public void send(AddressedPacket packet) {
		Log.debug(Log.PART_NET_SERVICE_SEND_UDP, this, String.format("sending Packet via UDPSocket to %s: \n%s", packet.getServiceAddress().toString(), packet.represent(true)));
		DataByteBuf dataByteBuf = new DataByteBuf(packet.length());
		try {
			packet.write(dataByteBuf);
		} catch (BufException e) {
			Log.error(Log.PART_NET_SERVICE_SEND_UDP, this, String.format("could not write Packet to ByteBuf due to BufException: %s", packet.represent(true)));
			Log.exception(Log.PART_NET_SERVICE_SEND_UDP, this, e);
			return;
		}
		try {
			udpSocket.send(packet.getServiceAddress().getSocketAddress(), dataByteBuf.getData());
		} catch (IOException e) {
			Log.exception(Log.PART_NET_SERVICE_SEND_UDP, this, e);
			return;
		}
	}
	
	/**
	 * sends the given ConnectedPacket's content to the ConnectedPacket's connection
	 * @param packet the ConnectedPacket to send
	 */
	public void send(ConnectedPacket packet) {
		TCPSocketConnection tcpSocketConnection = packet.getTCPSocketConnection();
		Log.debug(Log.PART_NET_SERVICE_SEND_UDP, this, String.format("sending Packet via TCPSocketConnection to %s:%d: \n%s", tcpSocketConnection.getInetAddress().toString(), tcpSocketConnection.getPort(), packet.represent(true)));
		try {
			packet.write(tcpSocketConnection.getByteBuf());
		} catch (BufException e) {
			Log.error(Log.PART_NET_SERVICE_SEND_UDP, this, String.format("could not write Packet to ByteBuf due to BufException: %s", packet.represent(true)));
			Log.exception(Log.PART_NET_SERVICE_SEND_UDP, this, e);
			return;
		}
	}

	@Override
	public boolean onReceiveA2S(TCPSocketConnection tcpSocketConnection, ByteBuf byteBuf) {
		boolean keep = processOnReceiveA2S(tcpSocketConnection, byteBuf);
		if(!keep) {
			OnConnectionErrorListener onConnectionErrorListener = tcpSocketConnection.getApplicationConnection().getOnConnectionErrorListener();
			if(onConnectionErrorListener != null) onConnectionErrorListener.onConnectionError();
		}
		return keep;
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
	
	/**
	 * tries to parse an application-to-service message from the given {@link ByteBuf}
	 * @param tcpSocketConnection the {@link TCPSocketConnection} from which the message is received
	 * @param byteBuf the {@link ByteBuf} from which the message is read
	 * @return true if parsing succeeded, false if parsing failed and the connection should be closed
	 */
	private boolean processOnReceiveA2S(TCPSocketConnection tcpSocketConnection, ByteBuf byteBuf) {
		ConnectedPacket packet;
		try {
			packet = new ConnectedPacket(byteBuf, tcpSocketConnection);
		} catch (BufException e) {
			Log.exception(Log.PART_NET_SERVICE_RECEIVEA2S, this, e);
			return false;
		} catch (ParseException e) {
			Log.exception(Log.PART_NET_SERVICE_RECEIVEA2S, this, e);
			return true;
		}
		Log.debug(Log.PART_NET_SERVICE_RECEIVEA2S, this, String.format("received Application-to-Service Packet from %s:%d: \n%s", tcpSocketConnection.getInetAddress(), tcpSocketConnection.getPort(), packet.represent(true)));
		
		a2sProcessDeliveryAgent.receive(packet);
		
		return true;
	}
	
	/**
	 * add a Process for execution
	 * @param process the Process to add for execution
	 */
	public void addProcess(Process process) {
		Log.debug(Log.PART_NET_SERVICE_PROCESS_ADD, this, String.format("adding Process: %s", process.toString()));
		initializeProcess(process);
		executeProcess(process);
	}
	
	/**
	 * stops execution of a Process
	 * @param process the Process to stop
	 */
	public void removeProcess(Process process) {
		Log.debug(Log.PART_NET_SERVICE_PROCESS_REMOVE, this, String.format("removing Process: %s", process.toString()));
		finalizeProcess(process);
	}
	
	@Override
	public void addFollowUpProcess(Process originalProcess, Process followUpProcess) {
		addProcess(followUpProcess);
	}
	
	@Override
	public void removeProcess(Process originalProcess, Process removeProcess) {
		removeProcess(removeProcess);
	}

	@Override
	public void addReceiveFollowUpProcess(Process originalProcess, Process followUpProcess) {
		if(!originalProcess.isPersistent()) finalizeProcess(originalProcess);
		if(followUpProcess != Process.NULLPROCESS) addFollowUpProcess(originalProcess, followUpProcess);
	}
	
	/**
	 * sends a Processes Service-to-Service message
	 * @param process the Process holding the message
	 */
	private void sendProcessS2S(Process process) {
		CachedServiceAddress cachedServiceAddress = process.getCachedServiceAddress();
		RevisionMessage revisionMessage = process.getS2SMessage(DCL.REVISION); // TODO set revision based on CachedServiceAddress
		this.send(new AddressedPacket(revisionMessage, cachedServiceAddress.getServiceAddress()));
	}
	
	/**
	 * sends a Processes Application-to-Service message
	 * @param process the Process holding the message
	 */
	private void sendProcessA2S(Process process) {
		ApplicationConnection applicationConnection = process.getApplicationConnection();
		RevisionMessage revisionMessage = process.getA2SMessage(applicationConnection.getRevision()); // TODO set revision based on ApplicationConnection
		this.send(new ConnectedPacket(revisionMessage, applicationConnection.getTCPSocketConnection()));
	}
	
	/**
	 * initializes a Process
	 * @param process the Process to initialize
	 */
	private void initializeProcess(Process process) {
		Log.debug(Log.PART_NET_SERVICE_PROCESS_INIT, this, String.format("initializing Process: %s", process.toString()));
		process.init(addressCache);
		if(process.hasS2SReceiver()) {
			processReceiveQueue.addProcess(process);
		}
		if(process.hasA2SReceiver()) {
			a2sProcessDeliveryAgent.addProcess(process);
		}
		if(process.isDaemon()) {
			process.start(this);
		}
		if(process.hasLink() && process.hasCachedServiceAddressFilter()) {
			CachedServiceAddress cachedServiceAddress = process.getCachedServiceAddressFilter().getCachedServiceAddress();
			Link link = process.getLink();
			Log.debug(Log.PART_NET_SERVICE_PROCESS_INIT, this, String.format("assigning CachedServiceAddress %s the Link %s", cachedServiceAddress, link));
			cachedServiceAddress.link = link;
		}
	}
	
	/**
	 * executes a Process
	 * @param process the Process to execute
	 */
	private void executeProcess(Process process) {
		Log.debug(Log.PART_NET_SERVICE_PROCESS_EXEC, this, String.format("executing Process: %s", process.toString()));
		if(process.hasTimeout()) {
			processTimeoutQueue.add(process, process.getTimeout());
			
		} else {
			boolean s2sMsg = process.hasS2SMessage(), a2sMsg = process.hasA2SMessage(), finalize = s2sMsg || a2sMsg;
			if(finalize) finalizeProcess(process);
			if(s2sMsg) sendProcessS2S(process);
			if(a2sMsg) sendProcessA2S(process);
		}
	}
	
	/**
	 * finalizes a Process
	 * @param process the Process to finalize
	 * @param timeout whether or not this Process is finalized due to a timeout (true if timeout, false if normal)
	 */
	private void finalizeProcess(Process process, boolean timeout) {
		Log.debug(Log.PART_NET_SERVICE_PROCESS_FINALIZE, this, String.format("finalizing Process: %s", process.toString()));
		if(process.hasS2SReceiver()) {
			processReceiveQueue.removeProcess(process);
		}
		if(process.hasA2SReceiver()) {
			a2sProcessDeliveryAgent.removeProcess(process);
		}
		if(process.hasFinalizeCallback()) {
			Process followUpProcess = process.onFinalize(timeout);
			if(followUpProcess != null) addFollowUpProcess(process, followUpProcess);
		}
		process.setDead(true);
	}
	
	/**
	 * finalizes a Process
	 * @param process the Process to finalize
	 */
	private void finalizeProcess(Process process) {
		finalizeProcess(process, false);
	}

	@Override
	public void onProcessTimeout(Process process) {
		if(!process.isDead()) finalizeProcess(process, true);
	}

}
