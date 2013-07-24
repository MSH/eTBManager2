package org.msh.tb.bd.entities.enums;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.msh.utils.date.DateUtils;

public enum Quarter {
	FIRST(01, GregorianCalendar.JANUARY, 31, GregorianCalendar.MARCH),
	SECOND(01, GregorianCalendar.APRIL, 30, GregorianCalendar.JUNE),
	THIRD(01, GregorianCalendar.JULY, 30, GregorianCalendar.SEPTEMBER),
	FOURTH(01, GregorianCalendar.OCTOBER, 31, GregorianCalendar.DECEMBER);
	
	private final int iniDay;
	private final int iniMonth;
	private final int endDay;
	private final int endMonth;
	
	private int year;
	
	Quarter(int iniDay, int iniMonth, int endDay, int endMonth){
		this.iniDay = iniDay;
		this.iniMonth = iniMonth;
		this.endDay = endDay;
		this.endMonth = endMonth;
	}
	
	/**
	 * Returns the quarter according to the month passed as parameter.
	 * @param month
	 * @return
	 */
	public static Quarter getQuarterByMonth(int month, int year){
		if(year == 0)
			return null;
		
		Quarter ret = null;
		
		if(month == GregorianCalendar.JANUARY || month == GregorianCalendar.FEBRUARY || month == GregorianCalendar.MARCH){
			ret = Quarter.FIRST;
			ret.setYear(year);
		}else if(month == GregorianCalendar.APRIL || month == GregorianCalendar.MAY || month == GregorianCalendar.JUNE){
			ret = Quarter.SECOND;
			ret.setYear(year);
		}else if(month == GregorianCalendar.JULY || month == GregorianCalendar.AUGUST || month == GregorianCalendar.SEPTEMBER){
			ret = Quarter.THIRD;
			ret.setYear(year);
		}else if(month == GregorianCalendar.OCTOBER || month == GregorianCalendar.NOVEMBER || month == GregorianCalendar.DECEMBER){
			ret = Quarter.FOURTH;
			ret.setYear(year);
		}
		
		return ret;
	}
	
	/**
	 * Returns the quarter according to the date passed as parameter.
	 * @param month
	 * @return
	 */
	public static Quarter getQuarterByDate(Date dt){
		if(dt == null)
			return null;
		
		return Quarter.getQuarterByMonth(DateUtils.monthOf(dt), DateUtils.yearOf(dt));
	}
	
	/**
	 * Returns the next quarter according to the quarter passed as parameter.
	 * @param quarter
	 * @return
	 */
	public static Quarter getNextQuarter(Quarter quarter){
		if(quarter.getYear() == 0)
			return null;
		
		Quarter ret = null;
		
		if(quarter.equals(Quarter.FIRST)){
			ret = Quarter.SECOND;
			ret.setYear(quarter.getYear());
		}else if(quarter.equals(Quarter.SECOND)){
			ret = Quarter.THIRD;
			ret.setYear(quarter.getYear());
		}else if(quarter.equals(Quarter.THIRD)){
			ret = Quarter.FOURTH;
			ret.setYear(quarter.getYear());
		}else if(quarter.equals(Quarter.FOURTH)){
			ret = Quarter.FIRST;
			ret.setYear(quarter.getYear()+1);
		}
		
		return ret;
	}
	
	/**
	 * Returns the next previous according to the quarter passed as parameter.
	 * @param quarter
	 * @return
	 */
	public static Quarter getPreviousQuarter(Quarter quarter){
		if(quarter.getYear() == 0)
			return null;
		
		Quarter ret = null;
		
		if(quarter.equals(Quarter.FOURTH)){
			ret = Quarter.THIRD;
			ret.setYear(quarter.getYear());
		}else if(quarter.equals(Quarter.THIRD)){
			ret = Quarter.SECOND;
			ret.setYear(quarter.getYear());
		}else if(quarter.equals(Quarter.SECOND)){
			ret = Quarter.FIRST;
			ret.setYear(quarter.getYear());
		}else if(quarter.equals(Quarter.FIRST)){
			ret = Quarter.FOURTH;
			ret.setYear(quarter.getYear()-1);
		}
		
		return ret;
	}
	
	/** 
	 * @return the initial date of the quarter passed as parameter
	 */
	public static Date getIniDate(Quarter quarter, int year){
		if(quarter.getYear() == 0)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, quarter.getIniMonth());
		c.set(Calendar.DAY_OF_MONTH, quarter.getIniDay());
		
		return c.getTime();
	}
	
	/** 
	 * @return the final date of the quarter passed as parameter
	 */
	public static Date getEndDate(Quarter quarter, int year){
		if(quarter.getYear() == 0)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, quarter.getIniMonth());
		c.set(Calendar.DAY_OF_MONTH, quarter.getIniDay());
		
		return c.getTime();
	}
	
	
	/** 
	 * @return current quarter according to todays date
	 */
	public static Quarter getCurrentQuarter(){
		return Quarter.getQuarterByMonth(DateUtils.monthOf(DateUtils.getDate()), DateUtils.yearOf(DateUtils.getDate()));
	}
	
	/** 
	 * @return the initial date of the quarter.
	 */
	public Date getIniDate(){
		if(this.getYear() == 0)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		
		c.set(Calendar.YEAR, this.getYear());
		c.set(Calendar.MONTH, this.getIniMonth());
		c.set(Calendar.DAY_OF_MONTH, this.getIniDay());
		
		return c.getTime();
	}
	
	/** 
	 * @return the final date of the quarter.
	 */
	public Date getEndDate(){
		if(this.getYear() == 0)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		
		c.set(Calendar.YEAR, this.getYear());
		c.set(Calendar.MONTH, this.getEndMonth());
		c.set(Calendar.DAY_OF_MONTH, this.getEndDay());
		
		return c.getTime();
	}
	
	/**
	 * Compares if the passed quarter is the same quarter of the instanced including year.
	 */
	public boolean isTheSame(Quarter quarterP){
		return this.getYear() == quarterP.getYear() && this.ordinal() == quarterP.ordinal();
	} 
	
	/**
	 * @return Returns a new instance of the same quarter.
	 */
	public Quarter copy(){
		Quarter ret = null;
		
		if(this.equals(Quarter.FOURTH)){
			ret = Quarter.FOURTH;
			ret.setYear(this.getYear());
		}else if(this.equals(Quarter.THIRD)){
			ret = Quarter.THIRD;
			ret.setYear(this.getYear());
		}else if(this.equals(Quarter.SECOND)){
			ret = Quarter.SECOND;
			ret.setYear(this.getYear());
		}else if(this.equals(Quarter.FIRST)){
			ret = Quarter.FIRST;
			ret.setYear(this.getYear());
		}
		
		return ret;
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

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}
}
