package org.dclayer.net.process.template;

import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;

/**
 * base class for one-time application-to-service message send process
 */
public abstract class OneTimeA2SSendProcess extends Process {
	
	/**
	 * the {@link ApplicationConnection} over which to send the message
	 */
	private ApplicationConnection applicationConnection;
	
	public OneTimeA2SSendProcess(ApplicationConnection applicationConnection) {
		this.applicationConnection = applicationConnection;
	}

	@Override
	protected int defineProperties() {
		return A2SMESSAGE | FINALIZECALLBACK;
	}
	
	@Override
	public ApplicationConnection getApplicationConnection() {
		return applicationConnection;
	}
	
	@Override
	public abstract RevisionMessage getA2SMessage(int revision);
	
	@Override
	public Process onFinalize(boolean timeout) {
		return null;
	}

}
