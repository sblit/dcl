package org.dclayer.net.llacache;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.dclayer.datastructure.tree.ParentTreeNode;
import org.dclayer.net.Data;

/**
 * Maps lower-level addresses to {@link CachedLLA} objects
 */
public class LLACache {
	
	/**
	 * {@link ArrayList} holding all known addresses
	 */
	private ArrayList<CachedLLA> addresses = new ArrayList<CachedLLA>();
	
//	/**
//	 * array of trees used to quickly find IP and port {@link CachedLLA}s
//	 */
//	private ParentTreeNode<CachedLLA>[] ipPortTrees = new ParentTreeNode[16];
	/**
	 * tree used to quickly find IP and port {@link CachedLLA}s
	 */
	private ParentTreeNode<CachedLLA> addressTree = new ParentTreeNode<>(0);
	
//	/**
//	 * array of byte arrays used to write IP and port into for usage as key inside the tree
//	 */
//	private byte[][] addressBufs = new byte[16][];
	/**
	 * {@link Data} used to write address into for usage as key inside the tree
	 */
	private Data addressBufData = new Data();
	
	public int size() {
		return addresses.size();
	}
	
	public synchronized CachedLLA getCachedLLA(LLA lla, boolean create) {
		
		Data addressData = lla.getData();
		
		CachedLLA cachedLLA = addressTree.get(addressData);
		
		if(cachedLLA == null && create) {
			cachedLLA = new CachedLLA(lla);
			addressTree.put(addressData, cachedLLA);
			addresses.add(cachedLLA);
		}
		
		return cachedLLA;
		
	}
	
	public synchronized CachedLLA getIPPortCachedLLA(InetAddress inetAddress, int port, boolean create) {
		
//		byte[] ipAddress = inetAddress.getAddress();
//		
//		if(ipAddress.length >= ipPortTrees.length) ipPortTrees = Arrays.copyOf(ipPortTrees, ipAddress.length + 1);
//		ParentTreeNode<CachedLLA> tree = ipPortTrees[ipAddress.length];
//		if(tree == null) tree = ipPortTrees[ipAddress.length] = new ParentTreeNode<CachedLLA>(0);
//		
//		if(ipAddress.length >= addressBufs.length) addressBufs = Arrays.copyOf(addressBufs, ipAddress.length + 1);
//		byte[] addressBuf = addressBufs[ipAddress.length];
//		if(addressBuf == null) addressBuf = addressBufs[ipAddress.length] = new byte[ipAddress.length + 2]; // 2 bytes for the port number
//		
//		System.arraycopy(ipAddress, 0, addressBuf, 0, ipAddress.length);
//		addressBuf[addressBuf.length - 2] = (byte)((port >> 8) & 0xFF);
//		addressBuf[addressBuf.length - 1] = (byte)(port & 0xFF);
//		
//		CachedLLA cachedLLA = tree.get(addressBuf);
		
		InetSocketLLA.serialize(inetAddress, port, addressBufData);
		
		CachedLLA cachedLLA = addressTree.get(addressBufData);
		
		if(cachedLLA == null && create) {
			LLA lla = new InetSocketLLA(inetAddress, port);
			cachedLLA = new CachedLLA(lla);
			addressTree.put(addressBufData, cachedLLA);
			addresses.add(cachedLLA);
		}
		
		return cachedLLA;
		
	}
	
	/**
	 * returns a random selection of at max limit cached lower-level addresses.
	 * duplicate LLAs might be returned.
	 * @param limit how much LLAs to return at max
	 * @return a random selection of cached lower-level addresses. duplicate LLAs might be returned.
	 */
	public synchronized List<CachedLLA> getRandomCachedLLAs(int limit) {
		final int size = addresses.size();
		if(limit > size) limit = size;
		ArrayList<CachedLLA> list = new ArrayList<CachedLLA>(limit);
		for(int i = 0; i < limit; i++) {
			list.add(addresses.get((int)(Math.random() * size)));
		}
		return list;
	}
	
	/**
	 * returns a random selection of at max limit lower-level addresses.
	 * duplicate LLAs might be returned.
	 * @param limit how much LLAs to return at max
	 * @return a random selection of lower-level addresses. duplicate LLAs might be returned.
	 */
	public synchronized List<LLA> getRandomLLAs(int limit) {
		final int size = addresses.size();
		if(limit > size) limit = size;
		ArrayList<LLA> list = new ArrayList<LLA>(limit);
		for(int i = 0; i < limit; i++) {
			list.add(addresses.get((int)(Math.random() * size)).getLLA());
		}
		return list;
	}
}
