package org.dclayer.net.packetcomponent;


import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponentI;
import org.dclayer.net.buf.ByteBuf;

public abstract class ParentPacketComponent extends AutoPacketComponent<PacketComponentI, AutoPacketComponentChildInfo<PacketComponentI>> {

	private String name;
	
	public ParentPacketComponent(String name) {
		super(PacketComponentI.class, AutoPacketComponentChildInfo.class);
		this.name = name;
	}
	
	public ParentPacketComponent() {
		this(null);
	}
	
	//
	
	@Override
	public final void read(ByteBuf byteBuf) throws ParseException, BufException {
		for(AutoPacketComponentChildInfo<?> child : children) {
			child.packetComponent.read(byteBuf);
		}
	}

	@Override
	public final void write(ByteBuf byteBuf) throws BufException {
		for(AutoPacketComponentChildInfo<?> child : children) {
			child.packetComponent.write(byteBuf);
		}
	}

	@Override
	public final int length() {
		int length = 0;
		for(AutoPacketComponentChildInfo<?> child : children) {
			length += child.packetComponent.length();
		}
		return length;
	}

	@Override
	public final PacketComponentI[] getChildren() {
		return packetComponentChildren;
	}

	@Override
	public String toString() {
		if(name == null) name = this.getClass().getSimpleName();
		return name;
	}
	
}
