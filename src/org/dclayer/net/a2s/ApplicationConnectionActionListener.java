package org.dclayer.net.a2s;

import java.net.Socket;

import org.dclayer.listener.net.NetworkInstanceListener;
import org.dclayer.net.address.Address;

public interface ApplicationConnectionActionListener extends NetworkInstanceListener {
	public ApplicationConnection onApplicationConnection(Socket socket);
	public void onAddress(Address asymmetricKeyPairAddress);
}
