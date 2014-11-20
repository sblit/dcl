package org.dclayer.net.a2s;

import java.net.Socket;

import org.dclayer.listener.net.NetworkInstanceListener;

public interface ApplicationConnectionActionListener extends NetworkInstanceListener {
	public ApplicationConnection onApplicationConnection(Socket socket);
}
