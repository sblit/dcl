package org.dclayer.net.component;

import java.util.Collection;
import java.util.Iterator;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.PacketComponentI;
import org.dclayer.net.buf.ByteBuf;

public abstract class ArrayPacketComponent<T extends PacketComponentI> extends PacketComponent implements Iterable<T> {
	
	private class Element {
		T packetComponent = newElementPacketComponent();
		Element next;
		boolean active;
	}
	
	private class ChainIterator implements Iterator<T> {
		private Element current;
		
		@Override
		public boolean hasNext() {
			return current != null && current.active;
		}
		
		@Override
		public T next() {
			Element element = current;
			current = current.next;
			return element.packetComponent;
		}

		@Override
		public void remove() {}
		
		public void reset() {
			current = chain;
		}
	}
	
	//
	
	private Element chain;
	private ChainIterator chainIterator = new ChainIterator();
	
	private FlexNum elementsFlexNum = new FlexNum(0, Integer.MAX_VALUE);
	private Iterator<T> elementIterator = null;
	
	protected abstract T newElementPacketComponent();

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		
		elementsFlexNum.read(byteBuf);
		
		final int num = (int) elementsFlexNum.getNum();
		Element element = null;
		
		for(int i = 0; i < num; i++) {
			
			if(element == null) {
				if(chain == null) {
					chain = new Element();
				}
				element = chain;
			} else {
				if(element.next == null) {
					element.next = new Element();
				}
				element = element.next;
			}
			
			element.active = true;
			element.packetComponent.read(byteBuf);
			
		}
		
		if(element.next != null) {
			element.next.active = false;
		}
		
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		
		elementsFlexNum.write(byteBuf);
		
		while(elementIterator.hasNext()) {
			elementIterator.next().write(byteBuf);
		}
		
	}

	@Override
	public int length() {
		int elementsLength = 0;
		while(elementIterator.hasNext()) {
			elementsLength += elementIterator.next().length();
		}
		return elementsFlexNum.length() + elementsLength;
	}

	@Override
	public PacketComponentI[] getChildren() {
		return null; // TODO
	}

	@Override
	public String toString() {
		return String.format("ArrayPacketComponent(numElements=%d)", elementsFlexNum.getNum());
	}

	@Override
	public Iterator<T> iterator() {
		chainIterator.reset();
		return chainIterator;
	}
	
	public int getNumElements() {
		return (int) elementsFlexNum.getNum();
	}
	
	public void setElements(Collection<T> elements) {
		this.elementsFlexNum.setNum(elements.size());
		this.elementIterator = elements.iterator();
	}
	
}
