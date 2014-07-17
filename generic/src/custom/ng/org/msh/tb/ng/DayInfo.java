package org.msh.tb.ng;

import org.jboss.seam.core.Locale;
import org.msh.tb.ng.entities.enums.Dot;

import java.text.DateFormatSymbols;

/**
 * Stores information about an specific treatment day
 * @author Ricardo Lima
 *
 */
public class DayInfo {

	private boolean prescribed;
	private boolean dispensed;
	private boolean notused;
	private boolean treated;
	private Dot selected;
	
	private MonthInfo monthInfo;

	private boolean checked;
	private boolean available;
	private Integer day;
	private int weekDay;
	

	public DayInfo(MonthInfo monthInfo) {
		super();
		this.monthInfo = monthInfo;
	}

	
	public DayInfo() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * Returns if the day is in use or not (example, out of the treatment period)
	 * @return the notused
	 */
	public boolean isNotused() {
		return notused;
	}

	/**
	 * @param notused the notused to set
	 */
	public void setNotused(boolean notused) {
		this.notused = notused;
	}
	
	/**
	 * Returns if the day is planned for dispensing
	 * @return true - if it's planned prescribing
	 */
	public boolean isPrescribed() {
		return prescribed;
	}
	
	public void setPrescribed(boolean prescribed) {
		this.prescribed = prescribed;
	}
	
	/**
	 * Returns if there was medicine dispensing in the day
	 * @return true - if it was dispensed medicine
	 */
	public boolean isDispensed() {
		return dispensed;
	}
	public void setDispensed(boolean dispensed) {
		this.dispensed = dispensed;
		if (dispensed)
			 monthInfo.setDispensingDays(monthInfo.getDispensingDays() + 1);
		else monthInfo.setDispensingDays(monthInfo.getDispensingDays() - 1);
	}

	/**
	 * @param treated the treated to set
	 */
	public void setTreated(boolean treated) {
		this.treated = treated;
	}

	/**
	 * Returns if this day already is inside a dispensing period
	 * @return the treated
	 */
	public boolean isTreated() {
		return treated;
	}


	public Dot getSelected() {
		return selected;
	}


	public void setSelected(Dot selected) {
		this.selected = selected;
	}
	
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
