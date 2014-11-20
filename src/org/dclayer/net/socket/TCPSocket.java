package org.dclayer.net.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.dclayer.meta.Log;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.ApplicationConnectionActionListener;

/**
 * A TCP Server that provides sending and callback on receive.
 */
public class TCPSocket extends Thread {
	/**
	 * the {@link ServerSocket} to listen on
	 */
	private ServerSocket serverSocket;
	private ApplicationConnectionActionListener applicationConnectionActionListener;
	
	public TCPSocket(int port, ApplicationConnectionActionListener applicationConnectionActionListener) throws IOException {
		this.serverSocket = new ServerSocket();
		this.serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
		this.applicationConnectionActionListener = applicationConnectionActionListener;
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
			
			ApplicationConnection applicationConnection = applicationConnectionActionListener.onApplicationConnection(connectionSocket);
			
		}
	}
}
