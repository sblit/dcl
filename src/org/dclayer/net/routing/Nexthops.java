package org.dclayer.net.routing;

import java.util.Iterator;

public class Nexthops<T> implements Iterable<ForwardDestination<T>> {
	
	private Nexthops<T> next;
	private ForwardDestination<T> forwardDestination;
	
	public Nexthops() {
	}
	
	public Nexthops(ForwardDestination<T> forwardDestination) {
		this.forwardDestination = forwardDestination;
	}
	
	public void append(Nexthops<T> nexthops) {
		if(nexthops == this) return; // no loops
		if(next == null) {
			this.next = nexthops;
		} else {
			this.next.append(nexthops);
		}
	}
	
	public void append(ForwardDestination<T> forwardDestination) {
		this.append(new Nexthops<T>(forwardDestination));
	}
	
	public ForwardDestination<T> getForwardDestination() {
		return forwardDestination;
	}
	
	public boolean forward(T message) {
		boolean success = false;
		for(ForwardDestination<T> forwardDestination : this) {
			success |= forwardDestination.onForward(message);
		}
		return success;
	}

	@Override
	public Iterator<ForwardDestination<T>> iterator() {
		return new Iterator<ForwardDestination<T>>() {
			
			private Nexthops<T> current = Nexthops.this;
			
			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public ForwardDestination<T> next() {
				ForwardDestination<T> forwardDestination = current.forwardDestination;
				current = current.next;
				return forwardDestination;
			}

			@Override
			public void remove() {
				
			}
			
		};
	}
	
}
