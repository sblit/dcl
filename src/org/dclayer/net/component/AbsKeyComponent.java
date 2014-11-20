package org.dclayer.net.component;

import org.dclayer.crypto.key.Key;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.net.PacketComponent;

public abstract class AbsKeyComponent extends PacketComponent {
	
	public abstract byte getTypeId();
	
	public abstract Key getKey() throws InsufficientKeySizeException;
	
}
