package org.msh.tb.workspaces.customizable;

import java.util.Date;

import org.msh.tb.entities.TbCase;

public class ExamControl {

	/**
	 * Returns a key related to the system messages to display the month
	 * @param tbcase object representing the TB Case
	 * @param dt reference date for the month display
	 * @return
	 */
	public String getMonthDisplay(TbCase tbcase, Date dt) {
		Integer num = tbcase.getMonthTreatment(dt);
		
		if (num > 0) {
			return "global.monthth";  //Integer.toString(num);
		}
		
		Date dtReg = tbcase.getRegistrationDate();
		
		if ((dtReg == null) || (!dt.before(dtReg)))
			return "cases.exams.zero";
		else return "cases.exams.prevdt";
	}
}
