package org.dclayer.net.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.DataByteBuf;

/**
 * a UDP Server that provides sending and callback on receive.
 */
public class UDPSocket extends Thread implements HierarchicalLevel {
	
	private HierarchicalLevel parentHierarchicalLevel;
	
	/**
	 * the {@link DatagramSocket} to listen on
	 */
	private DatagramSocket socket;
	/**
	 * the {@link OnReceiveListener} to call upon receipt
	 */
	private OnReceiveListener onReceiveListener;
	
	public UDPSocket(HierarchicalLevel parentHierarchicalLevel, int port, OnReceiveListener onReceiveListener) throws SocketException {
		this.parentHierarchicalLevel = parentHierarchicalLevel;
		this.socket = new DatagramSocket(port);
		this.onReceiveListener = onReceiveListener;
		this.start();
	}
	
	@Override
	public void run() {
		
		Data data = new Data(0xFFFF);
		DataByteBuf dataByteBuf = new DataByteBuf(data);
		byte[] buf = data.getData();
		
		DatagramPacket p;
		
		for(;;) {
			
			p = new DatagramPacket(buf, buf.length);
			
			try {
				this.socket.receive(p);
			} catch (IOException e) {
				Log.exception(Log.PART_NET_UDPSOCKET, this, e);
				continue;
			}
			
			Log.debug(this, "received %d bytes from %s", p.getLength(), p.getSocketAddress().toString());
			
			dataByteBuf.reset(0, p.getLength());
			
			InetSocketAddress inetSocketAddress;
			try {
				inetSocketAddress = (InetSocketAddress) p.getSocketAddress();
			} catch(ClassCastException e) {
				Log.exception(this, e);
				return;
			}
			
			this.onReceiveListener.onReceiveS2S(inetSocketAddress, dataByteBuf, data);
			
		}
	}
	
	/**
	 * send the given {@link Data} to the given {@link SocketAddress}
	 * @param socketAddress the {@link SocketAddress} to send the data
	 * @param data the {@link Data} to send
	 * @throws IOException if sending fails
	 */
	public void send(SocketAddress socketAddress, Data data) throws IOException {
		DatagramPacket p = new DatagramPacket(data.getData(), data.offset(), data.length(), socketAddress);
		this.socket.send(p);
		Log.debug(this, "sent %d bytes to %s", p.getLength(), p.getSocketAddress());
	}
	
	/**
	 * send the given {@link Data} to the given {@link InetAddress} and port
	 * @param inetAddress the {@link InetAddress} to send the data to
	 * @param port the port to send the data to
	 * @param data the {@link Data} to send
	 * @throws IOException if sending fails
	 */
	public void send(InetAddress inetAddress, int port, Data data) throws IOException {
		DatagramPacket p = new DatagramPacket(data.getData(), data.offset(), data.length(), inetAddress, port);
		this.socket.send(p);
		Log.debug(this, "sent %d bytes to %s:%d", p.getLength(), p.getAddress(), p.getPort());
	}

	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		return parentHierarchicalLevel;
	}
	
	@Override
	public String toString() {
		return "UDPSocket";
	}
	
}
