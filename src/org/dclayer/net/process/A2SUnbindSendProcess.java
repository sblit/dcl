package org.dclayer.net.process;

import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.UnbindMessage;
import org.dclayer.net.process.template.OneTimeA2SSendProcess;
import org.dclayer.net.s2s.rev0.component.ApplicationIdentifierComponent;

/**
 * a one-time application-to-service unbind message send process
 */
public class A2SUnbindSendProcess extends OneTimeA2SSendProcess {
	
	private ApplicationIdentifier applicationIdentifier;
	
	public A2SUnbindSendProcess(ApplicationConnection applicationConnection, ApplicationIdentifier applicationIdentifier) {
		super(applicationConnection);
		this.applicationIdentifier = applicationIdentifier;
	}

	@Override
	public RevisionMessage getA2SMessage(int revision) {
		switch(revision) {
		case 35: {
			return new org.dclayer.net.a2s.rev35.Message(
					new org.dclayer.net.a2s.rev35.message.UnbindMessage(
							new org.dclayer.net.a2s.rev35.component.ApplicationIdentifierComponent(applicationIdentifier)
						)
				);
		}
		case 0:
		default: {
			return new Message(new UnbindMessage(new ApplicationIdentifierComponent(applicationIdentifier)));
		}
		}
	}
	
}
