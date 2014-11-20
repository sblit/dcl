package org.dclayer.net.routing;

import org.dclayer.net.network.component.NetworkPacket;

public interface ForwardDestination {
	public abstract boolean onForward(NetworkPacket networkPacket);
}
