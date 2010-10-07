package org.msh.tb.application;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Duration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.core.Events;

/**
 * System timer responsible for generating events in a regular basis (it's intended to raise events twice a day)
 * @author Ricardo Memoria
 *
 */
@Name("systemTimer")
public class SystemTimer {

	/**
	 * Called asynchronously to trigger system events that must be executed regularly
	 * @param initDelay
	 * @param interval
	 */
	@Asynchronous
	public void trigger(@Duration long initDelay, @IntervalDuration long interval) {
		Events.instance().raiseEvent("system-timer-event");
	}
}
