package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.link.bmcp.crypto.component.CryptoDataComponent;
import org.dclayer.net.link.bmcp.crypto.component.NoCryptoDataComponent;
import org.dclayer.net.link.bmcp.crypto.component.RSACryptoDataComponent;
import org.dclayer.net.link.bmcp.crypto.component.RotCryptoDataComponent;

/**
 * a part of different BMCP commands, describing a cryptographic method and its parameters
 */
public class BMCPCryptoCommandComponent extends PacketComponent {
	
	/**
	 * local instance of {@link NoCryptoDataComponent}
	 */
	private NoCryptoDataComponent noCryptoDataComponent = new NoCryptoDataComponent();
	/**
	 * local instance of {@link RotCryptoDataComponent}
	 */
	private RotCryptoDataComponent rotCryptoDataComponent = new RotCryptoDataComponent();
	/**
	 * local instance of {@link RSACryptoDataComponent}
	 */
	private RSACryptoDataComponent rsaCryptoDataComponent = new RSACryptoDataComponent();
	
	/**
	 * a String containing the crypto method identifier
	 */
	private String cryptoMethodIdentifier;
	/**
	 * the current {@link CryptoDataComponent}
	 */
	private CryptoDataComponent cryptoDataComponent = noCryptoDataComponent;

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		cryptoMethodIdentifier = byteBuf.readString();
		switch(cryptoMethodIdentifier) {
		case CryptoDataComponent.CRYPTO_METHOD_NONE: {
			setNoCryptoDataComponent();
			break;
		}
		case CryptoDataComponent.CRYPTO_METHOD_ROT: {
			setRotCryptoDataComponent();
			break;
		}
		case CryptoDataComponent.CRYPTO_METHOD_RSA: {
			setRsaCryptoDataComponent();
			break;
		}
		default: {
			cryptoDataComponent = null;
			throw new ParseException(String.format("unsupported crypto method: %s", cryptoMethodIdentifier));
		}
		}
		cryptoDataComponent.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.writeString(cryptoMethodIdentifier);
		cryptoDataComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return cryptoMethodIdentifier.length() + 1 + cryptoDataComponent.length();
	}
	
	@Override
	public String toString() {
		return String.format("BMCPCryptoCommandComponent(cryptoMethod=%s, cryptoDataComponent=%s)",
				cryptoMethodIdentifier, cryptoDataComponent != null ? cryptoDataComponent.getClass().getName() : "null");
	}
	
	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { cryptoDataComponent };
	}
	
	/**
	 * sets the local {@link NoCryptoDataComponent} instance to be used as {@link CryptoDataComponent}
	 * @return the {@link NoCryptoDataComponent} instance to be used as {@link CryptoDataComponent}
	 */
	public NoCryptoDataComponent setNoCryptoDataComponent() {
		this.cryptoDataComponent = noCryptoDataComponent;
		this.cryptoMethodIdentifier = cryptoDataComponent.getCryptoMethodIdentifier();
		return noCryptoDataComponent;
	}
	
	/**
	 * sets the local {@link RotCryptoDataComponent} instance to be used as {@link CryptoDataComponent}
	 * @return the {@link RotCryptoDataComponent} instance to be used as {@link CryptoDataComponent}
	 */
	public RotCryptoDataComponent setRotCryptoDataComponent() {
		this.cryptoDataComponent = rotCryptoDataComponent;
		this.cryptoMethodIdentifier = cryptoDataComponent.getCryptoMethodIdentifier();
		return rotCryptoDataComponent;
	}
	
	/**
	 * sets the local {@link RSACryptoDataComponent} instance to be used as {@link CryptoDataComponent}
	 * @return the {@link RSACryptoDataComponent} instance to be used as {@link CryptoDataComponent}
	 */
	public RSACryptoDataComponent setRsaCryptoDataComponent() {
		this.cryptoDataComponent = rsaCryptoDataComponent;
		this.cryptoMethodIdentifier = cryptoDataComponent.getCryptoMethodIdentifier();
		return rsaCryptoDataComponent;
	}
	
	/**
	 * returns the current {@link CryptoDataComponent}
	 * @return the current {@link CryptoDataComponent}
	 */
	public CryptoDataComponent getCryptoDataComponent() {
		return cryptoDataComponent;
	}
	
}
