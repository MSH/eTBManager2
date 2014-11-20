package org.msh.tb.workspaces.customizable;

import org.jboss.seam.core.Locale;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;

import java.text.MessageFormat;
import java.util.Date;

public class ExamControl {

	/**
	 * Returns a key related to the system messages to display the month
	 * @param tbcase object representing the TB Case
	 * @param dt reference date for the month display
	 * @return String value to be displayed to the user
	 */
	public String getMonthDisplay(TbCase tbcase, Date dt) {
		if (dt == null)
			return null;

		Integer num = tbcase.getMonthTreatment(dt);
		
		if (num > 0) {
			String s = Integer.toString(num);

			// if language is English, so handle it to display the ordinal suffix of the language
			if ("en".equals(Locale.instance().getLanguage())) {
				switch (num >= 11 && num <= 13? 0: num % 10) {
				case 1:
					s += "st";
					break;
				case 2:
					s += "nd";
					break;
				case 3:
					s += "rd";
					break;

				default:
					s += "th";
					break;
				}
			}

			return MessageFormat.format( Messages.instance().get("global.monthth"), s);
		}
		
		Date dtReg = tbcase.getDiagnosisDate();
		
		if ((dtReg == null) || (!dt.before(dtReg)))
			return Messages.instance().get("cases.exams.zero");
		else return Messages.instance().get("cases.exams.prevdt");
	}
}
