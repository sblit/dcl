package org.dclayer.net.a2s;

import java.net.Socket;

public interface ApplicationConnectionActionListener {
	public ApplicationConnection onApplicationConnection(Socket socket);
}
