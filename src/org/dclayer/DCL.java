package org.dclayer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.llacache.InetSocketLLA;
import org.dclayer.net.lladatabase.LLADatabase;
import org.dclayer.net.network.APBRNetworkType;

public class DCL {
	
	/**
	 * The current revision of this DCL Service implementation
	 */
	public static final int REVISION = 0;
	/**
	 * the protocol identifier used for application data channels
	 */
	public static final String CHANNEL_APPDATA = "org.dclayer.s2s.rev0.appdata";

	public static void main(String[] args) throws ParseException {
		
		final LLADatabase llaDatabase = new LLADatabase();
		
		int s2sPort = 1337;
		int a2sPort = 2000;
		
		LinkedList<APBRNetworkType> apbrNetworkTypes = new LinkedList<>();
		
		for(String arg : args) {
			String[] argParts = arg.split("=", 2);
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
					llaDatabase.store(new InetSocketLLA((Inet4Address) InetAddress.getByName(remoteHost), remotePort));
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return;
				}
				break;
			}
			case "apbrnet": {
				APBRNetworkType apbrNetworkType = new APBRNetworkType(argParts[1]);
				System.out.println(String.format("will join network %s", apbrNetworkType));
				apbrNetworkTypes.add(apbrNetworkType);
				break;
			}
			}
		}
		
		System.out.println(String.format("starting... s2s=%d a2s=%d", s2sPort, a2sPort));
		
		DCLService service;
		try {
			service = new DCLService(s2sPort, a2sPort, llaDatabase);
		} catch (IOException e) {
			Log.fatal(Log.PART_MAIN, e);
			return;
		}
		
		for(APBRNetworkType apbrNetworkType : apbrNetworkTypes) {
			apbrNetworkType.activate(service);
			service.getServiceAPBRAddress().getNetworkTypeCollection().addNetworkType(apbrNetworkType);
		}
		
	}

}
