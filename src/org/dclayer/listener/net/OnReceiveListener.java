package org.dclayer.listener.net;

import java.net.InetSocketAddress;

import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.socket.TCPSocketConnection;

/**
 * used to execute callbacks upon data receipt
 */
public interface OnReceiveListener {
	/**
	 * Service-to-Service receive callback, called by UDPSocket
	 * @param inetSocketAddress the InetSocketAddress this was received from
	 * @param byteBuf ByteBuf holding the received data
	 */
	public void onReceiveS2S(InetSocketAddress inetSocketAddress, DataByteBuf byteBuf);
	/**
	 * Application-to-Service receive callback, called by TCPSocket
	 * @param tcpSocketConnection the TCPSocketConnection this was received from
	 * @param byteBuf ByteBuf holding the received data
	 * @return true if the received data was successfully processed, false if an error occured
	 */
	public boolean onReceiveA2S(TCPSocketConnection tcpSocketConnection, ByteBuf byteBuf);
}
