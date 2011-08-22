package org.msh.tb.medicines.dispensing;

import java.util.Calendar;

import org.jboss.seam.annotations.Name;

/**
 * Display information about dispensing for patients 
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingView")
public class CaseDispensingView {

	private Integer month;
	private Integer year;


	/**
	 * Initialize the month and year with the current month/year
	 */
	public void initMonthYear() {
		Calendar c = Calendar.getInstance();
		month = c.get(Calendar.MONTH);
		year = c.get(Calendar.YEAR);
	}

	/**
	 * @return the month
	 */
	public Integer getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(Integer month) {
		this.month = month;
	}

	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}
}
