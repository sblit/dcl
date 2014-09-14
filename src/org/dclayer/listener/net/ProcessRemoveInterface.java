package org.dclayer.listener.net;

import org.dclayer.net.process.template.Process;

/**
 * used to allow {@link Process} removal by another {@link Process}
 */
public interface ProcessRemoveInterface {
	/**
	 * removes a Process that is requested to by removed by another Process
	 * @param originalProcess the Process that requests the removal
	 * @param removeProcess the Process that should be removed
	 */
	public void removeProcess(Process originalProcess, Process removeProcess);
}
