package org.msh.tb.cases.dispensing;

import java.text.DateFormatSymbols;

import org.jboss.seam.core.Locale;

/**
 * Hold temporary information during editing about a day of treatment 
 * @author Ricardo Memoria
 *
 */
public class DayInfo {
	private boolean checked;
	private boolean available;
	private Integer day;
	private int weekDay;

	
	public String getShortWeekname() {
		String[] shortWeekDays = new DateFormatSymbols(Locale.instance()).getShortWeekdays();
		return shortWeekDays[weekDay];
	}

	public boolean isDayOfMonth() {
		return (day != null) && (day > 0);
	}
	
	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}
	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	/**
	 * @return the available
	 */
	public boolean isAvailable() {
		return available;
	}
	/**
	 * @param available the available to set
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}
	/**
	 * @return the day
	 */
	public Integer getDay() {
		return day;
	}
	/**
	 * @param day the day to set
	 */
	public void setDay(Integer day) {
		this.day = day;
	}

	/**
	 * @return the weekDay
	 */
	public int getWeekDay() {
		return weekDay;
	}

	/**
	 * @param weekDay the weekDay to set
	 */
	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

}
