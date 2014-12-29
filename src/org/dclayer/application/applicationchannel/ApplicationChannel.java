package org.dclayer.application.applicationchannel;

import java.io.BufferedOutputStream;
import java.io.InputStream;

import org.dclayer.crypto.key.Key;

public interface ApplicationChannel {
	
	public Key getRemotePublicKey();
	public String getActionIdentifier();
	
	public InputStream getInputStream();
	public BufferedOutputStream getOutputStream();

}
