package org.dclayer.exception.net.parse;

/**
 * an Exception which is thrown when a malformed network descriptor is received
 */
public class MalformedNetworkDescriptorException extends ParseException {
	
	/**
	 * creates a new {@link MalformedNetworkDescriptorException}
	 * @param descriptor the invalid network descriptor that was received
	 */
	public MalformedNetworkDescriptorException(String descriptor) {
		super(String.format("Malformed network descriptor: %s", descriptor));
	}
	
}
