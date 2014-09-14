package org.dclayer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.listener.net.FollowUpProcessSpawnInterface;
import org.dclayer.listener.net.OnConnectionErrorListener;
import org.dclayer.listener.net.OnProcessTimeoutListener;
import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.listener.net.ProcessRemoveInterface;
import org.dclayer.listener.net.ReceiveFollowUpProcessSpawnInterface;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.ConnectedPacket;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.link.Link;
import org.dclayer.net.process.A2SBindReceiveProcess;
import org.dclayer.net.process.A2SKnownAddressesRequestReceiveProcess;
import org.dclayer.net.process.KnownAddressesRequestReceiveProcess;
import org.dclayer.net.process.PingProcess;
import org.dclayer.net.process.PingReceiveProcess;
import org.dclayer.net.process.PingRedirectReceiveProcess;
import org.dclayer.net.process.PongRedirectReceiveProcess;
import org.dclayer.net.process.deliveryagent.A2SProcessDeliveryAgent;
import org.dclayer.net.process.deliveryagent.ProcessReceiveQueue;
import org.dclayer.net.process.queue.ProcessTimeoutQueue;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.AddressedPacket;
import org.dclayer.net.serviceaddress.ServiceAddress;
import org.dclayer.net.serviceaddress.ServiceAddressIPv4;
import org.dclayer.net.serviceaddress.ServiceAddressIPv6;
import org.dclayer.net.socket.TCPSocket;
import org.dclayer.net.socket.TCPSocketConnection;
import org.dclayer.net.socket.UDPSocket;

public class DCLService implements OnReceiveListener, OnProcessTimeoutListener, FollowUpProcessSpawnInterface, ProcessRemoveInterface, ReceiveFollowUpProcessSpawnInterface {
	
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
	
	public DCLService(int s2sPort, int a2sPort, AddressCache addressCache) throws IOException {
		
		this.addressCache = addressCache;
		
		processTimeoutQueue = new ProcessTimeoutQueue(this);
		
		processReceiveQueue = new ProcessReceiveQueue(this, addressCache);
		a2sProcessDeliveryAgent = new A2SProcessDeliveryAgent(this);
		
		udpSocket = new UDPSocket(s2sPort, this);
		tcpSocket = new TCPSocket(a2sPort, this);
		
//		// TODO REMOVE
//		try {
//			addressCache.addServiceAddress(new ServiceAddressIPv4((Inet4Address) Inet4Address.getByAddress(new byte[] { 127, 0, 0, 1 }), s2sPort == 1337 ? 2337 : 1337), 0);
////			addressCache.addServiceAddress(new ServiceAddressIPv4((Inet4Address) Inet4Address.getByAddress(new byte[] { 10, (byte)0, 0, 3 }), 1337), 0);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
		
		this.addProcess(new PingReceiveProcess()); // TODO move somewhere more special
		this.addProcess(new PingRedirectReceiveProcess());
		this.addProcess(new PongRedirectReceiveProcess());
		this.addProcess(new KnownAddressesRequestReceiveProcess());
		this.addProcess(new PingProcess());
		
		this.addProcess(new A2SBindReceiveProcess(this, this));
		this.addProcess(new A2SKnownAddressesRequestReceiveProcess());
	}
	
	/**
	 * returns the {@link ProcessReceiveQueue} of this {@link DCLService} instance
	 * @return returns the {@link ProcessReceiveQueue} of this {@link DCLService} instance
	 */
	public ProcessReceiveQueue getProcessReceiveQueue() {
		return processReceiveQueue;
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
	public void onReceiveS2S(InetAddress inetAddress, int port, ByteBuf byteBuf) {
		ServiceAddress serviceAddress;
		if(inetAddress instanceof Inet4Address) {
			serviceAddress = new ServiceAddressIPv4((Inet4Address)inetAddress, port);
		} else if(inetAddress instanceof Inet6Address) {
			serviceAddress = new ServiceAddressIPv6((Inet6Address)inetAddress, port);
		} else {
			Log.error(Log.PART_NET_SERVICE_RECEIVES2S, this, String.format("cannot process packet due to unsupported address (type '%s'): %s", inetAddress.getClass().getName(), inetAddress.toString()));
			return;
		}
		
		AddressedPacket packet;
		try {
			packet = new AddressedPacket(byteBuf, serviceAddress);
		} catch (BufException e) {
			Log.exception(Log.PART_NET_SERVICE_RECEIVES2S, this, e);
			return;
		} catch (ParseException e) {
			Log.exception(Log.PART_NET_SERVICE_RECEIVES2S, this, e);
			return;
		}
		Log.debug(Log.PART_NET_SERVICE_RECEIVES2S, this, String.format("received Service-to-Service Packet from %s (%s:%d): \n%s", serviceAddress.toString(), inetAddress.toString(), port, packet.represent(true)));
		
		processReceiveQueue.receive(packet);
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
