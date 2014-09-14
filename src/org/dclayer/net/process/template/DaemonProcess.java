package org.dclayer.net.process.template;

import org.dclayer.listener.net.FollowUpProcessSpawnInterface;

/**
 * base class for {@link Process}es that continuously run in an own Thread
 */
public abstract class DaemonProcess extends Process {
	
	/**
	 * A {@link FollowUpProcessSpawnInterface} used to spawn new {@link Process}es
	 */
	private FollowUpProcessSpawnInterface followUpProcessSpawnInterface;
	
	/**
	 * the {@link Thread} for this {@link DaemonProcess}
	 */
	private Thread thread = new Thread() {
		@Override
		public void run() {
			DaemonProcess.this.runDaemon();
		}
	};

	@Override
	protected int defineProperties() {
		return DAEMON;
	}
	
	@Override
	public final void start(FollowUpProcessSpawnInterface followUpProcessSpawnInterface) {
		this.followUpProcessSpawnInterface = followUpProcessSpawnInterface;
		thread.start();
	}
	
	/**
	 * this is called once inside the Thread of this {@link DaemonProcess}, work should be done here
	 */
	protected abstract void runDaemon();
	
	/**
	 * returns the {@link FollowUpProcessSpawnInterface} for this {@link DaemonProcess}
	 * @return the {@link FollowUpProcessSpawnInterface} for this {@link DaemonProcess}
	 */
	protected FollowUpProcessSpawnInterface getFollowUpProcessSpawnInterface() {
		return followUpProcessSpawnInterface;
	}
	
}
