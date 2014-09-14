package org.dclayer.net.a2s;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.socket.TCPSocketConnection;

/**
 * an {@link A2SPacket} received from a {@link TCPSocketConnection}
 */
public class ConnectedPacket extends A2SPacket {

	/**
	 * the {@link TCPSocketConnection} this {@link ConnectedPacket} was sent to or received from
	 */
	private TCPSocketConnection tcpSocketConnection;

	/**
	 * creates a new {@link ConnectedPacket} for the given {@link TCPSocketConnection} to read from the given {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} to reconstruct this {@link A2SPacket} from
	 * @param tcpSocketConnection the {@link TCPSocketConnection} this packet is sent to or received from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public ConnectedPacket(ByteBuf byteBuf, TCPSocketConnection tcpSocketConnection) throws ParseException, BufException {
		super(byteBuf);
		this.tcpSocketConnection = tcpSocketConnection;
	}
	
	/**
	 * creates a new {@link ConnectedPacket} for the given {@link TCPSocketConnection} containing the given {@link RevisionMessage}
	 * @param message the {@link RevisionMessage} this {@link A2SPacket} contains
	 * @param tcpSocketConnection the {@link TCPSocketConnection} this packet is sent to or received from
	 */
	public ConnectedPacket(RevisionMessage message, TCPSocketConnection tcpSocketConnection) {
		super(message);
		this.tcpSocketConnection = tcpSocketConnection;
	}

	/**
	 * returns the {@link TCPSocketConnection} this {@link A2SPacket} was received from or is sent to
	 * @return the {@link TCPSocketConnection} this {@link A2SPacket} was received from or is sent to
	 */
	public TCPSocketConnection getTCPSocketConnection() {
		return tcpSocketConnection;
	}

}
