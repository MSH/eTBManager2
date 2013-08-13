package org.msh.tb.bd.entities.enums;

import java.util.GregorianCalendar;

public enum QuarterMonths {
	FIRST(01, GregorianCalendar.JANUARY, 31, GregorianCalendar.MARCH),
	SECOND(01, GregorianCalendar.APRIL, 30, GregorianCalendar.JUNE),
	THIRD(01, GregorianCalendar.JULY, 30, GregorianCalendar.SEPTEMBER),
	FOURTH(01, GregorianCalendar.OCTOBER, 31, GregorianCalendar.DECEMBER);
	
	private final int iniDay;
	private final int iniMonth;
	private final int endDay;
	private final int endMonth;
	
	QuarterMonths(int iniDay, int iniMonth, int endDay, int endMonth){
		this.iniDay = iniDay;
		this.iniMonth = iniMonth;
		this.endDay = endDay;
		this.endMonth = endMonth;
	}

	public String getKey(){
		return "Quarter." + this.name();
	}

	/**
	 * @return the iniDay
	 */
	public int getIniDay() {
		return iniDay;
	}

	/**
	 * @return the iniMonth
	 */
	public int getIniMonth() {
		return iniMonth;
	}
	
	/**
	 * @return the endDay
	 */
	public int getEndDay() {
		return endDay;
	}
	
	/**
	 * @return the endMonth
	 */
	public int getEndMonth() {
		return endMonth;
	}
}
