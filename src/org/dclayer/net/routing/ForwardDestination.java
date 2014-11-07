package org.dclayer.net.routing;

import org.dclayer.net.address.Address;

public interface ForwardDestination<T> {
	public boolean onForward(T object);
	public Address getAddress();
}
