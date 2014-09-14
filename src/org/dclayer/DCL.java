package org.dclayer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.dclayer.meta.Log;
import org.dclayer.net.addresscache.AddressCache;
import org.dclayer.net.addresscache.CachedServiceAddress;
import org.dclayer.net.serviceaddress.ServiceAddressIPv4;

public class DCL {
	
	/**
	 * The current revision of this DCL Service implementation
	 */
	public static final int REVISION = 0;
	/**
	 * the protocol identifier used for application data channels
	 */
	public static final String CHANNEL_APPDATA = "org.dclayer.s2s.rev0.appdata";

	public static void main(String[] args) {
		
		final AddressCache addressCache = new AddressCache();
		
		int s2sPort = 1337;
		int a2sPort = 2000;
		
		for(String arg : args) {
			String[] argParts = arg.split("=");
			if(argParts.length < 2) continue;
			switch(argParts[0]) {
			case "s2s": {
				s2sPort = Integer.parseInt(argParts[1]);
				break;
			}
			case "a2s": {
				a2sPort = Integer.parseInt(argParts[1]);
				break;
			}
			case "remote": {
				String[] remoteParts = argParts[1].split(":");
				String remoteHost = remoteParts[0];
				int remotePort = Integer.parseInt(remoteParts[1]);
				System.out.println(String.format("adding remote: host=%s port=%s", remoteHost, remotePort));
				try {
					addressCache.addServiceAddress(new ServiceAddressIPv4((Inet4Address) InetAddress.getByName(remoteHost), remotePort), 0);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return;
				}
				break;
			}
			}
		}
		
		System.out.println(String.format("starting... s2s=%d a2s=%d", s2sPort, a2sPort));
		
		DCLService service;
		try {
			service = new DCLService(s2sPort, a2sPort, addressCache);
		} catch (IOException e) {
			Log.fatal(Log.PART_MAIN, e);
			return;
		}
		
		for(;;) {
			StringBuilder addressDump = new StringBuilder();
			List<CachedServiceAddress> cachedServiceAddresses = addressCache.get(10, 0, 0, 0);
			for(CachedServiceAddress cSA : cachedServiceAddresses) {
				addressDump.append(String.format("\n\t%s", cSA));
			}
			Log.debug(Log.PART_MAIN, null, String.format("addresses currently in cache (total %d):%s", addressCache.size(), addressDump.toString()));
			try { Thread.sleep(5000); }
			catch (InterruptedException e) { break; }
		}
	}

}
