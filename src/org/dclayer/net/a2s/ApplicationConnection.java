package org.dclayer.net.a2s;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.dclayer.DCL;
import org.dclayer.crypto.Crypto;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.a2s.message.DataMessageI;
import org.dclayer.net.a2s.message.RevisionMessageI;
import org.dclayer.net.a2s.message.SlotAssignMessageI;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.a2s.rev35.Rev35Message;
import org.dclayer.net.address.Address;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.componentinterface.AbsKeyComponentI;
import org.dclayer.net.network.ApplicationNetworkInstance;
import org.dclayer.net.network.NetworkInstanceCollection;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.component.NetworkPayload;
import org.dclayer.net.network.properties.CommonNetworkPayloadProperties;
import org.dclayer.net.network.slot.NetworkSlot;
import org.dclayer.net.network.slot.NetworkSlotMap;

/**
 * a connection to an application instance
 */
public class ApplicationConnection extends Thread implements A2SMessageReceiver, HierarchicalLevel {
	
	private Socket socket;
	
	private HierarchicalLevel parentHierarchicalLevel;
	private ApplicationConnectionActionListener applicationConnectionActionListener;
	
	private StreamByteBuf streamByteBuf;
	
	private final Rev0Message receiveRev0Message = new Rev0Message();
	private final Rev0Message sendRev0Message = new Rev0Message();
	
	private final Rev35Message receiveRev35Message = new Rev35Message();
	private final Rev35Message sendRev35Message = new Rev35Message();
	
	private A2SMessage receiveMessage = receiveRev35Message;
	private A2SMessage sendMessage = sendRev35Message;
	
	private NetworkSlotMap networkSlotMap = new NetworkSlotMap();
	
	//
	
	private int revision = 0;
	
	private KeyPair applicationAddressKeyPair = null;
	private Address applicationAddress = null;
	
	/**
	 * create a new {@link ApplicationConnection} for the given {@link TCPSocketConnection}
	 * @param tcpSocketConnection the {@link TCPSocketConnection} to create an {@link ApplicationConnection} for
	 */
	public ApplicationConnection(HierarchicalLevel parentHierarchicalLevel, ApplicationConnectionActionListener applicationConnectionActionListener, Socket socket) {
		
		this.socket = socket;
		this.parentHierarchicalLevel = parentHierarchicalLevel;
		this.applicationConnectionActionListener = applicationConnectionActionListener;
		
		InputStream inputStream;
		OutputStream outputStream;
		
		try {
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			Log.exception(Log.PART_NET_TCPSOCKETCONNECTION, this, e);
			return;
		}
		
		try {
			outputStream = socket.getOutputStream();
		} catch (IOException e) {
			Log.exception(Log.PART_NET_TCPSOCKETCONNECTION, this, e);
			return;
		}
		
		this.streamByteBuf = new StreamByteBuf(inputStream, outputStream);
		
		this.start();
		
	}
	
	public void setApplicationAddressKeyPair(KeyPair applicationAddressKeyPair) {
		this.applicationAddressKeyPair = applicationAddressKeyPair;
		this.applicationAddress = new Address(applicationAddressKeyPair, new NetworkInstanceCollection());
		applicationConnectionActionListener.onAddress(applicationAddress);
	}
	
	private void generateKeyPair() {
		Log.msg(this, "generating rsa address key pair");
		setApplicationAddressKeyPair(Crypto.generateAddressRSAKeyPair());
		Log.msg(this, "generated %d bits rsa address key pair", this.applicationAddressKeyPair.getPublicKey().getNumBits());
	}
	
	private void onForward(NetworkPayload networkPayload, NetworkSlot networkSlot, NetworkPacket networkPacket) {
		
		Log.debug(this, "received NetworkPacket: %s", networkPacket.represent(true));
		
		try {
			networkPayload.read();
		} catch (ParseException e) {
			Log.exception(this, e, "could not parse network payload");
			return;
		} catch (BufException e) {
			Log.exception(this, e, "could not parse network payload");
			return;
		}
		
		if(networkPayload.destinedForService()) {
			// TODO
		} else {
			Log.debug(this, "sending data message to application containing network payload: %s", networkPayload.represent(true));
			sendDataMessage(networkSlot.getSlot(), networkPayload);
		}
		
	}
	
	private void joinNetwork(final NetworkType networkType) {
		
		final NetworkPayload inNetworkPayload = networkType.makeInNetworkPayload(null);
		
		ApplicationNetworkInstance applicationNetworkInstance = new ApplicationNetworkInstance(this, networkType, applicationAddress) {
			@Override
			public synchronized boolean onForward(NetworkPacket networkPacket, NetworkSlot networkSlot) {
				inNetworkPayload.setReadDataComponent(networkPacket.getDataComponent());
				ApplicationConnection.this.onForward(inNetworkPayload, networkSlot, networkPacket);
				return true;
			}
		};
		
		applicationAddress.getNetworkInstanceCollection().addNetworkInstance(applicationNetworkInstance);
		
		applicationConnectionActionListener.onNetworkInstance(applicationNetworkInstance);
		
		NetworkSlot networkSlot = networkSlotMap.add(networkType);
		networkSlot.addNetworkNode(applicationNetworkInstance);
		applicationNetworkInstance.setNetworkSlot(networkSlot);
		
		// TODO make these changeable for the connected application
		CommonNetworkPayloadProperties commonNetworkPayloadProperties = new CommonNetworkPayloadProperties()
				.destinedForService(false)
				.sourceAddress(true);
		
		applicationNetworkInstance.setOutNetworkPayload(commonNetworkPayloadProperties);
		
		Log.msg(this, "joined network: %s", networkSlot);
		
		sendSlotAssignMessage(networkSlot.getSlot(), networkType, applicationNetworkInstance.getScaledAddress());
		
	}
	
	@Override
	public void run() {
		for(;;) {
			
			try {
				receiveMessage.read(streamByteBuf);
			} catch (ParseException e) {
				Log.exception(this, e);
				close();
				return;
			} catch (BufException e) {
				Log.exception(this, e);
				close();
				return;
			}
			
			Log.debug(this, "received application to service message: %s", receiveMessage.represent(true));
			
			receiveMessage.callOnReceiveMethod(this);
			
		}
	}
	
	private void close() {
		try {
			socket.close();
		} catch (IOException e) {
			Log.exception(this, e, "exception while closing Socket");
		}
	}
	
	private void send() {
		try {
			sendMessage.write(streamByteBuf);
		} catch (BufException e) {
			Log.exception(this, e, "error while sending application to service message %s", sendMessage.represent(true));
			return;
		}
	}
	
	//
	
	private void sendNetworkPacket(NetworkSlot networkSlot, Data addressData, Data data) {
		
		NetworkNode networkNode = networkSlot.getNetworkNodes().get(0);

		NetworkPacket networkPacket = networkSlot.getNetworkPacket();
		NetworkPayload networkPayload = networkNode.getOutNetworkPayload();
		
		synchronized(networkNode) {
			
			networkPacket.setDestinationAddressData(addressData);
			networkPayload.setPayloadData(data);
			
			try {
				networkPayload.write(networkPacket.getDataComponent());
			} catch (BufException e) {
				Log.exception(this, e, "could not write network packet payload");
				return;
			}
			
			networkNode.forward(networkPacket);
			
		}
		
	}
	
	//
	
	private synchronized void sendRevisionMessage(int revision) {
		RevisionMessageI revisionMessage = sendMessage.setRevisionMessage();
		revisionMessage.setRevision(revision);
		send();
	}
	
	private synchronized void sendDataMessage(int slot, NetworkPayload networkPayload) {
		DataMessageI dataMessage = sendMessage.setDataMessage();
		dataMessage.getSlotNumComponent().setNum(slot);
		dataMessage.getAddressComponent().setAddressData(networkPayload.getSourceAddressData()); // this may be null, doesn't matter though
		dataMessage.getDataComponent().setData(networkPayload.getPayloadData());
		send();
	}
	
	private synchronized void sendSlotAssignMessage(int slot, NetworkType networkType, Data addressData) {
		SlotAssignMessageI slotAssignMessage = sendMessage.setSlotAssignMessage();
		slotAssignMessage.setSlot(slot);
		slotAssignMessage.getNetworkTypeComponent().setNetworkType(networkType);
		slotAssignMessage.setAddressData(addressData);
		send();
	}
	
	//
	
	public synchronized void onReceiveRevisionMessage(int revision) {
		
		Log.debug(this, "onReceiveRevisionMessage(%d)", revision);

		this.revision = revision;
		
		switch(revision) {
		case 0: {
			this.sendMessage = sendRev0Message;
			this.receiveMessage = receiveRev0Message;
			break;
		}
		case 35: {
			this.sendMessage = sendRev35Message;
			this.receiveMessage = receiveRev35Message;
			break;
		}
		default: {
			Log.debug(this, "revision %d is not supported, using revision 0", revision);
			this.revision = 0;
		}
		}
		
		Log.debug(this, "using revision %d, application connection %sready", this.revision, (revision == this.revision) ? "" : "not ");
		
		sendRevisionMessage(this.revision);
		
	}
	
	public void onReceiveDataMessage(int slot, Data addressData, Data data) {
		
		Log.debug(this, "onReceiveDataMessage(%d, %s, %s)", slot, addressData, data);
		
		NetworkSlot networkSlot = networkSlotMap.get(slot);
		
		if(networkSlot == null) {
			Log.warning(this, "received data message for empty network slot %d", slot);
			return;
		}
		
		sendNetworkPacket(networkSlot, addressData, data);
		
	}
	
	public void onReceiveGenerateKeyMessage() {
		Log.debug(this, "onReceiveGenerateKeyMessage");
		generateKeyPair();
	}
	
	public void onReceiveJoinNetworkMessage(NetworkType networkType) {
		Log.debug(this, "onReceiveJoinNetworkMessage(%s)", networkType);
		joinNetwork(networkType);
	}
	
	public void onReceiveSlotAssignMessage(int slot, NetworkType networkType, Data addressData) {
		// TODO illegal
	}
	
	@Override
	public void onReceiveAddressPublicKeyMessage(AbsKeyComponentI absKeyComponentI) {
		Log.debug(this, "onReceiveAddressPublicKeyMessage(%s)", absKeyComponentI);
		try {
			setApplicationAddressKeyPair(KeyPair.fromPublicKey(absKeyComponentI.getKey()));
		} catch (CryptoException e) {
			Log.exception(this, e);
		}
	}

	@Override
	public void onReceiveJoinDefaultNetworksMessage() {
		Log.debug(this, "onReceiveJoinDefaultNetworksMessage()");
		for(NetworkType networkType : DCL.DEFAULT_NETWORK_TYPES) {
			joinNetwork(networkType);
		}
	}
	
	//
	
	@Override
	public String toString() {
		return String.format("ApplicationConnection %s:%d", socket.getInetAddress().toString(), socket.getPort());
	}

	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		return parentHierarchicalLevel;
	}
	
}
