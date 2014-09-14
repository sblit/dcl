package org.dclayer.net.a2s.rev0.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.Rev0Message;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;

/**
 * an unbind message of revision 0 of the application-to-service protocol
 */
public class UnbindMessage extends Rev0Message {
	/**
	 * the {@link ApplicationIdentifierComponent} containing the {@link ApplicationIdentifier} of this {@link BindMessage}
	 */
	private ApplicationIdentifierComponent applicationIdentifierComponent;
	
	/**
	 * constructor called when this {@link Rev0Message} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link Rev0Message} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public UnbindMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link Rev0Message} is newly created rather than reconstructed from data
	 */
	public UnbindMessage(ApplicationIdentifierComponent applicationIdentifierComponent) {
		this.applicationIdentifierComponent = applicationIdentifierComponent;
	}
	
	/**
	 * returns the {@link ApplicationIdentifierComponent} containing the {@link ApplicationIdentifier} of this {@link UnbindMessage} 
	 * @return the {@link ApplicationIdentifierComponent} containing the {@link ApplicationIdentifier} of this {@link UnbindMessage}
	 */
	public ApplicationIdentifierComponent getApplicationIdentifierComponent() {
		return applicationIdentifierComponent;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		applicationIdentifierComponent = new ApplicationIdentifierComponent(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		applicationIdentifierComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return applicationIdentifierComponent.length();
	}

	@Override
	public String toString() {
		return "UnbindMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { applicationIdentifierComponent };
	}

	@Override
	public byte getType() {
		return Message.UNBIND;
	}
	
}
