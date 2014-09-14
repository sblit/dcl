package org.dclayer.net.link.bmcp.crypto.component;


import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.buf.TransparentByteBuf;
import org.dclayer.net.link.bmcp.crypto.TransparentByteBufGenerator;

/**
 * a {@link PacketComponent} containing data for the "no" crypto method (i.e. plaintext transmission)
 */
public class NoCryptoDataComponent extends CryptoDataComponent {
	
	/**
	 * creates an empty {@link NoCryptoDataComponent}
	 */
	public NoCryptoDataComponent() {
		
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		
	}

	@Override
	public int length() {
		return 0;
	}

	@Override
	public String toString() {
		return "NoCryptoDataComponent";
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	@Override
	public TransparentByteBufGenerator makeTransparentByteBufGenerator() {
		
		return new TransparentByteBufGenerator() {
			
			@Override
			public TransparentByteBuf makeLinkPacketHeaderTransparentByteBuf() {
				return new TransparentByteBuf() {
					@Override
					public byte translateWrite(byte b) {
						return b;
					}
					
					@Override
					public byte translateRead(byte b) {
						return b;
					}
				};
			}
			
			@Override
			public TransparentByteBuf makeLinkPacketBodyTransparentByteBuf() {
				return new TransparentByteBuf() {
					@Override
					public byte translateWrite(byte b) {
						return b;
					}
					
					@Override
					public byte translateRead(byte b) {
						return b;
					}
				};
			}
			
		};
	
	}

	@Override
	public String getCryptoMethodIdentifier() {
		return CryptoDataComponent.CRYPTO_METHOD_NONE;
	}

	@Override
	public void generateRandomParameters() {
		
	}
}
