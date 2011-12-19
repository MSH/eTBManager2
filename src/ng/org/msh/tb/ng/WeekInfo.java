package org.msh.tb.ng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WeekInfo implements Serializable {
	private static final long serialVersionUID = 8953891395553078074L;

	private int iniDay;
	private int endDay;
	private int week;

	private List<DayInfo> days = new ArrayList<DayInfo>();

	public int getNumDays() {
		return endDay - iniDay + 1;
	}
	
	
	/**
	 * @return the treatments
	 */
	public List<DayInfo> getDays() {
		return days;
	}
	/**
	 * @param treatments the treatments to set
	 */
	public void setDays(List<DayInfo> days) {
		this.days = days;
	}
	/**
	 * @return the iniDay
	 */
	public int getIniDay() {
		return iniDay;
	}
	/**
	 * @param iniDay the iniDay to set
	 */
	public void setIniDay(int iniDay) {
		this.iniDay = iniDay;
	}
	/**
	 * @return the endDay
	 */
	public int getEndDay() {
		return endDay;
	}
	/**
	 * @param endDay the endDay to set
	 */
	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}
	/**
	 * @return the week
	 */
	public int getWeek() {
		return week;
	}
	/**
	 * @param week the week to set
	 */
	public void setWeek(int week) {
		this.week = week;
	}
}
