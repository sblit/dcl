package org.dclayer.net.a2s;

import org.dclayer.net.PacketComponent;

public abstract class A2SRevisionMessage extends PacketComponent {

	public abstract byte getRevision();
	public abstract void callOnReceiveMethod(ApplicationConnection applicationConnection);
	
}
