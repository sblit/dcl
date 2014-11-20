package org.dclayer.net.a2s.rev35.component;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.IllegalCharacterException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.a2s.rev35.Rev35PacketComponent;
import org.dclayer.net.buf.ByteBuf;

public class NumberComponent extends Rev35PacketComponent {
	
	private int number;
	private int length;
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		
		number = 0;
		length = 0;
		byte b;
		while((b = byteBuf.read()) != ' ') {
			if(b < '0' || b > '9') {
				throw new IllegalCharacterException((char)b);
			}
			number *= 10;
			number += (int)(b - '0');
			length++;
		}
		
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		
		byteBuf.writeNonTerminatedString(Integer.toString(number));
		
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public String toString() {
		return String.format("NumberComponent(number=%d)", number);
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
		this.length = (int) Math.log10(number);
	}
	
}
