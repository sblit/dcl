package org.dclayer.net.process;

import org.dclayer.exception.net.parse.UnsupportedRevisionException;
import org.dclayer.listener.net.FollowUpProcessSpawnInterface;
import org.dclayer.listener.net.ProcessRemoveInterface;
import org.dclayer.meta.Log;
import org.dclayer.net.ApplicationIdentifier;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.rev0.Message;
import org.dclayer.net.a2s.rev0.message.BindMessage;
import org.dclayer.net.filter.message.A2SBindMessageFilter;
import org.dclayer.net.process.template.PersistentA2SReceiverProcess;
import org.dclayer.net.process.template.Process;

/**
 * persistent application-to-service receiver process accepting bind messages
 */
public class A2SBindReceiveProcess extends PersistentA2SReceiverProcess {
	
	private FollowUpProcessSpawnInterface followUpProcessSpawnInterface;
	private ProcessRemoveInterface processRemoveInterface;

	public A2SBindReceiveProcess(FollowUpProcessSpawnInterface followUpProcessSpawnInterface, ProcessRemoveInterface processRemoveInterface) {
		super(new A2SBindMessageFilter());
		this.followUpProcessSpawnInterface = followUpProcessSpawnInterface;
		this.processRemoveInterface = processRemoveInterface;
	}

	@Override
	public Process receiveA2S(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		ApplicationIdentifier applicationIdentifier;
		switch(revision) {
		case 0: {
			BindMessage bindMessage = (BindMessage) ((Message) revisionMessage).getMessage();
			applicationIdentifier = bindMessage.getApplicationIdentifierComponent().getApplicationIdentifier();
			break;
		}
		case 35: {
			org.dclayer.net.a2s.rev35.message.BindMessage bindMessage = (org.dclayer.net.a2s.rev35.message.BindMessage) ((org.dclayer.net.a2s.rev35.Message) revisionMessage).getMessage();
			applicationIdentifier = bindMessage.getApplicationIdentifierComponent().getApplicationIdentifier();
			break;
		}
		default: {
			throw new RuntimeException(new UnsupportedRevisionException(revision));
		}
		}
		Log.debug(Log.PART_PROCESS, this, String.format("received bind message for %s from ApplicationConnection %s", applicationIdentifier.toString(), applicationConnection.toString()));
		A2SDataReceiveProcess a2sDataReceiveProcess = new A2SDataReceiveProcess(applicationConnection, applicationIdentifier);
		S2SApplicationDataReceiveProcess s2sApplicationDataReceiveProcess = new S2SApplicationDataReceiveProcess(applicationConnection, applicationIdentifier);
		A2SUnbindReceiveProcess a2sUnbindReceiveProcess = new A2SUnbindReceiveProcess(applicationConnection, applicationIdentifier, processRemoveInterface, a2sDataReceiveProcess, s2sApplicationDataReceiveProcess);
		followUpProcessSpawnInterface.addFollowUpProcess(this, a2sDataReceiveProcess);
		followUpProcessSpawnInterface.addFollowUpProcess(this, s2sApplicationDataReceiveProcess);
		followUpProcessSpawnInterface.addFollowUpProcess(this, a2sUnbindReceiveProcess);
		return new A2SBindSendProcess(applicationConnection, applicationIdentifier);
	}

}
