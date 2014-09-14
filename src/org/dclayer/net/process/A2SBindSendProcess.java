package org.dclayer.net.process;

import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.BindMessage;
import org.dclayer.net.process.template.OneTimeA2SSendProcess;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;

/**
 * a one-time application-to-service bind message send process
 */
public class A2SBindSendProcess extends OneTimeA2SSendProcess {
	
	/**
	 * the {@link ApplicationIdentifier} of the bind message sent by this {@link Process}
	 */
	private ApplicationIdentifier applicationIdentifier;
	
	public A2SBindSendProcess(ApplicationConnection applicationConnection, ApplicationIdentifier applicationIdentifier) {
		super(applicationConnection);
		this.applicationIdentifier = applicationIdentifier;
	}

	@Override
	public RevisionMessage getA2SMessage(int revision) {
		switch(revision) {
		case 35: {
			return new org.dclayer.net.a2s.rev35.Message(
					new org.dclayer.net.a2s.rev35.message.BindMessage(
							new org.dclayer.net.a2s.rev35.component.ApplicationIdentifierComponent(applicationIdentifier)
						)
				);
		}
		case 0:
		default: {
			return new Message(new BindMessage(new ApplicationIdentifierComponent(applicationIdentifier)));
		}
		}
	}
	
}
