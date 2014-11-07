package org.dclayer.net;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.s2s.rev0.component.ServiceAddressComponent;

/**
 * abstract class that parts of packets (e.g. {@link Message} and {@link ServiceAddressComponent}) extend.
 */
public abstract class PacketComponent {
	/**
	 * constructor called when this {@link PacketComponent} is reconstructed from data in a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} this {@link PacketComponent} is read from
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public PacketComponent(ByteBuf byteBuf) throws ParseException, BufException {
		this.read(byteBuf);
	}
	
	/**
	 * constructor called when this {@link PacketComponent} is newly created (and not reconstructed from data)
	 */
	public PacketComponent() {
		
	}
	
	/**
	 * returns a String representing this {@link PacketComponent} and its children
	 * @param tree true if the representation should be multi-lined, false otherwise
	 * @param level the current level of indentation
	 * @return a String representing this {@link PacketComponent} and its children
	 */
	private final String represent(boolean tree, int level) {
		PacketComponent[] children = this.getChildren();
		StringBuilder b = new StringBuilder();
		String indent = null;
		if(tree) {
			StringBuilder ib = new StringBuilder();
			for(int i = 0; i < level; i++) ib.append("    ");
			indent = ib.toString();
			b.append(indent);
		}
		b.append(this.toString());
		if(children != null && children.length > 0) {
			int ii = 0;
			for(int i = 0; i < children.length; i++) {
				PacketComponent child = children[i];
				if(child == null) continue;
				if(ii++ > 0) b.append(", ");
				else b.append(" {");
				if(tree) b.append("\n");
				b.append(child.represent(tree, level+1));
			}
			if(ii > 0) {
				if(tree) b.append("\n").append(indent);
				b.append("}");
			}
		}
		return b.toString();
	}
	
	/**
	 * returns a String representing this {@link PacketComponent} and its children
	 * @param tree true if the representation should be multi-lined, false otherwise
	 * @return a String representing this {@link PacketComponent} and its children
	 */
	public final String represent(boolean tree) {
		return represent(tree, 0);
	}
	
	/**
	 * returns a single-line String representing this {@link PacketComponent} and its children
	 * @return a single-line String representing this {@link PacketComponent} and its children
	 */
	public final String represent() {
		return represent(false);
	}
	
	/**
	 * reconstructs the {@link PacketComponent} from data in the given {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} containing the data that should be used to reconstruct this {@link PacketComponent}
	 * @throws ParseException thrown if the {@link ByteBuf} can not be parsed
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public abstract void read(ByteBuf byteBuf) throws ParseException, BufException;
	/**
	 * writes this {@link PacketComponent} to a {@link ByteBuf}
	 * @param byteBuf the {@link ByteBuf} to write this {@link PacketComponent} to
	 * @throws BufException thrown if an operation on the {@link ByteBuf} fails
	 */
	public abstract void write(ByteBuf byteBuf) throws BufException;
	/**
	 * returns the amount of bytes this {@link PacketComponent} will occupy if written to a {@link ByteBuf}
	 * @return the amount of bytes this {@link PacketComponent} will occupy if written to a {@link ByteBuf}
	 */
	public abstract int length();
	
	/**
	 * returns a {@link PacketComponent} array containing the children of this {@link PacketComponent}
	 * @return a {@link PacketComponent} array containing the children of this {@link PacketComponent}
	 */
	public abstract PacketComponent[] getChildren();

	public abstract String toString();
	
}
