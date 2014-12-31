package org.dclayer.application.applicationchannelslotmap;

import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.crypto.key.Key;
import org.dclayer.datastructure.map.slotmap.Slot;


public class ApplicationChannelSlot extends Slot<Key> {
	
	private ApplicationChannel applicationChannel;
	
	public ApplicationChannelSlot(int slotId, ApplicationChannel applicationChannel) {
		super(slotId);
		this.applicationChannel = applicationChannel;
	}
	
	public ApplicationChannel getApplicationChannel() {
		return applicationChannel;
	}

	@Override
	public Key getSearchObject() {
		return applicationChannel.getRemotePublicKey();
	}
	
	@Override
	public String contentToString() {
		return applicationChannel.toString();
	}
	
}
