package org.dclayer.exception.net.parse;

/**
 * an Exception which is thrown when attempting to parse a ServiceAddress from invalid data
 */
public class InvalidServiceAddressException extends ParseException {
	
	/**
	 * creates an {@link InvalidServiceAddressException}
	 */
	public InvalidServiceAddressException() {
		super("Invalid service address");
	}
	
}
