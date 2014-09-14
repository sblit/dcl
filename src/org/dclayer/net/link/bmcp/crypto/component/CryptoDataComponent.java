package org.dclayer.net.link.bmcp.crypto.component;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.link.bmcp.crypto.TransparentByteBufGenerator;

/**
 * base class for crypto data components holding information about a cryptographic method
 */
public abstract class CryptoDataComponent extends PacketComponent {
	
	public static final String CRYPTO_METHOD_NONE = "none";
	public static final String CRYPTO_METHOD_ROT = "nonsense.rot";
	public static final String CRYPTO_METHOD_RSA = "rsa";
	
	/**
	 * @return the identifier of the cryptographic method
	 */
	public abstract String getCryptoMethodIdentifier();
	/**
	 * @return a {@link TransparentByteBufGenerator} object that generates {@link TransparentByteBuf}s for this crypto method,
	 * en-/decrypting using the crypto information this {@link CryptoDataComponent} objects holds at the moment this
	 * ({@link CryptoDataComponent#makeTransparentByteBufGenerator()}) is executed.
	 */
	public abstract TransparentByteBufGenerator makeTransparentByteBufGenerator();
	
	/**
	 * generates new random parameters for the used cryptographic method.<br />
	 * this does not affect already created {@link TransparentByteBufGenerator}s.
	 */
	public abstract void generateRandomParameters();
	
}
