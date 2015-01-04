package org.dclayer.application.applicationchannel;

import java.io.BufferedOutputStream;
import java.io.InputStream;

import org.dclayer.net.applicationchannel.ApplicationChannelTarget;

public class DCLApplicationChannel extends AbsApplicationChannel {

	public DCLApplicationChannel(ApplicationChannelTarget applicationChannelTarget, ApplicationChannelActionListener applicationChannelActionListener, boolean locallyInitiated) {
		super(applicationChannelTarget, applicationChannelActionListener, locallyInitiated);
	}

	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedOutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getName() {
		return "DCLApplicationChannel";
	}

}
