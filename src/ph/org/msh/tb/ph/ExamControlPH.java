package org.msh.tb.ph;

import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.workspaces.customizable.ExamControl;
import org.msh.utils.date.DateUtils;


/**
 * Extension of the {@link ExamControl} class for Philippines, where they use specific rules for 
 * displaying the month of treatment
 * @author Ricardo Memoria
 *
 */
@Name("examControl_ph")
@BypassInterceptors
public class ExamControlPH extends ExamControl {

	@Override
	public String getMonthDisplay(TbCase tbcase, Date dt) {
		Integer num = tbcase.getMonthTreatment(dt);
		
		if (num > 1) {
			return "global.monthth";  //Integer.toString(num);
		}
		
		if (tbcase.getTreatmentPeriod() == null)
			return "cases.exams.prevdt";
	
		Date dtTreat = tbcase.getTreatmentPeriod().getIniDate();
		if (dtTreat == null)
			return "cases.exams.prevdt";
		
		int days = DateUtils.daysBetween(dt, dtTreat);
		
		if (((dt.before(dtTreat)) && (days <= 30)) ||
			((dt.after(dtTreat)) && (days <= 7))) {
			return "cases.exams.zero";
		}
		
		if (dt.before(dtTreat))
			return "cases.exams.prevdt";
		else return "global.monthth";
	}

}
