package org.dclayer.net.link.bmcp.crypto.component;


import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.link.bmcp.crypto.TransparentByteBufGenerator;

/**
 * a {@link PacketComponent} containing data for the rot crypto method
 */
public class RotCryptoDataComponent extends CryptoDataComponent {
	
	/**
	 * the value of rotation
	 */
	private byte rotateBy;
	
	/**
	 * creates an empty {@link RotCryptoDataComponent} that needs to be read into before it can be written from
	 */
	public RotCryptoDataComponent() {
		this((byte)0);
	}
	
	/**
	 * creates a {@link RotCryptoDataComponent} using the supplied value of rotation
	 * @param rotateBy the value of rotation
	 */
	public RotCryptoDataComponent(byte rotateBy) {
		this.rotateBy = rotateBy;
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		rotateBy = byteBuf.read();
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		byteBuf.write(rotateBy);
	}

	@Override
	public int length() {
		return 1;
	}

	@Override
	public String toString() {
		return String.format("RotCryptoDataComponent(rotateBy=%d)", rotateBy);
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	@Override
	public TransparentByteBufGenerator makeTransparentByteBufGenerator() {

		return new TransparentByteBufGenerator() {
			
			private final byte rotateBy = RotCryptoDataComponent.this.rotateBy;

			@Override
			public TransparentByteBuf makeLinkPacketHeaderTransparentByteBuf() {
				return new TransparentByteBuf() {
					@Override
					public byte translateRead(byte b) {
						return (byte)(b - rotateBy);
					}

					@Override
					public byte translateWrite(byte b) {
						return (byte)(b + rotateBy);
					}
				};
			}

			@Override
			public TransparentByteBuf makeLinkPacketBodyTransparentByteBuf() {
				return new TransparentByteBuf() {
					private byte lastByte = rotateBy;

					@Override
					public byte translateRead(byte c) {
						byte p = (byte)(c + lastByte);
						lastByte = p;
						return p;
					}

					@Override
					public byte translateWrite(byte p) {
						byte c = (byte)(p - lastByte);
						lastByte = p;
						return c;
					}
				};
			}
	
		};
		
	}
	
	@Override
	public String getCryptoMethodIdentifier() {
		return CryptoDataComponent.CRYPTO_METHOD_ROT;
	}

	@Override
	public void generateRandomParameters() {
		this.rotateBy = (byte)(Math.random() * 256);
	}
	
}
