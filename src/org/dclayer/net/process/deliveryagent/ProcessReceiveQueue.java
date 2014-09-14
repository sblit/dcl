package org.dclayer.net.process.deliveryagent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dclayer.listener.net.ReceiveFollowUpProcessSpawnInterface;
import org.dclayer.meta.Log;
import org.dclayer.net.RevisionMessage;
import org.dclayer.net.a2s.ApplicationConnection;
import org.dclayer.net.a2s.ConnectedPacket;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.filter.MessageFilter;
import org.dclayer.net.filter.MessageFilterCollection;
import org.dclayer.net.process.template.Process;
import org.dclayer.net.s2s.AddressedPacket;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * an service-to-service packet delivery agent that delivers packets to receiver {@link Process}es with matching filters
 */
public class ProcessReceiveQueue {
	
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
	/**
	 * the {@link AddressCache} used to get corresponding {@link CachedServiceAddress} objects to each {@link ServiceAddress}
	 */
	private AddressCache addressCache;
	
	public ProcessReceiveQueue(ReceiveFollowUpProcessSpawnInterface receiveFollowUpProcessSpawnInterface, AddressCache addressCache) {
		this.receiveFollowUpProcessSpawnInterface = receiveFollowUpProcessSpawnInterface;
		this.addressCache = addressCache;
	}
	
	/**
	 * tries to deliver a packet, returns true on success or false if the delivery failed
	 * @param cachedServiceAddress the {@link CachedServiceAddress} from which this packet was received
	 * @param revisionMessage the message contained in the packet that was received
	 * @param revision the revision of the received packet
	 * @param messageTypeId the message type id of the received message
	 * @param processMatches the {@link ProcessMatch}es containing the possibly matching {@link Process}es
	 * @return true on success or false if the delivery failed
	 */
	private boolean deliverPacket(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId, List<ProcessMatch> processMatches) {
		String packetInfo = String.format("revision=%d, messagetypeid=%d, from=%s", revision, messageTypeId, cachedServiceAddress.getServiceAddress().toString());
		Process followUpProcess;
		for(ProcessMatch processMatch : processMatches) {
			Log.debug(Log.PART_PROCESSRECEIVEQUEUE_DELIVERPACKET, this, String.format("delivering Packet (%s) to Process %s", packetInfo, processMatch.process.toString()));
			if((followUpProcess = processMatch.process.receiveS2S(cachedServiceAddress, revisionMessage, revision, messageTypeId)) != null) {
				Log.debug(Log.PART_PROCESSRECEIVEQUEUE_DELIVERPACKET, this, String.format("Process %s accepted Packet (%s), returned Process: %s", processMatch.process.toString(), packetInfo, followUpProcess.toString()));
				receiveFollowUpProcessSpawnInterface.addReceiveFollowUpProcess(processMatch.process, followUpProcess);
				return true;
			}
		}
		return false;
	}

	/**
	 * tries to deliver the given {@link AddressedPacket}
	 * @param addressedPacket the {@link AddressedPacket} to deliver
	 */
	public void receive(AddressedPacket addressedPacket) {
		CachedServiceAddress cachedServiceAddress = addressCache.addServiceAddress(addressedPacket.getServiceAddress(), AddressCache.STATUS_RECV);
		RevisionMessage revisionMessage = addressedPacket.getMessage();
		int revision = addressedPacket.getRevision();
		int messageTypeId = revisionMessage.getMessageTypeId();
		
		receive(cachedServiceAddress, revisionMessage, revision, messageTypeId);
	}
	
	/**
	 * tries to deliver the given message
	 * @param cachedServiceAddress the {@link CachedServiceAddress} this message was received from
	 * @param revisionMessage the message
	 * @param revision the revision this message is in
	 * @param messageTypeId the typeId of the message
	 */
	public void receive(CachedServiceAddress cachedServiceAddress, RevisionMessage revisionMessage, int revision, int messageTypeId) {
		
		LinkedList<ProcessMatch> processMatches;
		
		MessageFilterCollection messageFilterCollection = cachedServiceAddress.getMessageFilterCollection();
		Log.debug(Log.PART_PROCESSRECEIVEQUEUE_RECEIVE, this, String.format("%s MessageFilterCollection attached to CachedServiceAddress %s", messageFilterCollection == null ? "no" : "got a", cachedServiceAddress.toString()));
		if(messageFilterCollection != null) {
			processMatches = (LinkedList<ProcessMatch>) messageFilterCollection.getMatches(revision, messageTypeId);
			Log.debug(Log.PART_PROCESSRECEIVEQUEUE_RECEIVE, this, String.format("%s Processes subscribed to messagetypeid %d of revision %d via CachedServiceAddress-attached MessageFilterCollection", processMatches != null ? processMatches.size() : "no", messageTypeId, revision));
			if(processMatches != null && deliverPacket(cachedServiceAddress, revisionMessage, revision, messageTypeId, processMatches)) return;
		}
		
		processMatches = (LinkedList<ProcessMatch>) this.messageFilterCollection.getMatches(revision, messageTypeId);
		Log.debug(Log.PART_PROCESSRECEIVEQUEUE_RECEIVE, this, String.format("%s Processes subscribed to messagetypeid %d of revision %d", processMatches != null ? processMatches.size() : "no", messageTypeId, revision));
		if(processMatches != null && deliverPacket(cachedServiceAddress, revisionMessage, revision, messageTypeId, processMatches)) return;
		
		Log.debug(Log.PART_PROCESSRECEIVEQUEUE_RECEIVE, this, String.format("could not deliver Packet from %s with messagetypeid %d of revision %d", cachedServiceAddress.toString(), messageTypeId, revision));
	}
	
	/**
	 * adds a {@link Process} to this delivery agent
	 * @param process the Process to add
	 */
	public synchronized void addProcess(Process process) {
		ProcessMatch processMatch = new ProcessMatch(process);
		boolean hasCachedServiceAddressFilter = process.hasCachedServiceAddressFilter();
		Log.debug(Log.PART_PROCESSRECEIVEQUEUE_ADD, this, String.format("adding Process %s (hasCachedServiceAddressFilter=%s)", process.toString(), hasCachedServiceAddressFilter));
		processes.add(processMatch);
		processMatch.messageFilter = process.getMessageFilter();
		if(hasCachedServiceAddressFilter) {
			processMatch.cachedServiceAddressAttachedTo = process.getCachedServiceAddressFilter().getCachedServiceAddress();
			processMatch.cachedServiceAddressAttachedTo.getMessageFilterCollection(true).addFilter(processMatch.messageFilter, processMatch);
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
				Log.debug(Log.PART_PROCESSRECEIVEQUEUE_REMOVE_SUCC, this, String.format("removing Process %s", process.toString()));
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
		Log.debug(Log.PART_PROCESSRECEIVEQUEUE_REMOVE_FAIL, this, String.format("could not remove Process %s (not in queue)", process.toString()));
	}
	
}
