package org.msh.tb.medicines.dispensing;

import java.util.Calendar;
import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.msh.utils.date.DateUtils;


@Name("dispensingView")
public class DispensingView {

	private Integer month;
	private Integer year;

	/**
	 * Initialize the month and year 
	 */
	public void initializeMonthYear() {
		if (month == null) {
			Calendar c = Calendar.getInstance();
			month = c.get(Calendar.MONTH);
			year = c.get(Calendar.YEAR);			
		}
	}

	
	public boolean isValidMonth() {
		if ((month == null) || (year == null))
			return false;
		Date today = new Date();
		int m = DateUtils.monthOf( today );
		int y = DateUtils.yearOf( today );
		
		if ((y > year) || ((y == year) && (m > month)))
			return false;

		return true;
	}
	
	
	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

}
