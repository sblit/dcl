package org.dclayer.net.s2s.rev0.component;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.Rev0PacketComponent;

/**
 * a {@link PacketComponent} containing an {@link ApplicationIdentifier}
 */
public class ApplicationIdentifierComponent extends Rev0PacketComponent {
	/**
	 * the contained {@link ApplicationIdentifier}
	 */
	private ApplicationIdentifier applicationIdentifier;

	/**
	 * constructor called when this {@link PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
    public ApplicationIdentifierComponent(ByteBuf byteBuf) throws ParseException, BufException {
		super(byteBuf);
	}
    
    /**
	 * constructor called when this {@link PacketComponent} is newly created rather than reconstructed from data
	 */
    public ApplicationIdentifierComponent(ApplicationIdentifier applicationIdentifier) {
		this.applicationIdentifier = applicationIdentifier;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		this.applicationIdentifier = new ApplicationIdentifier(byteBuf.readString());
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.writeString(applicationIdentifier.getApplicationIdentifier());
	}

	@Override
	public int length() {
		return 1 + applicationIdentifier.getApplicationIdentifier().length();
	}

	@Override
	public String toString() {
		return String.format("ApplicationIdentifierComponent(applicationidentifier=%s)", applicationIdentifier.toString());
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	/**
	 * @return the contained {@link ApplicationIdentifier}
	 */
	public ApplicationIdentifier getApplicationIdentifier() {
		return applicationIdentifier;
	}
}
