package org.dclayer.net.packetcomponent;

import java.lang.reflect.Field;

import org.dclayer.net.PacketComponentI;

public class AutoPacketComponentChildInfo<T extends PacketComponentI> {
	
	private Field field;
	private Object object;
	
	private T packetComponent;
	
	public void setField(Field field, Object object) {
		this.field = field;
	}
	
	public Field getField() {
		return field;
	}
	
	public void setPacketComponent(T packetComponent) {
		this.packetComponent = packetComponent;
	}
	
	public T getPacketComponent() {
		if(packetComponent == null) {
			try {
				packetComponent = (T) field.get(object);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(String.format("AutoPacketComponent %s: Field '%s': Could not access", field.getName(), object), e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(String.format("AutoPacketComponent %s: Field '%s': Could not access", field.getName(), object), e);
			}
		}
		return packetComponent;
	}
	
}