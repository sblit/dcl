package org.dclayer.net.link.bmcp.crypto.component;


import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.component.BigIntComponent;
import org.dclayer.net.link.bmcp.crypto.TransparentByteBufGenerator;

/**
 * a {@link PacketComponent} containing data for the RSA crypto method
 */
public class RSACryptoDataComponent extends CryptoDataComponent {
	/**
	 * the modulus
	 */
	private BigIntComponent modulusBigIntComponent;
	
	/**
	 * the exponent
	 */
	private BigIntComponent exponentBigIntComponent;
	
	/**
	 * creates an empty {@link RSACryptoDataComponent} that needs to be read into before it can be written from
	 */
	public RSACryptoDataComponent() {
		this(new BigIntComponent(), new BigIntComponent());
	}
	
	/**
	 * creates a {@link RSACryptoDataComponent} using the supplied {@link BigIntComponent}s
	 * @param modulusBigIntComponent the {@link BigIntComponent} representing the modulus
	 * @param exponentBigIntComponent the {@link BigIntComponent} representing the exponent
	 */
	public RSACryptoDataComponent(BigIntComponent modulusBigIntComponent, BigIntComponent exponentBigIntComponent) {
		this.modulusBigIntComponent = modulusBigIntComponent;
		this.exponentBigIntComponent = exponentBigIntComponent;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		modulusBigIntComponent.read(byteBuf);
		exponentBigIntComponent.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		modulusBigIntComponent.write(byteBuf);
		exponentBigIntComponent.write(byteBuf);
	}

	@Override
	public int length() {
		return modulusBigIntComponent.length() + exponentBigIntComponent.length();
	}

	@Override
	public String toString() {
		return "RSACryptoDataComponent";
	}

	@Override
	public PacketComponent[] getChildren() {
		return new PacketComponent[] { modulusBigIntComponent, exponentBigIntComponent };
	}
	
	@Override
	public TransparentByteBufGenerator makeTransparentByteBufGenerator() {
		
		return new TransparentByteBufGenerator() {

			@Override
			public TransparentByteBuf makeLinkPacketHeaderTransparentByteBuf() {
				return new TransparentByteBuf() {
					@Override
					public byte translateRead(byte b) {
						// TODO Auto-generated method stub
						return 0;
					}
		
					@Override
					public byte translateWrite(byte b) {
						// TODO Auto-generated method stub
						return 0;
					}
				};
			}
			
			@Override
			public TransparentByteBuf makeLinkPacketBodyTransparentByteBuf() {
				return new TransparentByteBuf() {
					@Override
					public byte translateRead(byte b) {
						// TODO Auto-generated method stub
						return 0;
					}
		
					@Override
					public byte translateWrite(byte b) {
						// TODO Auto-generated method stub
						return 0;
					}
				};
			}
		
		};
		
	}
	
	@Override
	public String getCryptoMethodIdentifier() {
		return CryptoDataComponent.CRYPTO_METHOD_RSA;
	}

	@Override
	public void generateRandomParameters() {
		// TODO generate keys
	}
	
}
