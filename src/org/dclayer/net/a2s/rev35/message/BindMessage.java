package org.dclayer.net.a2s.rev35.message;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.a2s.rev35.Message;
import org.dclayer.net.a2s.rev35.Rev35Message;
import org.dclayer.net.a2s.rev35.component.ApplicationIdentifierComponent;

/**
 * a bind message of revision 35 of the application-to-service protocol
 */
public class BindMessage extends Rev35Message {
	/**
	 * the contained {@link ApplicationIdentifierComponent} containing the {@link ApplicationIdentifier} of this {@link BindMessage}
	 */
	private ApplicationIdentifierComponent applicationIdentifierComponent;
	
	/**
	 * constructor called when this {@link PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public BindMessage(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
	
	/**
	 * constructor called when this {@link PacketComponent} is newly created rather than reconstructed from data
	 */
	public BindMessage(ApplicationIdentifierComponent applicationIdentifierComponent) {
		this.applicationIdentifierComponent = applicationIdentifierComponent;
	}
	
	/**
	 * returns the {@link ApplicationIdentifierComponent} contained in this {@link BindMessage}
	 * @return the {@link ApplicationIdentifierComponent} contained in this {@link BindMessage}
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
		return "BindMessage";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { applicationIdentifierComponent };
	}

	@Override
	public byte getType() {
		return Message.BIND;
	}
	
}
