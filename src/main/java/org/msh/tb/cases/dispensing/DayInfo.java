package org.msh.tb.cases.dispensing;

import org.jboss.seam.core.Locale;
import org.msh.tb.entities.enums.TreatmentDayOption;

import java.text.DateFormatSymbols;

/**
 * Hold temporary information during editing about a day of treatment 
 * @author Ricardo Memoria
 *
 */
public class DayInfo {
	private TreatmentDayOption value;
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
		return (value == TreatmentDayOption.DOTS) || (value == TreatmentDayOption.SELF_ADMIN);
	}
	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		value = checked? TreatmentDayOption.DOTS: TreatmentDayOption.NOT_TAKEN;
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

	/**
	 * @return the value
	 */
	public TreatmentDayOption getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(TreatmentDayOption value) {
		this.value = value;
	}

}
