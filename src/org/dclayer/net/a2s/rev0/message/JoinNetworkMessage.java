package org.dclayer.net.a2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.A2SMessageReceiver;
import org.dclayer.net.a2s.A2SRevisionSpecificMessage;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.NetworkTypeComponent;

public class JoinNetworkMessage extends A2SRevisionSpecificMessage {
	
	private NetworkTypeComponent networkTypeComponent = new NetworkTypeComponent();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		networkTypeComponent.read(byteBuf);
	}
	
	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		networkTypeComponent.write(byteBuf);
	}
	
	@Override
	public int length() {
		return networkTypeComponent.length();
	}
	
	@Override
	public String toString() {
		return "JoinNetworkMessage";
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { networkTypeComponent };
	}
	
	@Override
	public byte getType() {
		return Rev0Message.JOIN_NETWORK;
	}
	
	public NetworkTypeComponent getNetworkTypeComponent() {
		return networkTypeComponent;
	}

	@Override
	public void callOnReceiveMethod(A2SMessageReceiver a2sMessageReceiver) {
		a2sMessageReceiver.onReceiveJoinNetworkMessage(networkTypeComponent.getNetworkType());
	}

	@Override
	public int getMessageRevision() {
		return 0;
	}
	
}
