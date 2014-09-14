package org.dclayer.net;

/**
 * byte array wrapper
 */
public class Data {
	/**
	 * the byte array containing the data
	 */
	protected byte[] data;
	/**
	 * the offset specifying where the data starts in the byte array
	 */
	protected int offset;
	/**
	 * the length specifying how long the usable area of the byte array is
	 */
	protected int length;
	
	/**
	 * creates a new {@link Data} instance
	 * @param data the byte array to use
	 * @param offset the offset specifying where the data starts in the byte array
	 * @param length the length specifying how long the usable area of the byte array is
	 */
	public Data(byte[] data, int offset, int length) {
		reset(data, offset, length);
	}
	
	/**
	 * creates a new {@link Data} instance
	 * @param data the byte array to use
	 */
	public Data(byte[] data) {
		reset(data, 0, data.length);
	}
	
	/**
	 * creates a new Data instance, also newly creating the underlying byte array if length > 0
	 * @param length the length for the newly created byte array
	 */
	public Data(int length) {
		prepare(length);
	}
	
	/**
	 * creates a new, empty Data instance with no underlying byte array.
	 * This is the same as using Data(0).
	 */
	public Data() {
		this(0);
	}
	
	/**
	 * adapts the space in this {@link Data} to the given length value, enlarging the underlying byte array if necessary
	 * @param length the amount of bytes the space in this {@link Data} needs to be adapted to
	 */
	public void prepare(int length) {
		if(length > this.length) {
			reset(new byte[length], 0, length);
		} else {
			reset(0, length);
		}
	}
	
	/**
	 * resets offset and length values
	 * @param offset the offset specifying where the data starts in the byte array
	 * @param length the length specifying how long the usable area of the byte array is
	 */
	public void reset(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}
	
	/**
	 * resets the byte array and the offset and length values
	 * @param data the byte array to use
	 * @param offset the offset specifying where the data starts in the byte array
	 * @param length the length specifying how long the usable area of the byte array is
	 */
	public void reset(byte[] data, int offset, int length) {
		this.data = data;
		this.offset = offset;
		this.length = length;
	}
	
	/**
	 * returns the byte array containing the data
	 * @return the byte array containing the data
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * sets the byte array containing the data
	 * @param data the byte array containing the data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * @return the offset specifying where the data starts in the byte array
	 */
	public int offset() {
		return offset;
	}
	
	/**
	 * @return the length specifying how long the usable area of the byte array is
	 */
	public int length() {
		return length;
	}
	
	/**
	 * creates a new byte array and copies this {@link Data}'s contents into it
	 * @return the new byte array
	 */
	public byte[] copyToByteArray() {
		byte[] bytes = new byte[length];
		System.arraycopy(this.data, offset, bytes, 0, length);
		return bytes;
	}
	
	/**
	 * creates a new {@link Data} object with a new underlying byte array containing the same data as this {@link Data} object
	 * @return the new {@link Data} object
	 */
	public Data copy() {
		return new Data(copyToByteArray());
	}
	
	/**
	 * returns the byte at the specified index
	 * @param index the index of the byte to return
	 * @return the byte at index
	 */
	public byte getByte(int index) {
		return this.data[offset + index];
	}
	
	/**
	 * sets the byte at the specified index to the given byte
	 * @param index the index of the byte to set
	 * @param b the byte to set
	 * @return the byte at index
	 */
	public void setByte(int index, byte b) {
		this.data[offset + index] = b;
	}
	
	/**
	 * copies the specified amount of bytes from the specified position in the specified byte array to the specified position in this {@link Data} object
	 * @param index the position inside this {@link Data} object to copy the given bytes to
	 * @param bytes the byte array to copy bytes from
	 * @param offset the position in the given byte array to start copying from
	 * @param length the amount of bytes to copy from the given byte array
	 */
	public void setBytes(int index, byte[] bytes, int offset, int length) {
		System.arraycopy(bytes, offset, data, index, length);
	}
	
	/**
	 * copies the specified amount of bytes from the specified position in this {@link Data} object to the specified position in the given byte array
	 * @param index the position inside this {@link Data} object to copy the given bytes from
	 * @param length the amount of bytes to copy from this {@link Data} object
	 * @param bytes the byte array to copy bytes to
	 * @param offset the position in the given byte array to copy bytes to
	 */
	public void getBytes(int index, int length, byte[] bytes, int offset) {
		System.arraycopy(data, index, bytes, offset, length);
	}
}
