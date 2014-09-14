package org.dclayer.exception.net.parse;

/**
 * an Exception which is thrown when parsing fails
 */
public class ParseException extends Exception {
	
	/**
	 * creates a new ParseException
	 * @param msg the error message
	 */
	public ParseException(String msg) {
		super(msg);
	}
	
}