package org.dclayer;

import java.util.List;
import java.util.Random;

import org.dclayer.meta.HierarchicalLevel;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.llacache.CachedLLA;
import org.dclayer.net.llacache.LLA;

public class ConnectionInitiationManager extends Thread implements HierarchicalLevel {
	
	/**
	 * the initial delay between sending messages to initiate a connection with an LLA.
	 * the delay will be increased during connection initiation
	 */
	public static final int CONNECT_STARTDELAY_MILLIS = 500;
	
	/**
	 * the number of messages after which to give up trying to initiate a connection with an LLA
	 */
	public static final int MAX_NUM_CONNECTION_INIT_MSGS = 5;
	
	//

	private DCLService dclService;
	
	public ConnectionInitiationManager(DCLService dclService) {
		this.dclService = dclService;
		this.start();
	}

	@Override
	public HierarchicalLevel getParentHierarchicalLevel() {
		return dclService;
	}
	
	@Override
	public String toString() {
		return "ConnectionInitiationManager";
	}
	
	@Override
	public void run() {
		List<LLA> llas = dclService.getLLAs();
		for(LLA lla : llas) {
			connect(lla);
		}
	}
	
	private void connect(LLA lla) {
		
		final CachedLLA cachedLLA = dclService.getLLACache().getCachedLLA(lla, true);
		
		if(!cachedLLA.changeStatus(CachedLLA.DISCONNECTED, CachedLLA.CONNECTING_PRELINK)) {
			Log.debug(this, "not connecting to %s (status not disconnected)", cachedLLA);
			return;
		}
		
		Log.debug(this, "attempting to connect to %s", cachedLLA);
		
		final Data data = new Data((int)(Math.random() * 8) + 8);
		new Random().nextBytes(data.getData());
		
		(new Thread() {
			@Override
			public void run() {
				
				int n = 0;
				int delay = CONNECT_STARTDELAY_MILLIS;
				boolean disconnected;
				
				while((disconnected = (cachedLLA.getStatus() == CachedLLA.CONNECTING_PRELINK)) && n < MAX_NUM_CONNECTION_INIT_MSGS) {
					
					dclService.sendLinkPacket(cachedLLA, data);
					
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						Log.exception(this, e);
						break;
					}
					
					delay *= 2;
					n++;
					
				}
				
				if(disconnected && cachedLLA.changeStatus(CachedLLA.CONNECTING_PRELINK, CachedLLA.DISCONNECTED)) {
					Log.debug(ConnectionInitiationManager.this, "could not connect to %s", cachedLLA);
				}
				
			}
		}).start();
		
	}
	
}
