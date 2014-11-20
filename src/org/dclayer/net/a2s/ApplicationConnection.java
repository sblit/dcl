package org.dclayer.net.a2s;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.dclayer.crypto.Crypto;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.a2s.rev35.message.DataMessage;
import org.dclayer.net.a2s.rev35.message.SlotAssignMessage;
import org.dclayer.net.address.Address;
import org.dclayer.net.address.AsymmetricKeyPairAddress;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.network.ApplicationNetworkInstance;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.NetworkNode;
import org.dclayer.net.network.NetworkInstanceCollection;
import org.dclayer.net.network.NetworkType;
import org.dclayer.net.network.component.NetworkPacket;
import org.dclayer.net.network.component.NetworkPayload;
import org.dclayer.net.network.properties.CommonNetworkPayloadProperties;
import org.dclayer.net.network.routing.Nexthops;
import org.dclayer.net.network.slot.NetworkSlot;
import org.dclayer.net.network.slot.NetworkSlotMap;

/**
 * a connection to an application instance
 */
public class ApplicationConnection extends Thread implements HierarchicalLevel {
	
	private Socket socket;
	
	private HierarchicalLevel parentHierarchicalLevel;
	private ApplicationConnectionActionListener applicationConnectionActionListener;
	
	private StreamByteBuf streamByteBuf;
	
	private A2SPacket receiveA2SPacket = new A2SPacket();
	private A2SPacket sendA2SPacket = new A2SPacket();
	
	private NetworkSlotMap networkSlotMap = new NetworkSlotMap();
	
	//
	
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
	
	private void generateKeyPair() {
		Log.msg(this, "generating rsa address key pair");
		this.applicationAddressKeyPair = Crypto.generateAddressRSAKeyPair();
		this.applicationAddress = new AsymmetricKeyPairAddress(applicationAddressKeyPair, new NetworkInstanceCollection());
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
		
		applicationConnectionActionListener.onNetworkInstance(applicationNetworkInstance);
		
		NetworkSlot networkSlot = networkSlotMap.add(applicationNetworkInstance);
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
				receiveA2SPacket.read(streamByteBuf);
			} catch (ParseException e) {
				Log.exception(this, e);
				close();
				return;
			} catch (BufException e) {
				Log.exception(this, e);
				close();
				return;
			}
			
			Log.debug(this, "received application to service packet: %s", receiveA2SPacket.represent(true));
			
			receiveA2SPacket.callOnReceiveMethod(this);
			
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
			sendA2SPacket.write(streamByteBuf);
		} catch (BufException e) {
			Log.exception(this, e, "error while sending application to service packet %s", sendA2SPacket.represent(true));
			return;
		}
	}
	
	//
	
	private void sendNetworkPacket(NetworkSlot networkSlot, Data addressData, Data data) {
		
		NetworkNode networkNode = networkSlot.getNetworkNode();

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
	
	private synchronized void sendDataMessage(int slot, NetworkPayload networkPayload) {
		DataMessage dataMessage = sendA2SPacket.setRevision35Message().setDataMessage();
		dataMessage.getSlotNumberComponent().setNumber(slot);
		dataMessage.getAddressComponent().setAddressData(networkPayload.getSourceAddressData()); // this may be null, doesn't matter though
		dataMessage.getDataComponent().setData(networkPayload.getPayloadData());
		send();
	}
	
	private synchronized void sendSlotAssignMessage(int slot, NetworkType networkType, Data addressData) {
		SlotAssignMessage slotAssignMessage = sendA2SPacket.setRevision35Message().setSlotAssignMessage();
		slotAssignMessage.setSlot(slot);
		slotAssignMessage.getNetworkTypeComponent().setNetworkType(networkType);
		slotAssignMessage.setAddressData(addressData);
		send();
	}
	
	//
	
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
