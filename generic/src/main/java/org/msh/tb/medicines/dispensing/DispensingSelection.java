package org.msh.tb.medicines.dispensing;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.msh.utils.date.DateUtils;

import java.util.Calendar;
import java.util.Date;

@Name("dispensingSelection")
@AutoCreate
public class DispensingSelection {

	private Integer month;
	private Integer year;
	
	
	/**
	 * Initialize the month and year to the current date
	 */
	public void initialize() {
		if ((month != null) && (year != null))
			return;
		Calendar c = Calendar.getInstance();
		month = c.get(Calendar.MONTH);
		year = c.get(Calendar.YEAR);
	}
	
	/**
	 * Check if month and year are selected or if one of them is null
	 * @return
	 */
	public boolean isMonthYearSelected() {
		return (month != null) && (year != null);
	}


	/**
	 * Return the initial date with the first day of the month and year selected 
	 * @return
	 */
	public Date getIniDate() {
		if (!isMonthYearSelected())
			return null;
		return DateUtils.newDate(year, month, 1);
	}
	
	/**
	 * Return the final date with the last day of the month and year selected
	 * @return
	 */
	public Date getEndDate() {
		if (!isMonthYearSelected())
			return null;
		return DateUtils.newDate(year, month, DateUtils.daysInAMonth(year, month));
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
