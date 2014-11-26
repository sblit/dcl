package org.dclayer.net.a2s;

import org.dclayer.net.a2s.message.AddressPublicKeyMessageI;
import org.dclayer.net.a2s.message.DataMessageI;
import org.dclayer.net.a2s.message.JoinDefaultNetworksMessageI;
import org.dclayer.net.a2s.message.RevisionMessageI;
import org.dclayer.net.a2s.message.SlotAssignMessageI;


public abstract class A2SMessage extends A2SPacketComponent {

	public abstract byte getRevision();
	
	public abstract RevisionMessageI setRevisionMessage();
	public abstract DataMessageI setDataMessage();
	public abstract SlotAssignMessageI setSlotAssignMessage();
	public abstract AddressPublicKeyMessageI setAddressPublicKeyMessage();
	public abstract JoinDefaultNetworksMessageI setJoinDefaultNetworksMessage();
	
}
