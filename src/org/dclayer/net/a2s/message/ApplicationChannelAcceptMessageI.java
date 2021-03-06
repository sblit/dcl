package org.dclayer.net.a2s.message;

import org.dclayer.net.componentinterface.DataComponentI;
import org.dclayer.net.componentinterface.KeyComponentI;
import org.dclayer.net.lla.LLA;


public interface ApplicationChannelAcceptMessageI {
	
	public int getNetworkSlot();
	public void setNetworkSlot(int networkSlot);
	
	public int getChannelSlot();
	public void setChannelSlot(int channelSlot);
	
	public String getActionIdentifierSuffix();
	public void setActionIdentifierSuffix(String actionIdentifierSuffix);
	
	public KeyComponentI getKeyComponent();
	
	public LLA getSenderLLA();
	public void setSenderLLA(LLA senderLLA);
	
	public DataComponentI getIgnoreDataComponent();
	
}
