package org.dclayer.net.packetcomponent;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponentI;
import org.dclayer.net.buf.ByteBuf;

public abstract class ParentPacketComponent extends AutoPacketComponent<PacketComponentI> {

	private String name;
	
	public ParentPacketComponent(String name) {
		super(PacketComponentI.class);
		this.name = name;
	}
	
	public ParentPacketComponent() {
		this(null);
	}
	
	//
	
	@Override
	public final void read(ByteBuf byteBuf) throws ParseException, BufException {
		for(PacketComponentI child : children) {
			child.read(byteBuf);
		}
	}

	@Override
	public final void write(ByteBuf byteBuf) throws BufException {
		for(PacketComponentI child : children) {
			child.write(byteBuf);
		}
	}

	@Override
	public final int length() {
		int length = 0;
		for(PacketComponentI child : children) {
			length += child.length();
		}
		return length;
	}

	@Override
	public final PacketComponentI[] getChildren() {
		return children;
	}

	@Override
	public String toString() {
		if(name == null) name = this.getClass().getSimpleName();
		return name;
	}
	
}
