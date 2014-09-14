package org.dclayer.net.s2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Message;
import org.dclayer.net.s2s.rev0.Rev0Message;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;
import org.dclayer.net.s2s.rev0.component.DataComponent;

/**
 * an application data message of revision 0 of the service-to-service protocol
 */
public class S2SApplicationDataMessage extends Rev0Message {
	/**
	 * the destination {@link ApplicationIdentifierComponent} contained in this {@link Rev0Message}
	 */
	private ApplicationIdentifierComponent destinationApplicationIdentifierComponent;
	/**
	 * the source {@link ApplicationIdentifierComponent} contained in this {@link Rev0Message}
	 */
	private ApplicationIdentifierComponent sourceApplicationIdentifierComponent;
	/**
	 * the {@link DataComponent} contained in this {@link Rev0Message}
	 */
	private DataComponent dataComponent;
	
	/**
	 * constructor called when this {@link Rev0Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public S2SApplicationDataMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev0Message} is newly created rather than reconstructed from data
	 */
	public S2SApplicationDataMessage(ApplicationIdentifierComponent destinationApplicationIdentifierComponent, ApplicationIdentifierComponent sourceApplicationIdentifierComponent, DataComponent dataComponent) {
		this.destinationApplicationIdentifierComponent = destinationApplicationIdentifierComponent;
		this.sourceApplicationIdentifierComponent = sourceApplicationIdentifierComponent;
		this.dataComponent = dataComponent;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		destinationApplicationIdentifierComponent = new ApplicationIdentifierComponent(byteBuf);
		sourceApplicationIdentifierComponent = new ApplicationIdentifierComponent(byteBuf);
		dataComponent = new DataComponent(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		destinationApplicationIdentifierComponent.write(byteBuf);
		sourceApplicationIdentifierComponent.write(byteBuf);
		dataComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return destinationApplicationIdentifierComponent.length()
				+ sourceApplicationIdentifierComponent.length()
				+ dataComponent.length();
	}

	@Override
	public String toString() {
		return "ApplicationDataMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] {
				destinationApplicationIdentifierComponent,
				sourceApplicationIdentifierComponent,
				dataComponent
			};
	}
	
	/**
	 * returns the {@link ApplicationIdentifierComponent} containing the destination {@link ApplicationIdentifier} of this {@link S2SApplicationDataMessage} 
	 * @return the {@link ApplicationIdentifierComponent} containing the destination {@link ApplicationIdentifier} of this {@link S2SApplicationDataMessage}
	 */
	public ApplicationIdentifierComponent getDestinationApplicationIdentifierComponent() {
		return destinationApplicationIdentifierComponent;
	}
	
	/**
	 * returns the {@link ApplicationIdentifierComponent} containing the source {@link ApplicationIdentifier} of this {@link S2SApplicationDataMessage} 
	 * @return the {@link ApplicationIdentifierComponent} containing the source {@link ApplicationIdentifier} of this {@link S2SApplicationDataMessage}
	 */
	public ApplicationIdentifierComponent getSourceApplicationIdentifierComponent() {
		return sourceApplicationIdentifierComponent;
	}
	
	/**
	 * returns the {@link DataComponent} of this {@link S2SApplicationDataMessage}
	 * @return the {@link DataComponent} of this {@link S2SApplicationDataMessage}
	 */
	public DataComponent getDataComponent() {
		return dataComponent;
	}

	@Override
	public byte getType() {
		return Message.APPLICATION_DATA;
	}
	
}
