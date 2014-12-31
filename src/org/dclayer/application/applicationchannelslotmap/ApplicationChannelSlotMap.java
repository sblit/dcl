package org.dclayer.application.applicationchannelslotmap;

import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.crypto.key.Key;
import org.dclayer.datastructure.map.slotmap.SlotMap;

public class ApplicationChannelSlotMap extends SlotMap<ApplicationChannel, Key, ApplicationChannelSlot> {

	@Override
	public ApplicationChannelSlot makeSlot(int slotId, ApplicationChannel applicationChannel) {
		return new ApplicationChannelSlot(slotId, applicationChannel);
	}
	
}
