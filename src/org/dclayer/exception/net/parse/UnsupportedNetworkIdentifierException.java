package org.dclayer.exception.net.parse;

/**
 * an Exception which is thrown when an unknown network identifier is received
 */
public class UnsupportedNetworkIdentifierException extends ParseException {
	
	/**
	 * creates a new {@link UnsupportedNetworkIdentifierException}
	 * @param identifier the invalid network identifier that was received
	 */
	public UnsupportedNetworkIdentifierException(String identifier) {
		super(String.format("Unsupported network identifier: %s", identifier));
	}
	
}
