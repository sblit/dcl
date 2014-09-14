package org.dclayer.net.a2s.rev35.component;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.rev35.Rev35PacketComponent;
import org.dclayer.net.buf.ByteBuf;

/**
 * a {@link PacketComponent} containing an {@link ApplicationIdentifier}
 */
public class ApplicationIdentifierComponent extends Rev35PacketComponent {
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
		this.applicationIdentifier = new ApplicationIdentifier(byteBuf.readTextModeString());
		byteBuf.readTilSpaceOrEOL();
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.writeTextModeString(applicationIdentifier.getApplicationIdentifier());
		byteBuf.write((byte)' ');
	}

	@Override
	public int length() {
		return 2 + applicationIdentifier.getApplicationIdentifier().length();
	}

	@Override
	public String toString() {
		return String.format("ApplicationIdentifierComponent(applicationidentifier=%s)", applicationIdentifier.toString());
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	public ApplicationIdentifier getApplicationIdentifier() {
		return applicationIdentifier;
	}
}
