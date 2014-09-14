package org.dclayer.net.addresscache;

/**
 * a cursor specifying a position inside an address list of an {@link AddressCache}
 */
public class AddressCacheCursor {
	/**
	 * the current position of the cursor
	 */
	private int index;
	
	/**
	 * creates a new {@link AddressCacheCursor}, setting the position to the given index
	 * @param index the initial position of the cursor
	 */
	public void set(int index) {
		this.index = index;
	}
	
	/**
	 * returns the current position of the cursor
	 * @return the current position of the cursor
	 */
	public int get() {
		return index;
	}
}
