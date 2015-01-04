package org.dclayer.net.a2s;

import java.net.Socket;

import org.dclayer.listener.net.NetworkInstanceListener;
import org.dclayer.net.Data;
import org.dclayer.net.address.Address;
import org.dclayer.net.applicationchannel.ApplicationChannel;
import org.dclayer.net.interservice.InterservicePolicy;
import org.dclayer.net.llacache.LLA;
import org.dclayer.net.network.NetworkInstance;
import org.dclayer.net.network.component.NetworkPayload;

public interface ApplicationConnectionActionListener extends NetworkInstanceListener {
	public ApplicationConnection onApplicationConnection(Socket socket);
	public void onAddress(Address asymmetricKeyPairAddress);
	public void onServiceNetworkPayload(NetworkPayload networkPayload, NetworkInstance networkInstance);
	
	public LLA getLocalLLA();
	
	public Data getServiceIgnoreData();
	
	public InterservicePolicy makeDefaultIncomingApplicationChannelInterservicePolicy(NetworkInstance networkInstance, ApplicationChannel applicationChannel, LLA remoteLLA);
	public InterservicePolicy makeDefaultOutgoingApplicationChannelInterservicePolicy(NetworkInstance networkInstance, ApplicationChannel applicationChannel, LLA remoteLLA);
	
	public void connect(LLA lla, InterservicePolicy interservicePolicy);
	public void prepareForIncomingConnection(LLA lla, InterservicePolicy interservicePolicy, Data ignoreData);
}
