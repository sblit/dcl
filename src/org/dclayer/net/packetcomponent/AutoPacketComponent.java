package org.dclayer.net.packetcomponent;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.LinkedList;

import org.dclayer.net.PacketComponent;
import org.dclayer.net.PacketComponentI;

public abstract class AutoPacketComponent<T extends PacketComponentI> extends PacketComponent {
	
	private static class ChildField {
		
		Child child;
		Field field;
		
		public ChildField(Child child, Field field) {
			this.child = child;
			this.field = field;
		}
		
	}
	
	//
	
	protected final T[] children;
	
	protected AutoPacketComponent(Class<T> commonType) {
		this.children = collectChildren(commonType);
	}
	
	//
	
	private T[] collectChildren(Class<T> commonType) {
		
		LinkedList<ChildField> fields = new LinkedList<>();
		
		for(Field field : this.getClass().getDeclaredFields()) {
			Child childAnnotation = field.getAnnotation(Child.class);
			if(childAnnotation != null) {
				fields.add(new ChildField(childAnnotation, field));
			}
		}
		
		T[] children = (T[]) Array.newInstance(commonType, fields.size());
		
		for(ChildField childField : fields) {
			
			if(!commonType.isAssignableFrom(childField.field.getType())) {
				throw new InstantiationError(String.format("AutoPacketComponent %s: Field '%s': Field type %s is not a sub-class of %s", this.getClass().getName(), childField.field.getName(), childField.field.getType().getName(), commonType.getName()));
			}
			
			if(children[childField.child.index()] != null) {
				throw new InstantiationError(String.format("AutoPacketComponent %s: Field '%s': Duplicate index %d", this.getClass().getName(), childField.field.getName(), childField.child.index()));
			}
			
			T packetComponent;
			
			try {
				packetComponent = (T) childField.field.getType().newInstance();
			} catch (InstantiationException e) {
				throw new InstantiationError(String.format("AutoPacketComponent %s: Field '%s': Could not instantiate type %s", this.getClass().getName(), childField.field.getName(), childField.field.getType().getName()));
			} catch (IllegalAccessException e) {
				throw new InstantiationError(String.format("AutoPacketComponent %s: Field '%s': Could not access type %s", this.getClass().getName(), childField.field.getName(), childField.field.getType().getName()));
			}
			
			try {
				childField.field.set(this, packetComponent);
			} catch (IllegalArgumentException e) {
				throw new InstantiationError(String.format("AutoPacketComponent %s: Field '%s': Could not assign instance of type %s", this.getClass().getName(), childField.field.getName(), childField.field.getType().getName()));
			} catch (IllegalAccessException e) {
				throw new InstantiationError(String.format("AutoPacketComponent %s: Field '%s': Could not access", this.getClass().getName(), childField.field.getName()));
			}
			
			children[childField.child.index()] = packetComponent;
			
		}
		
		return children;
		
	}
	
}
