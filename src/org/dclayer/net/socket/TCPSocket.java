package org.dclayer.net.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.a2s.ApplicationConnectionActionListener;

/**
 * A TCP Server that provides sending and callback on receive.
 */
public class TCPSocket extends Thread implements StreamSocket, HierarchicalLevel {
	/**
	 * the {@link ServerSocket} to listen on
	 */
	private ServerSocket serverSocket;
	private ApplicationConnectionActionListener applicationConnectionActionListener;
	private HierarchicalLevel parentHierarchicalLevel;
	
	public TCPSocket(int port) throws IOException {
		this.serverSocket = new ServerSocket();
		this.serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
		this.start();
	}
	
	@Override
	public void run() {
		for(;;) {
			
			Socket connectionSocket;
			try {
				connectionSocket = serverSocket.accept();
			} catch (IOException e) {
				Log.exception(this, e);
				continue;
			}
			
			Log.debug(this, "new connection from %s:%d", connectionSocket.getInetAddress().toString(), connectionSocket.getPort());
			
			if(applicationConnectionActionListener == null) {
				
				Log.warning(this, "ignoring connection from %s, service not ready yet (closing connection)", connectionSocket.getInetAddress().toString());
				try {
					connectionSocket.close();
				} catch (IOException e) {
					Log.exception(this, e);
				}
				
			} else {
				applicationConnectionActionListener.onApplicationConnection(connectionSocket);
			}
			
		}
	}

	@Override
	public void setApplicationConnectionActionListener(ApplicationConnectionActionListener applicationConnectionActionListener) {
		this.applicationConnectionActionListener = applicationConnectionActionListener;
	}

	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		return parentHierarchicalLevel;
	}
	
	public void setParentHierarchicalLevel(HierarchicalLevel parentHierarchicalLevel) {
		this.parentHierarchicalLevel = parentHierarchicalLevel;
	}
	
}
