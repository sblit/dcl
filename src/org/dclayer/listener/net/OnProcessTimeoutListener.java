package org.dclayer.listener.net;

import org.dclayer.net.process.template.Process;

/**
 * used to execute callbacks upon timeout of {@link Process}es
 */
public interface OnProcessTimeoutListener {
	/**
	 * Process timeout callback, called by ProcessTimeoutQueue upon timeout of a Process
	 * @param process the Process that timed out
	 */
	public void onProcessTimeout(Process process);
}
