package org.dclayer.net.packetcomponent;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.exception.net.parse.UnsupportedMessageTypeException;
import org.dclayer.net.PacketComponentI;
import org.dclayer.net.buf.ByteBuf;

public abstract class SwitchPacketComponent<T extends PacketComponentI> extends AutoPacketComponent<T, SwitchPacketComponent.ChildInfo<T>> {
	
	private static interface TypeComponent {
		
		public int readType(ByteBuf byteBuf) throws BufException, ParseException;
		public void writeType(ByteBuf byteBuf, int type) throws BufException;
		public int typeLength();
		
	}
	
	protected static class ChildInfo<U extends PacketComponentI> extends AutoPacketComponentChildInfo<U> {
		Method onReceiveMethod;
	}
	
	//
	
	private ChildInfo<T> activeChild;
	private int type;
	
	private final TypeComponent typeComponent;
	
	private Object onReceiveObject;
	
	public SwitchPacketComponent() {
		super(PacketComponentI.class, ChildInfo.class);
		this.typeComponent = makeTypeComponent(children.length);
	}
	
	public SwitchPacketComponent(Object onReceiveObject) {
		this();
		collectOnReceiveMethods(onReceiveObject);
	}
	
	//
	
	/**
	 * scans the given object for methods with {@link OnReceive} annotations and
	 * stores them for later use with {@link #callOnReceive()}
	 * @param onReceiveObject the object to scan
	 * @deprecated use constructor {@link #SwitchPacketComponent(Object)} instead
	 */
	@Deprecated
	public void loadOnReceiveObject(Object onReceiveObject) {
		
		collectOnReceiveMethods(onReceiveObject);
	
	}
	
	private void collectOnReceiveMethods(Object onReceiveObject) {
		
		for(Method method : onReceiveObject.getClass().getMethods()) {
			
			OnReceive onReceiveAnnotation = method.getAnnotation(OnReceive.class);
			if(onReceiveAnnotation != null) {
				
				ChildInfo<T> child = children[onReceiveAnnotation.index()];
				
				Class<?>[] parameterTypes = method.getParameterTypes();
				if(parameterTypes.length != 1
						|| !parameterTypes[0].isAssignableFrom(child.packetComponent.getClass())) {
					throw new InstantiationError(String.format("Invalid on receive callback method '%s': must accept exactly one parameter of type or supertype of '%s'", method.getName(), child.packetComponent.getClass().getSimpleName()));
				}
				
				child.onReceiveMethod = method;
				
			}
			
		}

		this.onReceiveObject = onReceiveObject;
		
	}
	
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
		activeChild.packetComponent.read(byteBuf);
		
	}

	@Override
	public final void write(ByteBuf byteBuf) throws BufException {
		
		typeComponent.writeType(byteBuf, type);
		activeChild.packetComponent.write(byteBuf);
		
	}

	@Override
	public final int length() {
		
		return typeComponent.typeLength() + activeChild.packetComponent.length();
		
	}

	@Override
	public final PacketComponentI[] getChildren() {
		return new PacketComponentI[] { activeChild.packetComponent };
	}

	@Override
	public String toString() {
		return String.format("%s(type=%d)", this.getClass().getSimpleName(), type);
	}
	
	public T get() {
		return activeChild.packetComponent;
	}
	
	public T set(int index) {
		type = index;
		return (activeChild = children[index]).packetComponent;
	}
	
	public void callOnReceive() {
		try {
			activeChild.onReceiveMethod.invoke(onReceiveObject, activeChild.packetComponent);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
