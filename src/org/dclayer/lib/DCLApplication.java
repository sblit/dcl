package org.dclayer.lib;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.dclayer.DCL;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.a2s.A2SPacket;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.BindMessage;
import org.dclayer.net.a2s.rev0.message.DataMessage;
import org.dclayer.net.a2s.rev0.message.UnbindMessage;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;
import org.dclayer.net.s2s.rev0.component.DataComponent;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;
import org.dclayer.net.serviceaddress.ServiceAddress;
import org.dclayer.net.serviceaddress.ServiceAddressIPv4;

/**
 * Library for {@link DCL}
 * 
 * @author Nikola Szucsich
 * 
 */
public class DCLApplication { 
	
	/**
	 * Socket for the DCL-connection
	 */
	private Socket toDCL;
	/**
	 * Unique application-identifier
	 */
	private ApplicationIdentifierComponent applicationIdentifierComponent;

	private StreamByteBuf out;
	private StreamByteBuf in;
	
	/**
	 * Initialises the communication between Application and DCL 
	 * 
	 * @param address
	 * Application to service {@link InetSocketAddress} of the running DCL-service
	 * 
	 * @param applicationIdentifier
	 * String, which uniquely identifies the application within the DCL-service
	 * 
	 * @throws IOException
	 * If setting up a new {@link Socket} fails
	 * 
	 * @throws BufException
	 *  thrown if an operation on the {@link ByteBuf} fails
	 */
	public DCLApplication(InetSocketAddress address, String applicationIdentifier) throws IOException, BufException {
		//Converts string into ApplicationIdentifierComponent
		ApplicationIdentifierComponent applicationIdentifierComponent = new ApplicationIdentifierComponent(new ApplicationIdentifier(applicationIdentifier));
		this.applicationIdentifierComponent = applicationIdentifierComponent;
		
		//Setup socket-based communication
		if(address.getHostName() != null)
			toDCL = new Socket(address.getHostName(), address.getPort());
		else
			toDCL = new Socket(address.getAddress(), address.getPort());
		out = new StreamByteBuf(toDCL.getOutputStream());
		in = new StreamByteBuf(toDCL.getInputStream());
		
		//Bind app to DCL service
		A2SPacket bindPacket = new A2SPacket(new Message(new BindMessage(applicationIdentifierComponent)));
		bindPacket.write(out);
		
		try {
			A2SPacket bindReply = new A2SPacket(in);
			System.out.println(bindReply.getMessage().getMessageTypeId());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Send data via DCL
	 * @param data
	 * The data to send
	 * @param remoteApplicationIdentifier
	 * The application identifier of the destination app
	 * @param remoteServiceAddress
	 * Service to Service {@link InetSocketAddress} of the DCL-service which runs the destination app
	 * 
	 * @throws BufException
	 * thrown if an operation on the {@link ByteBuf} fails
	 */
	public void send(byte[] data, String remoteApplicationIdentifier, InetSocketAddress remoteServiceAddress) throws BufException{
		
		//Preparing params for sending
		DataComponent dataComponent = new DataComponent(data);
		ServiceAddressComponent remoteServiceAddressComponent = new ServiceAddressComponent((ServiceAddress) new ServiceAddressIPv4(
				(Inet4Address)remoteServiceAddress.getAddress(), remoteServiceAddress.getPort()));
		
		ApplicationIdentifierComponent remoteApplicationIdentifierComponent = new ApplicationIdentifierComponent(new ApplicationIdentifier(remoteApplicationIdentifier));
		
		//Preparing packet
		A2SPacket packet = new A2SPacket(new Message(new DataMessage(remoteApplicationIdentifierComponent, applicationIdentifierComponent, remoteServiceAddressComponent, dataComponent)));
		
		//Sending Packet
		packet.write(out);
		
	}
	
	/**
	 * Waits for packet till it gets one
	 * @return
	 * byte[] containing the sent data
	 * @throws BufException
	 * thrown if an operation on the [{@link ByteBuf} fails
	 */
	public byte[] receive() throws  BufException{
		
		A2SPacket packet = null;
		try {
			packet = new A2SPacket(in);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataMessage message = (DataMessage)((Message)packet.getMessage()).getMessage();
		
		return message.getDataComponent().getData();
	}
	
	/**
	 * Closes the connection securely
	 * @throws IOException
	 * if an I/O error occurs when closing this socket.
	 */
	public void end() throws IOException{
		//Sends an unbind message to the DCL service 
		new A2SPacket(new Message(new UnbindMessage(applicationIdentifierComponent)));
		toDCL.close();
	}
	
}
