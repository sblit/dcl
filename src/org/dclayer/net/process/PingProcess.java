package org.dclayer.net.process;

import org.dclayer.meta.Log;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.AddressCacheCursor;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.process.template.DaemonProcess;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * a deamon {@link Process} that constantly re-pings {@link ServiceAddress}es in the {@link AddressCache}
 */
public class PingProcess extends DaemonProcess {

	@Override
	protected void runDaemon() {
		AddressCacheCursor reachableCursor = getAddressCache().getNewCursor();
		AddressCacheCursor nonReachableCursor = getAddressCache().getNewCursor();
		for(;;) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			
			CachedServiceAddress cachedServiceAddress;
			
			for(;;) {
				cachedServiceAddress = getAddressCache().getReachableAddressToPing(reachableCursor);
				if(cachedServiceAddress == null) break;
				Log.debug(Log.PART_PROCESS, this, String.format("re-pinging reachable address %s", cachedServiceAddress.toString()));
				getFollowUpProcessSpawnInterface().addFollowUpProcess(this, new PingSendProcess(cachedServiceAddress, 2));
			}
			
			cachedServiceAddress = getAddressCache().getNonReachableAddressToPing(nonReachableCursor);
			if(cachedServiceAddress != null) {
				getFollowUpProcessSpawnInterface().addFollowUpProcess(this, new PingSendProcess(cachedServiceAddress, PingSendProcess.FLAG_REQUESTKNOWNADDRESSES|PingSendProcess.FLAG_TRYPINGREDIRECT, 0));
			}
			
		}
	}
	
}
