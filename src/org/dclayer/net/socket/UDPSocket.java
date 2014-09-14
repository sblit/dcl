package org.dclayer.net.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import org.dclayer.listener.net.OnReceiveListener;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.DataByteBuf;

/**
 * a UDP Server that provides sending and callback on receive.
 */
public class UDPSocket extends Thread {
	/**
	 * the {@link DatagramSocket} to listen on
	 */
	private DatagramSocket socket;
	/**
	 * the {@link OnReceiveListener} to call upon receipt
	 */
	private OnReceiveListener onReceiveListener;
	
	public UDPSocket(int port, OnReceiveListener onReceiveListener) throws SocketException {
		this.socket = new DatagramSocket(port);
		this.onReceiveListener = onReceiveListener;
		this.start();
	}
	
	@Override
	public void run() {
		DataByteBuf dataByteBuf = new DataByteBuf(0xFFFF);
		byte[] buf = dataByteBuf.getData().getData();
		DatagramPacket p;
		for(;;) {
			p = new DatagramPacket(buf, buf.length);
			
			try {
				this.socket.receive(p);
			} catch (IOException e) {
				Log.exception(Log.PART_NET_UDPSOCKET, this, e);
				continue;
			}
			
			Log.debug(Log.PART_NET_UDPSOCKET, this, String.format("received %d bytes from %s", p.getLength(), p.getSocketAddress().toString()));
			
			dataByteBuf.reset(0, p.getLength());
			
			InetSocketAddress inetSocketAddress = (InetSocketAddress) p.getSocketAddress();
			this.onReceiveListener.onReceiveS2S(inetSocketAddress.getAddress(), inetSocketAddress.getPort(), dataByteBuf);
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
		Log.debug(Log.PART_NET_UDPSOCKET, this, String.format("sent %d bytes to %s", p.getLength(), p.getSocketAddress()));
	}
}
