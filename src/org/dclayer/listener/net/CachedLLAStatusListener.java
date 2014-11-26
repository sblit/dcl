package org.dclayer.listener.net;

import org.dclayer.net.llacache.CachedLLA;

public interface CachedLLAStatusListener {
	public void onStatusChanged(CachedLLA cachedLLA, int oldStatus, int newStatus);
}
