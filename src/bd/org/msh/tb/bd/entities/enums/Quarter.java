package org.msh.tb.bd.entities.enums;

import java.util.GregorianCalendar;

public enum Quarter {
	FIRST(01, GregorianCalendar.JANUARY, 31, GregorianCalendar.MARCH),
	SECOND(01, GregorianCalendar.APRIL, 30, GregorianCalendar.JUNE),
	THIRD(01, GregorianCalendar.JULY, 30, GregorianCalendar.SEPTEMBER),
	FOURTH(01, GregorianCalendar.OCTOBER, 31, GregorianCalendar.DECEMBER);
	
	private int iniDay;
	private int iniMonth;
	private int endDay;
	private int endMonth;
	
	Quarter(int iniDay, int iniMonth, int endDay, int endMonth){
		this.iniDay = iniDay;
		this.iniMonth = iniMonth;
		this.endDay = endDay;
		this.endMonth = endMonth;
	}
	
	public static Quarter getQuarterByMonth(int month){
		if(month == GregorianCalendar.JANUARY || month == GregorianCalendar.FEBRUARY || month == GregorianCalendar.MARCH)
			return Quarter.FIRST;
		if(month == GregorianCalendar.APRIL || month == GregorianCalendar.MAY || month == GregorianCalendar.JUNE)
			return Quarter.SECOND;		
		if(month == GregorianCalendar.JULY || month == GregorianCalendar.AUGUST || month == GregorianCalendar.SEPTEMBER)
			return Quarter.THIRD;
		if(month == GregorianCalendar.OCTOBER || month == GregorianCalendar.NOVEMBER || month == GregorianCalendar.DECEMBER)
			return Quarter.FOURTH;
		
		return null;
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
