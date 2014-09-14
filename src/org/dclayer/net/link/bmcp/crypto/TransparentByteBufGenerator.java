package org.dclayer.net.link.bmcp.crypto;

import org.dclayer.net.buf.TransparentByteBuf;

/**
 * interface describing an object that is able to create {@link TransparentByteBuf}s for message encryption and decryption
 */
public interface TransparentByteBufGenerator {

	public abstract TransparentByteBuf makeLinkPacketHeaderTransparentByteBuf();
	public abstract TransparentByteBuf makeLinkPacketBodyTransparentByteBuf();
	
}
