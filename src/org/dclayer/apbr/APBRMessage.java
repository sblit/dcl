package org.dclayer.apbr;

import org.dclayer.DCLService;
import org.dclayer.net.PacketComponent;

public abstract class APBRMessage extends PacketComponent {
	
	public abstract int getTypeId();
	public abstract void callOnReceiveMethod(DCLService dclService);
	
}
