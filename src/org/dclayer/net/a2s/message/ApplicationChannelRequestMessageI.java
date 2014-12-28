package org.dclayer.net.a2s.message;

import org.dclayer.net.componentinterface.KeyComponentI;


public interface ApplicationChannelRequestMessageI {
	
	public int getNetworkSlot();
	public void setNetworkSlot(int networkSlot);
	
	public int getChannelSlot();
	public void setChannelSlot(int channelSlot);
	
	public KeyComponentI getKeyComponent();
	
}
