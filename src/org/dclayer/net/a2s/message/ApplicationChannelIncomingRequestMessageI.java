package org.dclayer.net.a2s.message;

import org.dclayer.net.componentinterface.KeyComponentI;
import org.dclayer.net.llacache.LLA;


public interface ApplicationChannelIncomingRequestMessageI {
	
	public int getNetworkSlot();
	public void setNetworkSlot(int networkSlot);
	
	public String getActionIdentifierSuffix();
	public void setActionIdentifierSuffix(String actionIdentifierSuffix);
	
	public KeyComponentI getKeyComponent();
	
	public LLA getSenderLLA();
	public void setSenderLLA(LLA senderLLA);
	
}
