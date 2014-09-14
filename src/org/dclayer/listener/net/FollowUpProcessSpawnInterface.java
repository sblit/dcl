package org.dclayer.listener.net;

import org.dclayer.net.process.template.Process;

/**
 * used to spawn {@link Process}es from another Process
 */
public interface FollowUpProcessSpawnInterface {
	/**
	 * adds a follow-up Process (a Process that is spawned by another Process)
	 * @param originalProcess the Process that spawns this follow-up Process
	 * @param followUpProcess the new Process to spawn
	 */
	public void addFollowUpProcess(Process originalProcess, Process followUpProcess);
}
