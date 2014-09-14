package org.dclayer.net.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.meta.Log;

/**
 * A TCP Server that provides sending and callback on receive.
 */
public class TCPSocket extends Thread {
	/**
	 * the {@link ServerSocket} to listen on
	 */
	private ServerSocket serverSocket;
	/**
	 * the {@link OnReceiveListener} to call by the {@link TCPSocketConnection} upon receipt
	 */
	private OnReceiveListener onReceiveListener;
	
	public TCPSocket(int port, OnReceiveListener onReceiveListener) throws IOException {
		this.serverSocket = new ServerSocket();
		this.serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
		this.onReceiveListener = onReceiveListener;
		this.start();
	}
	
	@Override
	public void run() {
		for(;;) {
			Socket connectionSocket;
			try {
				connectionSocket = serverSocket.accept();
			} catch (IOException e) {
				Log.exception(Log.PART_NET_TCPSOCKET, this, e);
				continue;
			}
			Log.debug(Log.PART_NET_TCPSOCKET, this, String.format("new connection from %s:%d", connectionSocket.getInetAddress().toString(), connectionSocket.getPort()));
			new TCPSocketConnection(connectionSocket, onReceiveListener);
		}
	}
}
