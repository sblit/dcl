package org.dclayer.net.link;
import org.dclayer.net.Data;

/**
 * interface used by {@link Link} instances to send packets
 */
public interface LinkSendInterface {
	public void sendLinkPacket(Link link, Data data);
}
