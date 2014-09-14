package org.dclayer.net.process.deliveryagent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dclayer.listener.net.ReceiveFollowUpProcessSpawnInterface;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.ConnectedPacket;
import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.MessageFilterCollection;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.socket.TCPSocketConnection;

/**
 * an application-to-service packet delivery agent that delivers packets to receiver {@link Process}es with matching filters
 */
public class A2SProcessDeliveryAgent {
	
	/**
	 * an {@link ArrayList} containing a collection of {@link Process}es as {@link ProcessMatch} objects
	 */
	private ArrayList<ProcessMatch> processes = new ArrayList<ProcessMatch>();
	/**
	 * a {@link MessageFilterCollection} containing {@link MessageFilter}s for {@link Process}es that accept packets from all {@link ApplicationConnection}s
	 */
	private MessageFilterCollection messageFilterCollection = new MessageFilterCollection();
	
	/**
	 * a {@link ReceiveFollowUpProcessSpawnInterface} used to spawn new {@link Process}es returned by receiver {@link Process}es
	 */
	private ReceiveFollowUpProcessSpawnInterface receiveFollowUpProcessSpawnInterface;
	
	public A2SProcessDeliveryAgent(ReceiveFollowUpProcessSpawnInterface receiveFollowUpProcessSpawnInterface) {
		this.receiveFollowUpProcessSpawnInterface = receiveFollowUpProcessSpawnInterface;
	}
	
	/**
	 * tries to deliver a packet, returns true on success or false if the delivery failed
	 * @param applicationConnection the {@link ApplicationConnection} from which this packet was received
	 * @param revisionMessage the message contained in the packet that was received
	 * @param revision the revision of the received packet
	 * @param messageTypeId the message type id of the received message
	 * @param processMatches the {@link ProcessMatch}es containing the possibly matching {@link Process}es
	 * @return true on success or false if the delivery failed
	 */
	private boolean deliverPacket(ApplicationConnection applicationConnection, RevisionMessage revisionMessage, int revision, int messageTypeId, List<ProcessMatch> processMatches) {
		String packetInfo = String.format("revision=%d, messagetypeid=%d, from=%s", revision, messageTypeId, applicationConnection.toString());
		Process followUpProcess;
		for(ProcessMatch processMatch : processMatches) {
			Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_DELIVERPACKET, this, String.format("delivering Packet (%s) to Process %s", packetInfo, processMatch.process.toString()));
			if((followUpProcess = processMatch.process.receiveA2S(applicationConnection, revisionMessage, revision, messageTypeId)) != null) {
				Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_DELIVERPACKET, this, String.format("Process %s accepted Packet (%s), returned Process: %s", processMatch.process.toString(), packetInfo, followUpProcess.toString()));
				receiveFollowUpProcessSpawnInterface.addReceiveFollowUpProcess(processMatch.process, followUpProcess);
				return true;
			}
		}
		return false;
	}

	/**
	 * tries to deliver the given {@link ConnectedPacket}
	 * @param connectedPacket the {@link ConnectedPacket} to deliver
	 */
	public synchronized void receive(ConnectedPacket connectedPacket) {
		TCPSocketConnection tcpSocketConnection = connectedPacket.getTCPSocketConnection();
		ApplicationConnection applicationConnection = tcpSocketConnection.getApplicationConnection();
		if(applicationConnection == null) {
			applicationConnection = new ApplicationConnection(tcpSocketConnection);
			tcpSocketConnection.setApplicationConnection(applicationConnection);
		}
		
		RevisionMessage revisionMessage = connectedPacket.getMessage();
		int revision = connectedPacket.getRevision();
		int messageTypeId = revisionMessage.getMessageTypeId();
		
		applicationConnection.setRevision(revision);
		
		LinkedList<ProcessMatch> processMatches;
		
		MessageFilterCollection messageFilterCollection = applicationConnection.getMessageFilterCollection();
		Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_RECEIVE, this, String.format("%s MessageFilterCollection attached to ApplicationConnection %s", messageFilterCollection == null ? "no" : "got a", applicationConnection.toString()));
		if(messageFilterCollection != null) {
			processMatches = (LinkedList<ProcessMatch>) messageFilterCollection.getMatches(revision, messageTypeId);
			Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_RECEIVE, this, String.format("%s Processes subscribed to messagetypeid %d of revision %d via CachedServiceAddress-attached MessageFilterCollection", processMatches != null ? processMatches.size() : "no", messageTypeId, revision));
			if(processMatches != null && deliverPacket(applicationConnection, revisionMessage, revision, messageTypeId, processMatches)) return;
		}
		
		processMatches = (LinkedList<ProcessMatch>) this.messageFilterCollection.getMatches(revision, messageTypeId);
		Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_RECEIVE, this, String.format("%s Processes subscribed to messagetypeid %d of revision %d", processMatches != null ? processMatches.size() : "no", messageTypeId, revision));
		if(processMatches != null && deliverPacket(applicationConnection, revisionMessage, revision, messageTypeId, processMatches)) return;
		
		Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_RECEIVE, this, String.format("could not deliver Packet from application connection %s with messagetypeid %d of revision %d", applicationConnection.toString(), messageTypeId, revision));
	}
	
	/**
	 * adds a {@link Process} to this delivery agent
	 * @param process the Process to add
	 */
	public synchronized void addProcess(Process process) {
		ProcessMatch processMatch = new ProcessMatch(process);
		boolean hasApplicationConnectionFilter = process.hasApplicationConnectionFilter();
		Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_ADD, this, String.format("adding Process %s (hasApplicationConnectionFilter=%s)", process.toString(), hasApplicationConnectionFilter));
		processes.add(processMatch);
		processMatch.messageFilter = process.getMessageFilter();
		if(hasApplicationConnectionFilter) {
			processMatch.applicationConnectionAttachedTo = process.getApplicationConnectionFilter().getApplicationConnection();
			processMatch.applicationConnectionAttachedTo.getMessageFilterCollection(true).addFilter(processMatch.messageFilter, processMatch);
		} else {
			messageFilterCollection.addFilter(processMatch.messageFilter, processMatch);
		}
	}
	
	/**
	 * removes a {@link Process} from this deliver agent
	 * @param process the {@link Process} to remove
	 */
	public synchronized void removeProcess(Process process) {
		int i = 0;
		for(ProcessMatch processMatch : processes) {
			if(processMatch.process == process) {
				Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_REMOVE_SUCC, this, String.format("removing Process %s", process.toString()));
				if(processMatch.cachedServiceAddressAttachedTo == null) {
					messageFilterCollection.removeFilter(processMatch.messageFilter, processMatch);
				} else {
					MessageFilterCollection messageFilterCollection = processMatch.cachedServiceAddressAttachedTo.getMessageFilterCollection();
					if(messageFilterCollection != null) messageFilterCollection.removeFilter(processMatch.messageFilter, processMatch);
				}
				processes.remove(i);
				return;
			}
			i++;
		}
		Log.debug(Log.PART_A2SPROCESSDELIVERYAGENT_REMOVE_FAIL, this, String.format("could not remove Process %s (not in queue)", process.toString()));
	}
	
}
