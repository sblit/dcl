package org.dclayer.net.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.meta.Log;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.StreamByteBuf;

/**
 * a connection to a TCP endpoint that is handled in an own Thread
 */
public class TCPSocketConnection extends Thread {
	/**
	 * the {@link Socket} of this connection
	 */
	private Socket connectionSocket;
	/**
	 * the {@link OnReceiveListener} to call upon receipt
	 */
	private OnReceiveListener onReceiveListener;
	/**
	 * the {@link InetAddress} of the connected endpoint
	 */
	private InetAddress inetAddress;
	/**
	 * the port of the connected endpoint
	 */
	private int port;
	/**
	 * a human-readable address representation
	 */
	private String addrInfo;
	/**
	 * the {@link StreamByteBuf} used to read and write from/to this connection
	 */
	private StreamByteBuf streamByteBuf;
	
	/**
	 * the corresponding {@link ApplicationConnection}
	 */
	private ApplicationConnection applicationConnection;
	
	public TCPSocketConnection(Socket connectionSocket, OnReceiveListener onReceiveListener) {
		this.connectionSocket = connectionSocket;
		this.onReceiveListener = onReceiveListener;
		
		this.inetAddress = connectionSocket.getInetAddress();
		this.port = connectionSocket.getPort();
		this.addrInfo = String.format("%s:%d", inetAddress.toString(), port);
		
		InputStream inputStream;
		OutputStream outputStream;
		
		try {
			inputStream = this.connectionSocket.getInputStream();
		} catch (IOException e) {
			Log.error(Log.PART_NET_TCPSOCKETCONNECTION, this, String.format("can not process connection from %s due to IOException in opening InputStream", addrInfo));
			Log.exception(Log.PART_NET_TCPSOCKETCONNECTION, this, e);
			return;
		}
		
		try {
			outputStream = this.connectionSocket.getOutputStream();
		} catch (IOException e) {
			Log.error(Log.PART_NET_TCPSOCKETCONNECTION, this, String.format("can not process connection from %s due to IOException in opening OutputStream", addrInfo));
			Log.exception(Log.PART_NET_TCPSOCKETCONNECTION, this, e);
			return;
		}
		
		this.streamByteBuf = new StreamByteBuf(inputStream, outputStream);
		
		this.start();
	}
	
	@Override
	public void run() {
		for(;;) {
			Log.debug(Log.PART_NET_TCPSOCKETCONNECTION, this, String.format("receiving from %s ...", addrInfo));
			if(!this.onReceiveListener.onReceiveA2S(this, streamByteBuf)) {
				close();
				return;
			}
		}
	}
	
	/**
	 * closes this connection
	 */
	public void close() {
		try {
			connectionSocket.close();
		} catch (IOException e) {
			Log.error(Log.PART_NET_TCPSOCKETCONNECTION, this, "could not close connection");
			Log.exception(Log.PART_NET_TCPSOCKETCONNECTION, this, e);
		}
	}
	
	/**
	 * @return the ByteBuf used to read and write from/to this connection
	 */
	public ByteBuf getByteBuf() {
		return streamByteBuf;
	}
	
	/**
	 * @return the {@link InetAddress} of the connected endpoint
	 */
	public InetAddress getInetAddress() {
		return inetAddress;
	}
	
	/**
	 * @return the port of the connected endpoint
	 */
	public int getPort() {
		return port;
	}

	/**
	 * sets the corresponding {@link ApplicationConnection}
	 * @param applicationConnection the corresponding {@link ApplicationConnection} to set
	 */
	public void setApplicationConnection(ApplicationConnection applicationConnection) {
		this.applicationConnection = applicationConnection;
	}

	/**
	 * @return the corresponding {@link ApplicationConnection}
	 */
	public ApplicationConnection getApplicationConnection() {
		return applicationConnection;
	}
}