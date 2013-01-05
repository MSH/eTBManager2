package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.treatment.TreatmentsInfoHome;

@Name("treatmentsInfoUAHome")
public class TreatmentInfoUAHome {
	
	/**
	 * Return the percentage progress of the treatment
	 * instead of getPlannedProgress() in {@link TreatmentsInfoHome.TreatmentInfo}
	 * @return
	 */
	public Double getPlannedProgress(TreatmentsInfoHome.TreatmentInfo ti) {
		double res = (double)ti.getNumDaysDone()/(double)ti.getNumDaysPlanned();
		return res*100;
	}
	
	/**
	 * instead of getProgressPoints() in {@link TreatmentsInfoHome.TreatmentInfo}
	 * @return
	 */
	public Integer getProgressPoints(TreatmentsInfoHome.TreatmentInfo ti) {
		Double val = getPlannedProgress(ti);
		if ((val == null) || (val == 0))
			return 0;
		return (val > 100? 100: val.intValue());
	}
	
}
