package org.dclayer.net.a2s.rev35.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.rev35.Message;
import org.dclayer.net.a2s.rev35.Rev35Message;
import org.dclayer.net.a2s.rev35.component.ApplicationIdentifierComponent;
import org.dclayer.net.a2s.rev35.component.DataComponent;
import org.dclayer.net.a2s.rev35.component.ServiceAddressComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.serviceaddress.ServiceAddress;

public class DataMessage extends Rev35Message {
	private ApplicationIdentifierComponent destinationApplicationIdentifierComponent, sourceApplicationIdentifierComponent;
	private ServiceAddressComponent serviceAddressComponent;
	private DataComponent dataComponent;
	
	/**
	 * constructor called when this {@link PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public DataMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link PacketComponent} is newly created rather than reconstructed from data
	 */
	public DataMessage(ApplicationIdentifierComponent destinationApplicationIdentifierComponent, ApplicationIdentifierComponent sourceApplicationIdentifierComponent, ServiceAddressComponent serviceAddressComponent, DataComponent dataComponent) {
		this.destinationApplicationIdentifierComponent = destinationApplicationIdentifierComponent;
		this.sourceApplicationIdentifierComponent = sourceApplicationIdentifierComponent;
		this.serviceAddressComponent = serviceAddressComponent;
		this.dataComponent = dataComponent;
		
	}
	
	/**
	 * returns the {@link ApplicationIdentifierComponent} containing the destination {@link ApplicationIdentifier} of this {@link DataMessage} 
	 * @return the {@link ApplicationIdentifierComponent} containing the destination {@link ApplicationIdentifier} of this {@link DataMessage}
	 */
	public ApplicationIdentifierComponent getDestinationApplicationIdentifierComponent() {
		return destinationApplicationIdentifierComponent;
	}
	
	/**
	 * returns the {@link ApplicationIdentifierComponent} containing the source {@link ApplicationIdentifier} of this {@link DataMessage} 
	 * @return the {@link ApplicationIdentifierComponent} containing the source {@link ApplicationIdentifier} of this {@link DataMessage}
	 */
	public ApplicationIdentifierComponent getSourceApplicationIdentifierComponent() {
		return sourceApplicationIdentifierComponent;
	}
	
	/**
	 * returns the {@link ServiceAddressComponent} containing the {@link ServiceAddress} of this {@link DataMessage}
	 * @return the {@link ServiceAddressComponent} containing the {@link ServiceAddress} of this {@link DataMessage}
	 */
	public ServiceAddressComponent getServiceAddressComponent() {
		return serviceAddressComponent;
	}
	
	/**
	 * returns the {@link DataComponent} containing the data of this {@link DataMessage}
	 * @return the {@link DataComponent} containing the data of this {@link DataMessage}
	 */
	public DataComponent getDataComponent() {
		return dataComponent;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		destinationApplicationIdentifierComponent = new ApplicationIdentifierComponent(byteBuf);
		sourceApplicationIdentifierComponent = new ApplicationIdentifierComponent(byteBuf);
		serviceAddressComponent = new ServiceAddressComponent(byteBuf);
		dataComponent = new DataComponent(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		destinationApplicationIdentifierComponent.write(byteBuf);
		sourceApplicationIdentifierComponent.write(byteBuf);
		serviceAddressComponent.write(byteBuf);
		dataComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return destinationApplicationIdentifierComponent.length()
				+ sourceApplicationIdentifierComponent.length()
				+ serviceAddressComponent.length()
				+ dataComponent.length();
	}

	@Override
	public String toString() {
		return "DataMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] {
				destinationApplicationIdentifierComponent,
				sourceApplicationIdentifierComponent,
				serviceAddressComponent,
				dataComponent
			};
	}

	@Override
	public byte getType() {
		return Message.DATA;
	}
	
}
