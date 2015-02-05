package org.dclayer.net.packetcomponent;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedMessageTypeException;
import org.dclayer.net.PacketComponentI;
import org.dclayer.net.buf.ByteBuf;

public abstract class SwitchPacketComponent<T extends PacketComponentI> extends AutoPacketComponent<T> {
	
	private static interface TypeComponent {
		
		public int readType(ByteBuf byteBuf) throws BufException, ParseException;
		public void writeType(ByteBuf byteBuf, int type) throws BufException;
		public int typeLength();
		
	}
	
	//

	private String name;
	
	private T activeChild;
	private int type;
	
	private final TypeComponent typeComponent;
	
	public SwitchPacketComponent(String name, Class<T> commonType) {
		
		super(commonType);
		
		this.name = name;
		this.typeComponent = makeTypeComponent(children.length);
		
	}
	
	public SwitchPacketComponent(Class<T> commonType) {
		this(null, commonType);
	}
	
	//
	
	private TypeComponent makeTypeComponent(int numChildren) {
		
		if(numChildren < 0x100) {
			
			return new TypeComponent() {
				@Override
				public void writeType(ByteBuf byteBuf, int type) throws BufException {
					byteBuf.write((byte) type);
				}
				
				@Override
				public int typeLength() {
					return 1;
				}
				
				@Override
				public int readType(ByteBuf byteBuf) throws BufException, ParseException {
					return byteBuf.read() & 0xFF;
				}
			};
			
		} else if(numChildren < 0x10000) {
			
			return new TypeComponent() {
				@Override
				public void writeType(ByteBuf byteBuf, int type) throws BufException {
					byteBuf.write16(type);
				}
				
				@Override
				public int typeLength() {
					return 2;
				}
				
				@Override
				public int readType(ByteBuf byteBuf) throws BufException, ParseException {
					return byteBuf.read16();
				}
			};
			
		} else {
			
			return new TypeComponent() {
				@Override
				public void writeType(ByteBuf byteBuf, int type) throws BufException {
					byteBuf.write32(type);
				}
				
				@Override
				public int typeLength() {
					return 4;
				}
				
				@Override
				public int readType(ByteBuf byteBuf) throws BufException, ParseException {
					return (int) byteBuf.read32();
				}
			};
			
		}
		
	}
	
	//

	@Override
	public final void read(ByteBuf byteBuf) throws ParseException, BufException {
		
		int type = typeComponent.readType(byteBuf);
		if(type > children.length) {
			throw new UnsupportedMessageTypeException(type);
		}
		
		this.type = type;
		
		activeChild = children[type];
		activeChild.read(byteBuf);
		
	}

	@Override
	public final void write(ByteBuf byteBuf) throws BufException {
		
		typeComponent.writeType(byteBuf, type);
		activeChild.write(byteBuf);
		
	}

	@Override
	public final int length() {
		
		return typeComponent.typeLength() + activeChild.length();
		
	}

	@Override
	public final PacketComponentI[] getChildren() {
		return new PacketComponentI[] { activeChild };
	}

	@Override
	public String toString() {
		if(name == null) name = this.getClass().getSimpleName();
		return String.format("%s(type=%d)", name, type);
	}
	
	public T get() {
		return activeChild;
	}
	
	public T set(int index) {
		return activeChild = children[index];
	}
	
}
