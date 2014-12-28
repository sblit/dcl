package org.dclayer.application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.dclayer.application.exception.ConnectionException;
import org.dclayer.application.exception.RevisionNegotiationConnectionException;
import org.dclayer.application.networktypeslotmap.NetworkEndpointSlot;
import org.dclayer.application.networktypeslotmap.NetworkEndpointSlotMap;
import org.dclayer.crypto.key.Key;
import org.dclayer.crypto.key.KeyPair;
import org.dclayer.crypto.key.RSAKey;
import org.dclayer.exception.crypto.InvalidCipherCryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.a2s.A2SMessage;
import org.dclayer.net.a2s.A2SMessageReceiver;
import org.dclayer.net.a2s.message.ApplicationChannelRequestMessageI;
import org.dclayer.net.a2s.message.DataMessageI;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.address.Address;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.AbsKeyComponent;
import org.dclayer.net.componentinterface.AbsKeyComponentI;
import org.dclayer.net.network.NetworkType;

public class ApplicationInstance extends Thread implements A2SMessageReceiver {
	
	public static final int REVISION = 0;
	
	//
	
	private StreamByteBuf streamByteBuf;
	
	private A2SMessage receiveMessage = new Rev0Message();
	private A2SMessage sendMessage = new Rev0Message();
	
	private boolean receivedRevision = false;
	private int sentRevision;
	private int revision = REVISION;
	
	private NetworkEndpointSlotMap networkEndpointSlotMap = new NetworkEndpointSlotMap();
	
	private Address<RSAKey> address;
	
	private OnReceiveListener defaultNetworksOnReceiveListener;

	public ApplicationInstance(
			InetAddress inetAddress,
			int port,
			KeyPair<RSAKey> addressKeyPair,
			OnReceiveListener defaultNetworksOnReceiveListener) throws ConnectionException {
		
		this.address = new Address<>(addressKeyPair);
		
		this.defaultNetworksOnReceiveListener = defaultNetworksOnReceiveListener;
		
		try {
			
			Socket socket = new Socket(inetAddress, port);
			this.streamByteBuf = new StreamByteBuf(socket.getInputStream(), socket.getOutputStream());
			
		} catch(IOException e) {
			throw new ConnectionException(e);
		}
		
		try {
			
			this.sentRevision = REVISION;
			
			for(;;) {
				
				this.receivedRevision = false;
				
				sendRevisionMessage(sentRevision);
				
				receiveMessage().callOnReceiveMethod(this);
				
				if(!this.receivedRevision) {
					throw new RevisionNegotiationConnectionException();
				}
				
				if(this.revision == this.sentRevision) {
					break;
				}
				
				switch(this.revision) {
				case 0: {
					this.sentRevision = this.revision;
					break;
				}
				default: {
					throw new RevisionNegotiationConnectionException();
				}
				}
				
			}
			
		} catch (BufException e) {
			throw new ConnectionException(e);
		} catch (ParseException e) {
			throw new ConnectionException(e);
		}
		
		this.start();
		
		if(addressKeyPair != null) {
			sendAddressPublicKeyMessage(addressKeyPair.getPublicKey());
			
			if(defaultNetworksOnReceiveListener != null) {
				sendJoinDefaultNetworksMessage();
			}
		}
		
	}
	
	//
	
	@Override
	public void run() {
		for(;;) {
			
			A2SMessage a2sMessage;
			
			try {
				a2sMessage = receiveMessage();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (BufException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
//			System.out.println(a2sMessage.represent(true));
			
			a2sMessage.callOnReceiveMethod(this);
			
		}
	}
	
	//
	
	private void send() {
		try {
			sendMessage();
		} catch (BufException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//
	
	private void sendMessage() throws BufException {
		sendMessage.write(streamByteBuf);
	}
	
	private A2SMessage receiveMessage() throws ParseException, BufException {
		receiveMessage.read(streamByteBuf);
		return receiveMessage;
	}
	
	//
	
	private synchronized void sendRevisionMessage(int revision) throws BufException {
		sendMessage.setRevisionMessage().setRevision(revision);
		sendMessage();
	}
	
	private synchronized void sendAddressPublicKeyMessage(RSAKey publicKey) {
		sendMessage.setAddressPublicKeyMessage().getKeyComponent().setRSAKeyComponent().setKey(publicKey);
		send();
	}
	
	private synchronized void sendJoinDefaultNetworksMessage() {
		sendMessage.setJoinDefaultNetworksMessage();
		send();
	}
	
	private synchronized void sendDataMessage(NetworkEndpointSlot networkEndpointSlot, Data destinationAddressData, Data data) {
		DataMessageI dataMessageI = sendMessage.setDataMessage();
		dataMessageI.getSlotNumComponent().setNum(networkEndpointSlot.getSlot());
		dataMessageI.getAddressComponent().setAddressData(destinationAddressData);
		dataMessageI.getDataComponent().setData(data);
		send();
	}
	
	private synchronized void sendKeyCryptoResponseDataMessage(Data responseData) {
		sendMessage.setKeyCryptoResponseDataMessage().getResponseDataComponent().setData(responseData);
		send();
	}
	
	private synchronized void sendApplicationChannelRequestMessage(int networkSlotId, int channelSlotId, Key remotePublicKey) {
		ApplicationChannelRequestMessageI applicationChannelRequestMessage = sendMessage.setApplicationChannelRequestMessage();
		applicationChannelRequestMessage.setNetworkSlot(networkSlotId);
		applicationChannelRequestMessage.setChannelSlot(channelSlotId);
		applicationChannelRequestMessage.getKeyComponent().setKey(remotePublicKey);
		send();
	}
	
	//
	
	public void send(NetworkEndpointSlot networkEndpointSlot, Data destinationAddressData, Data data) {
		sendDataMessage(networkEndpointSlot, destinationAddressData, data);
	}
	
	public void requestApplicationChannel(NetworkEndpointSlot networkEndpointSlot, Key remotePublicKey) {
		sendApplicationChannelRequestMessage(networkEndpointSlot.getSlot(), 0 /* TODO */, remotePublicKey);
		// TODO
	}
	
	//

	@Override
	public void onReceiveRevisionMessage(int revision) {
		this.receivedRevision = true;
		this.revision = revision;
	}

	@Override
	public void onReceiveDataMessage(int slot, Data addressData, Data data) {
		NetworkEndpointSlot networkEndpointSlot = networkEndpointSlotMap.get(slot);
		NetworkEndpoint networkEndpoint = networkEndpointSlot.getNetworkEndpoint();
		networkEndpoint.getOnReceiveListener().onReceive(networkEndpointSlot, data, addressData);
	}

	@Override
	public void onReceiveGenerateKeyMessage() {
		// TODO illegal
	}

	@Override
	public void onReceiveJoinNetworkMessage(NetworkType networkType) {
		// TODO illegal
	}

	@Override
	public void onReceiveSlotAssignMessage(int slot, NetworkType networkType, Data addressData) {
		NetworkEndpoint networkEndpoint = new NetworkEndpoint(networkType, defaultNetworksOnReceiveListener);
		NetworkEndpointSlot networkEndpointSlot = networkEndpointSlotMap.put(slot, networkEndpoint);
		networkEndpoint.getOnReceiveListener().onJoin(networkEndpointSlot, addressData);
	}

	@Override
	public void onReceiveAddressPublicKeyMessage(AbsKeyComponentI absKeyComponentI) {
		// TODO illegal
	}

	@Override
	public void onReceiveJoinDefaultNetworksMessage() {
		// TODO illegal
	}

	@Override
	public void onReceiveKeyEncryptDataMessage(Data plainData) {
		Data cipherData;
		try {
			cipherData = this.address.getKeyPair().getPrivateKey().encrypt(plainData);
		} catch (InvalidCipherCryptoException e) {
			// TODO
			cipherData = null;
		}
		sendKeyCryptoResponseDataMessage(cipherData);
	}

	@Override
	public void onReceiveKeyDecryptDataMessage(Data cipherData) {
		Data plainData;
		try {
			plainData = this.address.getKeyPair().getPrivateKey().decrypt(cipherData);
		} catch (InvalidCipherCryptoException e) {
			// TODO
			plainData = null;
		}
		sendKeyCryptoResponseDataMessage(plainData);
	}

	@Override
	public void onReceiveKeyCryptoResponseDataMessage(Data responseData) {
		// TODO illegal
	}

	@Override
	public void onReceiveApplicationChannelRequestMessage(int networkSlotId, int channelSlotId, AbsKeyComponent keyComponent) {
		// TODO illegal
	}
	
	//
	
}
