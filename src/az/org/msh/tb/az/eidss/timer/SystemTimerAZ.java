package org.msh.tb.az.eidss.timer;

import java.util.Date;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.msh.tb.az.eidss.EidssIntHome;

/**
 * System timer responsible for generating events in a regular basis (it's intended to raise events twice a day)
 * @author Ricardo Memoria
 *
 */
@Name("systemTimerAZ")
public class SystemTimerAZ {
	/**
	 * Called asynchronously to trigger system events that must be executed regularly
	 * @param initDelay
	 * @param interval
	 */
	@Asynchronous
	public QuartzTriggerHandle trigger(@Expiration Date initDelay, @IntervalDuration long interval) {
		EidssIntHome eidssIntHome = (EidssIntHome) Component.getInstance("eidssIntHome");
		if (eidssIntHome.getTask()!=null)
			eidssIntHome.getTask().cancel();
		eidssIntHome.rewriteDatesInConfig();
		eidssIntHome.execute();
		return null;
	}
}
