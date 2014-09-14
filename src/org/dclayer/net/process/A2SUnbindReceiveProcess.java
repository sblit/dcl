package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.listener.net.OnConnectionErrorListener;
import org.dclayer.listener.net.ProcessRemoveInterface;
import org.dclayer.meta.Log;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.UnbindMessage;
import org.dclayer.net.filter.message.A2SUnbindMessageFilter;
import org.dclayer.net.process.template.OneTimeA2SReceiverProcess;
import org.dclayer.net.process.template.Process;

/**
 * a persistent application-to-service unbind message receiver process,
 * removes the supplied {@link Process}es upon receipt of a matching unbind message
 */
public class A2SUnbindReceiveProcess extends OneTimeA2SReceiverProcess implements OnConnectionErrorListener {
	
	private ApplicationIdentifier applicationIdentifier;
	private ProcessRemoveInterface processRemoveInterface;
	/**
	 * the first {@link Process} to remove upon unbind message receipt
	 */
	private Process removeProcess1;
	/**
	 * the second {@link Process} to remove upon unbind message receipt
	 */
	private Process removeProcess2;

	public A2SUnbindReceiveProcess(ApplicationConnection applicationConnection, ApplicationIdentifier applicationIdentifier, ProcessRemoveInterface processRemoveInterface, Process removeProcess1, Process removeProcess2) {
		super(applicationConnection, new A2SUnbindMessageFilter(), 0);
		applicationConnection.setOnConnectionErrorListener(this);
		this.applicationIdentifier = applicationIdentifier;
		this.processRemoveInterface = processRemoveInterface;
		this.removeProcess1 = removeProcess1;
		this.removeProcess2 = removeProcess2;
	}
	
	@Override
	public int defineProperties() {
		return super.defineProperties() & ~TIMEOUT;
	}

	@Override
	public Process receiveA2S(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		ApplicationIdentifier applicationIdentifier;
		
		switch(revision) {
		case 0: {
			UnbindMessage unbindMessage = (UnbindMessage) ((Message) revisionMessage).getMessage();
			applicationIdentifier = unbindMessage.getApplicationIdentifierComponent().getApplicationIdentifier();
			break;
		}
		case 35: {
			org.dclayer.net.a2s.rev35.message.UnbindMessage unbindMessage = (org.dclayer.net.a2s.rev35.message.UnbindMessage) ((org.dclayer.net.a2s.rev35.Message) revisionMessage).getMessage();
			applicationIdentifier = unbindMessage.getApplicationIdentifierComponent().getApplicationIdentifier();
			break;
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
		
		boolean match = this.applicationIdentifier.equals(applicationIdentifier);
		Log.debug(Log.PART_PROCESS, this, String.format("received unbind message for %s from ApplicationConnection %s, match=%s", applicationIdentifier.toString(), applicationConnection.toString(), match));
		if(match) {
			removeProcesses();
			return new A2SUnbindSendProcess(applicationConnection, applicationIdentifier);
		} else {
			return null;
		}
	}

	@Override
	public Process onFinalize(boolean timeout) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * removes the {@link Process}es
	 */
	private void removeProcesses() {
		processRemoveInterface.removeProcess(this, removeProcess1);
		processRemoveInterface.removeProcess(this, removeProcess2);
	}

	@Override
	public void onConnectionError() {
		Log.debug(Log.PART_PROCESS, this, "onConnectionError()");
		removeProcesses();
	}

}
