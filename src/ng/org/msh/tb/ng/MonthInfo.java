package org.msh.tb.ng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.msh.tb.ng.entities.enums.Dot;
import org.msh.utils.date.DateUtils;



/**
 * Stores information about the treatment in a specific month of the treatment 
 * @author Ricardo Lima
 *
 */
public class MonthInfo {

	private int month;
	private int year;
	private List<DayInfo> days = new ArrayList<DayInfo>();
	private int dispensingDays;
	private int dayIni;
	private int dayEnd;

	/**
	 * Recreate the list of days based on the selected month/year
	 */
	public void refreshDays(Date dtIni, Date dtEnd) {
		days.clear();
		if (year == 0)
			return;

		int daysInMonth = DateUtils.daysInAMonth(year, month);

		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.MONTH, month);
		c.set(Calendar.YEAR, year);
		
		dayIni = 1;
		dayEnd = 31;
		for (int i = 1; i <= 31; i++) {
			DayInfo day = new DayInfo(this);

			// check if the day is in use
			if (i > daysInMonth)
				day.setNotused(true);
			else {
				c.set(Calendar.DAY_OF_MONTH, i);
				if ((c.getTime().before(dtIni)) || (c.getTime().after(dtEnd)))
					day.setNotused(true);
			}
			
			if ((dayIni == i) && (day.isNotused()))
				dayIni++;
			if (!day.isNotused())
				dayEnd = i;
			days.add(day);
		}
	}


	/**
	 * Returns the number of days prescribed to the patient in the month/year of treatment
	 * @return Number of days prescribed
	 */
	public int getTotalPrescribed() {
		int nd = 0;
		for (DayInfo di: days) {
			if (di.isPrescribed())
				nd++;
		}
		return nd;
	}


	/**
	 * Return the number of days of medicine dispensing to the patient in the month/year of treatment
	 * @return
	 */
	public int getTotalDispensed() {
		int nd = 0;
		for (DayInfo di: days) {
			if (di.getSelected()!= Dot.X)
				nd++;
		}
		return nd;
	}
	
	/**
	 * Returns the month of the treatment
	 * @return month of treatment
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Changes the month
	 * @param month
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * Returns the year
	 * @return year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Changes the year
	 * @param year
	 */
	public void setYear(int year) {
		this.year = year;
	}

	
	/**
	 * Returns array with a list of days of the month
	 * @return
	 */
	public List<DayInfo> getDays() {
		return days;
	}

	public void setDispensingDays(int Value) {
		this.dispensingDays = Value;
	}
	
	/**
	 * @return the dispensingDays
	 */
	public int getDispensingDays() {
		return dispensingDays;
	}

	/**
	 * @return the dayIni
	 */
	public int getDayIni() {
		return dayIni;
	}

	/**
	 * @param dayIni the dayIni to set
	 */
	public void setDayIni(int dayIni) {
		this.dayIni = dayIni;
	}

	/**
	 * @return the dayEnd
	 */
	public int getDayEnd() {
		return dayEnd;
	}

	/**
	 * @param dayEnd the dayEnd to set
	 */
	public void setDayEnd(int dayEnd) {
		this.dayEnd = dayEnd;
	}
}
