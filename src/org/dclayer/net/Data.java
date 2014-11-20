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
	 * resets offset relative to the current offset and sets length value
	 * @param relativeOffset the relative offset specifying where the data starts in the byte array, relative to the current offset
	 * @param length the length specifying how long the usable area of the byte array is
	 */
	public void relativeReset(int relativeOffset, int length) {
		reset(offset+relativeOffset, length);
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
	
	public long getBits(int bitOffset, int length) {
		long bits = 0;
		for(int i = 0; i < length;) {
			
			int cBitIndex = bitOffset + i;
			int cOff = (cBitIndex % 8);
			int cNum = Math.min(8 - cOff, length - i);
			
			byte b = this.data[cBitIndex/8];
			
			bits <<= cNum;
			bits |= (b & (0xFF >> cOff)) >> (8-cOff-cNum);
			
			i += cNum;
			
		}
		return bits;
	}
	
	public void setBits(int bitOffset, int length, long bits) {
		for(int i = 0; i < length;) {
			
			int cBitIndex = bitOffset + i;
			int cOff = (cBitIndex % 8);
			int cNum = Math.min(8 - cOff, length - i);
			
//			System.out.println(String.format("  cBitIndex=%d cOff=%d cNum=%d", cBitIndex, cOff, cNum));
			
			byte mask = (byte)(((1 << cNum)-1) << (8 - cNum - cOff));
			byte b = (byte)((bits >> (length-i-cNum)) << (8 - cNum - cOff));
//			System.out.println(String.format("  mask=%8s b=%8s", Integer.toBinaryString(mask & 0xFF), Integer.toBinaryString(b & 0xFF)));
			
			this.data[cBitIndex/8] &= ~mask;
			this.data[cBitIndex/8] |= (b & mask);
			
			i += cNum;
			
		}
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
		if(index < 0) index += length;
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
	
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof Data)) return false;
		Data data = (Data) object;
		if(this.length != data.length) return false;
		for(int i = 0; i < length; i++) {
			if(this.data[i+this.offset] != data.data[i+data.offset]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean equals(int thisOffset, Data otherData, int otherOffset, int length) {
		if(this.length < (thisOffset+length) || otherData.length < (otherOffset+length)) return false;
		for(int i = 0; i < length; i++) {
			if(this.data[i+this.offset+thisOffset] != otherData.data[i+otherData.offset+otherOffset]) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for(int i = 0; i < length; i++) {
			hash ^= (data[i+offset] << 8*(i % 4));
		}
		return hash;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder(this.length() * 2);
		byte nibble;
		for(int i = 0; i < length; i++) {
			byte b = data[i+offset];
			nibble = (byte)((b >> 4) & 0xF);
			stringBuilder.append((char)((nibble < 10) ? ('0'+nibble) : ('a'+(nibble-10))));
			nibble = (byte)(b & 0xF);
			stringBuilder.append((char)((nibble < 10) ? ('0'+nibble) : ('a'+(nibble-10))));
		}
		return stringBuilder.toString();
	}
	
	public void parse(String string) {
		int strlen = string.length();
		int length = (strlen+1)/2;
		String chars = string.toLowerCase();
		prepare(length);
		for(int i = 0; i < strlen; i++) {
			int c = chars.charAt(i);
			byte b;
			if('a' <= c && c <= 'f') {
				b = (byte)(c - 'a' + 10);
			} else {
				b = (byte)((c - '0') % 10);
			}
			int index = (i/2)+offset;
			if((i%2) == 0) {
				data[index] = 0;
			} else {
				data[index] <<= 4;
			}
			data[index] |= b;
		}
	}
	
}
