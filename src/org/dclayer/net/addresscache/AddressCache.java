package org.dclayer.net.addresscache;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dclayer.meta.Log;
import org.dclayer.net.serviceaddress.ServiceAddress;

/**
 * a cache for {@link ServiceAddress}es that manages address statuses and schedules periodic re-pinging
 */
public class AddressCache {
	/**
	 * ping status
	 */
	public static final int STATUS_PING = 0x3;
	/**
	 * ping sent
	 */
	public static final int STATUS_PING_SENT = 0x1;
	/**
	 * ping successful
	 */
	public static final int STATUS_PING_SUCCESSFUL = 0x2;
	/**
	 * ping timed out
	 */
	public static final int STATUS_PING_TIMEOUT = 0x3;
	
	
	/**
	 * known addresses request status
	 */
	public static final int STATUS_KNOWNADDRESSESREQUEST = (0x3 << 2);
	/**
	 * known addresses request sent
	 */
	public static final int STATUS_KNOWNADDRESSESREQUEST_SENT = (0x1 << 2);
	/**
	 * known addresses request successful
	 */
	public static final int STATUS_KNOWNADDRESSESREQUEST_SUCCESSFUL = (0x2 << 2);
	/**
	 * known addresses request timed out
	 */
	public static final int STATUS_KNOWNADDRESSESREQUEST_TIMEOUT = (0x3 << 2);
	
	
	/**
	 * according to an already reachable service instance, this address is reachable from said service instance (e.g., received in a known addresses reply message)
	 */
	public static final int STATUS_REACHABLEFROMREACHABLE = (1 << 4);
	
	
	/**
	 * data was received from this address
	 */
	public static final int STATUS_RECV = (1 << 5);
	
	
	/**
	 * address is reachable
	 * private flag
	 */
	private static final int STATUS_REACHABLE = (1 << 6);
	
	
//	/**
//	 * address timed out
//	 */
//	public static final int STATUS_TIMEOUT = (1 << 7);
	
	
//	/**
//	 * address was reachable, but is no longer
//	 * private flag
//	 */
//	private static final int STATUS_LOSTINACTION = (1 << 8);
	
	/**
	 * creates a human-readable String representing the given status and returns it
	 * @param status the status to represent
	 * @return the human-readable representation of the given status
	 */
	public static String statusToString(int status) {
		int pingStatus = (status & STATUS_PING);
		
		StringBuilder b = new StringBuilder();
		boolean space = false;
		
		if((status & STATUS_REACHABLE) != 0) {
			if(space) b.append(" ");
			b.append("REACHABLE");
			space = true;
		}
		
		if((status & STATUS_REACHABLEFROMREACHABLE) != 0) {
			if(space) b.append(" ");
			b.append("RFR");
			space = true;
		}
		
		if((status & STATUS_RECV) != 0) {
			if(space) b.append(" ");
			b.append("RECV");
			space = true;
		}
		
		if(pingStatus != 0) {
			if(space) b.append(" ");
			b.append("PING");
			space = true;
			if(pingStatus == STATUS_PING_SENT) b.append("_SENT");
			else if(pingStatus == STATUS_PING_SUCCESSFUL) b.append("_SUCC");
			else if(pingStatus == STATUS_PING_TIMEOUT) b.append("_FAIL");
		}
		
		return b.toString();
	}
	
	// ---
	
	/**
	 * returns a new {@link AddressCacheCursor}
	 * @return a new {@link AddressCacheCursor}
	 */
	public AddressCacheCursor getNewCursor() {
		return new AddressCacheCursor();
	}
	
	/**
	 * {@link ArrayList} holding all known addresses
	 */
	private ArrayList<CachedServiceAddress> addresses = new ArrayList<CachedServiceAddress>();
	/**
	 * {@link ArrayList} holding all reachable addresses
	 */
	private ArrayList<CachedServiceAddress> reachableAddresses = new ArrayList<CachedServiceAddress>();
	
	/**
	 * two-dimensional Array of {@link LinkedList}s holding all known addresses,
	 * used to quickly find the {@link CachedServiceAddress} instance for an address
	 */
	private LinkedList[][] table = new LinkedList[256][];
	
	public int size() {
		return addresses.size();
	}
	
	/**
	 * adds a {@link ServiceAddress} to the cache with the given status if it is not already present or updates the status otherwise
	 * and returns the corresponding {@link CachedServiceAddress} instance
	 * @param serviceAddress the {@link ServiceAddress} to add
	 * @param originCachedServiceAddress the {@link CachedServiceAddress} representing the address from which the knowledge of the existence of the given {@link ServiceAddress} originated
	 * @param status the status with which to create the new {@link CachedServiceAddress} instance or with which to update the existing {@link CachedServiceAddress} instance with
	 * @return the corresponding {@link CachedServiceAddress} instance for the given {@link ServiceAddress}
	 */
	public synchronized CachedServiceAddress addServiceAddress(ServiceAddress serviceAddress, CachedServiceAddress originCachedServiceAddress, int status) {		
		byte[] data = serviceAddress.getData();
		final int tableIndex = data.length > 0 ? 0xFF&data[data.length-1] : 0;
		final int l1Index = data.length > 1 ? 0xFF&data[data.length-2] : 0;
		LinkedList[] l1 = table[tableIndex];
		if(l1 == null) l1 = table[tableIndex] = new LinkedList[256];
		LinkedList l2 = l1[l1Index];
		if(l2 == null) l2 = l1[l1Index] = new LinkedList();
		else {
			for(Object o : l2) {
				CachedServiceAddress cachedServiceAddress = (CachedServiceAddress) o;
				if(cachedServiceAddress.serviceAddress.equals(serviceAddress)) {
					setStatus(cachedServiceAddress, status, status);
					return cachedServiceAddress;
				}
			}
		}
		CachedServiceAddress cachedServiceAddress = new CachedServiceAddress(serviceAddress, originCachedServiceAddress, status);
		l2.add(cachedServiceAddress);
		addresses.add(cachedServiceAddress);
		return cachedServiceAddress;
	}
	
	/**
	 * adds a {@link ServiceAddress} to the cache with the given status if it is not already present or updates the status otherwise
	 * and returns the corresponding {@link CachedServiceAddress} instance
	 * @param serviceAddress the {@link ServiceAddress} to add
	 * @param status the status with which to create the new {@link CachedServiceAddress} instance or with which to update the existing {@link CachedServiceAddress} instance with
	 * @return the corresponding {@link CachedServiceAddress} instance for the given {@link ServiceAddress}
	 */
	public CachedServiceAddress addServiceAddress(ServiceAddress serviceAddress, int status) {
		return addServiceAddress(serviceAddress, null, status);
	}
	
	/**
	 * tries to find the corresponding {@link CachedServiceAddress} instance for the given {@link ServiceAddress}, returning it or null if not found
	 * @param serviceAddress the {@link ServiceAddress} to find the corresponding {@link CachedServiceAddress} instance for
	 * @return the corresponding {@link CachedServiceAddress} instance or null if it was not found
	 */
	private synchronized CachedServiceAddress findCachedServiceAddress(ServiceAddress serviceAddress) {
		byte[] data = serviceAddress.getData();
		LinkedList[] l1 = table[data.length > 0 ? 0xFF&data[data.length-1] : 0];
		if(l1 == null) return null;
		LinkedList l2 = l1[data.length > 1 ? 0xFF&data[data.length-2] : 0];
		if(l2 == null) return null;
		for(Object o : l2) {
			CachedServiceAddress serviceAddressStatus = (CachedServiceAddress) o;
			if(serviceAddressStatus.serviceAddress.equals(serviceAddress)) return serviceAddressStatus;
		}
		return null;
	}
	
	/**
	 * modifies the status of the corresponding {@link CachedServiceAddress} of the given {@link ServiceAddress}
	 * @param serviceAddress the {@link ServiceAddress} whose corresponding {@link CachedServiceAddress}'s status to modify
	 * @param statusMask a binary mask specifying the area to overwrite (set bits to 1 to overwrite, to 0 otherwise)
	 * @param status the bits to set (note that only bits inside statusMask are applied)
	 */
	public synchronized void setStatus(ServiceAddress serviceAddress, int statusMask, int status) {
		CachedServiceAddress cachedServiceAddress = findCachedServiceAddress(serviceAddress);
		setStatus(cachedServiceAddress, statusMask, status);
	}
	
	/**
	 * modifies the status of the given {@link CachedServiceAddress}
	 * @param cachedServiceAddress the {@link CachedServiceAddress} whose status to modify
	 * @param statusMask a binary mask specifying the area to overwrite (set bits to 1 to overwrite, to 0 otherwise)
	 * @param status the bits to set (note that only bits inside statusMask are applied)
	 * @return true if the given {@link CachedServiceAddress} is reachable for the first time, false otherwise
	 */
	public boolean setStatus(CachedServiceAddress cachedServiceAddress, int statusMask, int status) {
		boolean reachableStatus = cachedServiceAddress.hasStatus(STATUS_REACHABLE, STATUS_REACHABLE);
		
		boolean firstTimeReachable = false;
		
		cachedServiceAddress.status = (cachedServiceAddress.status & (~statusMask)) | status;
		
		long now = System.currentTimeMillis();
		
		if((statusMask & STATUS_PING) == STATUS_PING) {
			int pingStatus = (status & STATUS_PING);
			if(pingStatus == STATUS_PING_SENT) {
				cachedServiceAddress.lastPing = now;
				
			} else if(pingStatus == STATUS_PING_SUCCESSFUL && !reachableStatus) {
				// address is reachable now
				Log.debug(Log.PART_ADDRESSCACHE_SETSTATUS, this, String.format("adding CachedServiceAddress %s to reachableAddresses", cachedServiceAddress.toString()));
				cachedServiceAddress.lastPing = System.currentTimeMillis();
				reachableAddresses.add(cachedServiceAddress);
				cachedServiceAddress.status |= STATUS_REACHABLE;
				firstTimeReachable = cachedServiceAddress.neverReachable;
				cachedServiceAddress.neverReachable = false;
				
			} else if(pingStatus == STATUS_PING_TIMEOUT && reachableStatus) {
				// address was reachable before but is no longer
				Log.debug(Log.PART_ADDRESSCACHE_SETSTATUS, this, String.format("removing CachedServiceAddress %s from reachableAddresses", cachedServiceAddress.toString()));
				reachableAddresses.remove(cachedServiceAddress);
				cachedServiceAddress.status &= ~STATUS_REACHABLE;
				cachedServiceAddress.status &= ~STATUS_REACHABLEFROMREACHABLE;
				cachedServiceAddress.status &= ~STATUS_RECV;
				
			}
		}
		
		if((statusMask & STATUS_RECV) == STATUS_RECV && (status & STATUS_RECV) != 0) {
			cachedServiceAddress.lastRecv = now;
		}
		
		return firstTimeReachable;
	}
	
	/**
	 * returns a boolean indicating whether the given {@link CachedServiceAddress} has the reachable status set (i.e. whether the service instance running at that address is reachable)
	 * @param cachedServiceAddress the {@link CachedServiceAddress} whose reachable status to check
	 * @return true if the given {@link CachedServiceAddress} is reachable, false otherwise
	 */
	public boolean isReachable(CachedServiceAddress cachedServiceAddress) {
		return cachedServiceAddress.hasStatus(STATUS_REACHABLE, STATUS_REACHABLE);
	}

	/**
	 * returns the next known {@link CachedServiceAddress} matching the required status, starting from the given {@link AddressCacheCursor}'s position
	 * @param cursor the {@link AddressCacheCursor} whose position to use as a starting point for searching
	 * @param statusMask a binary mask specifying the area of a {@link CachedServiceAddress}'s status that has to equal the given status
	 * @param status the status that a {@link CachedServiceAddress}'s status has to equal inside of the given statusMask
	 * @return the next known {@link CachedServiceAddress} matching the required status, starting from the given {@link AddressCacheCursor}'s position
	 */
	public synchronized CachedServiceAddress get(AddressCacheCursor cursor, int statusMask, int status) {
		int index = cursor.get();
		final int size = addresses.size();
		if(index >= size) index = 0;
		CachedServiceAddress cachedServiceAddress;
		for(int i, i_ = 0; i_ < size; i_++) {
			i = (index + i_) % size;
			if((cachedServiceAddress = addresses.get(i)).hasStatus(statusMask, status)) {
				cursor.set(i+1);
				return cachedServiceAddress;
			}
		}
		cursor.set(0);
		return null;
	}
	
	/**
	 * returns the next reachable {@link CachedServiceAddress} to be pinged, starting from the given {@link AddressCacheCursor}'s position
	 * @param cursor the {@link AddressCacheCursor} whose position to use as a starting point for searching
	 * @return returns the next reachable {@link CachedServiceAddress} to be pinged, starting from the given {@link AddressCacheCursor}'s position
	 */
	public synchronized CachedServiceAddress getReachableAddressToPing(AddressCacheCursor cursor) {
		int index = cursor.get();
		final int size = reachableAddresses.size();
		if(index >= size) index = 0;
		CachedServiceAddress cachedServiceAddress;
		final long now = System.currentTimeMillis(), maxLastPing = now - 5000;
		for(int i, i_ = 0; i_ < size; i_++) {
			i = (index + i_) % size;
			if((cachedServiceAddress = reachableAddresses.get(i)).lastPing < maxLastPing) {
				cursor.set(i+1);
				return cachedServiceAddress;
			}
		}
		cursor.set(0);
		return null;
	}
	
	/**
	 * copies all {@link CachedServiceAddress}es in the reachable address list into an array and returns it
	 * @return a newly created array of {@link CachedServiceAddress}es, containing all {@link CachedServiceAddress}es that are inside the reachable address list at the point of execution
	 */
	public synchronized CachedServiceAddress[] getReachableAddresses() {
		CachedServiceAddress[] reachableAddresses = new CachedServiceAddress[this.reachableAddresses.size()];
		this.reachableAddresses.toArray(reachableAddresses);
		return reachableAddresses;
	}
	
	/**
	 * returns the next non-reachable {@link CachedServiceAddress} to be pinged, starting from the given {@link AddressCacheCursor}'s position
	 * @param cursor the {@link AddressCacheCursor} whose position to use as a starting point for searching
	 * @return returns the next non-reachable {@link CachedServiceAddress} to be pinged, starting from the given {@link AddressCacheCursor}'s position
	 */
	public synchronized CachedServiceAddress getNonReachableAddressToPing(AddressCacheCursor cursor) {
		int index = cursor.get();
		final int size = addresses.size();
		if(index >= size) index = 0;
		CachedServiceAddress cachedServiceAddress;
		final long now = System.currentTimeMillis(), maxLastPing = now - 20000;
		for(int i, i_ = 0; i_ < size; i_++) {
			i = (index + i_) % size;
			if((cachedServiceAddress = addresses.get(i)).hasStatus(STATUS_REACHABLE, 0) && cachedServiceAddress.lastPing < maxLastPing) {
				cursor.set(i+1);
				return cachedServiceAddress;
			}
		}
		cursor.set(0);
		return null;
	}
	
	/**
	 * returns a {@link LinkedList} containing known {@link CachedServiceAddress}es matching the required status
	 * @param limit return at most this many addresses
	 * @param offset skip this many addresses matching the required status
	 * @param statusMask a binary mask specifying the area of a {@link CachedServiceAddress}'s status that has to equal the given status
	 * @param status the status that a {@link CachedServiceAddress}'s status has to equal inside of the given statusMask
	 * @return a {@link LinkedList} containing known {@link CachedServiceAddress}es matching the required status
	 */
	public synchronized List<CachedServiceAddress> get(long limit, long offset, int statusMask, int status) {
		LinkedList<CachedServiceAddress> addrs = new LinkedList<CachedServiceAddress>();
		final int size = addresses.size();
		if(size <= 0) return addrs;
		int found = 0, added = 0;
		offset %= (long)size;
		CachedServiceAddress cachedServiceAddress;
		for(int i = 0; i < size; i++) {
			if((cachedServiceAddress = addresses.get(i)).hasStatus(statusMask, status)) {
				found++;
				if(found > offset) {
					addrs.add(cachedServiceAddress);
					added++;
					if(added >= limit) break;
				}
			}
		}
		return addrs;
	}
	
	/**
	 * returns a new {@link LinkedList} of reachable {@link CachedServiceAddress}es
	 * @param limit return at most this many addresses
	 * @param offset skip this many addresses
	 * @return a {@link LinkedList} of reachable {@link CachedServiceAddress}es
	 */
	public synchronized List<CachedServiceAddress> getReachableAddresses(long limit, long offset) {
		LinkedList<CachedServiceAddress> addrs = new LinkedList<CachedServiceAddress>();
		final int size = reachableAddresses.size();
		if(size <= 0) return addrs;
		offset %= (long)size;
		for(int i = 0; i < size; i++) {
			if(i >= offset) {
				if(i >= (offset+limit)) break;
				addrs.add(reachableAddresses.get(i));
			}
		}
		return addrs;
	}
}
