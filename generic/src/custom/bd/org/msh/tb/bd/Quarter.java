package org.msh.tb.bd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.msh.tb.bd.entities.enums.QuarterMonths;
import org.msh.utils.date.DateUtils;

public class Quarter {
	
	private QuarterMonths quarter;
	private int year;
	
	public Quarter(QuarterMonths quarter, int year){
		this.quarter = quarter;
		this.year = year;
	}
	
	public Quarter(){
		this.quarter = null;
		this.year = 0;
	}
	
	/**
	 * Returns the next quarter of this.
	 * @param quarter
	 * @return
	 */
	public Quarter getNextQuarter(){
		if(this.getYear() == 0)
			return null;
		
		QuarterMonths qMonths = null;
		int nYear = this.year;
		
		if(quarter.equals(QuarterMonths.FIRST)){
			qMonths = QuarterMonths.SECOND;
		}else if(quarter.equals(QuarterMonths.SECOND)){
			qMonths = QuarterMonths.THIRD;
		}else if(quarter.equals(QuarterMonths.THIRD)){
			qMonths = QuarterMonths.FOURTH;
		}else if(quarter.equals(QuarterMonths.FOURTH)){
			qMonths = QuarterMonths.FIRST;
			nYear = nYear+1;
		}
		
		return new Quarter(qMonths, nYear);
	}
	
	/**
	 * Returns the previous quarter of this.
	 * @param quarter
	 * @return
	 */
	public Quarter getPreviousQuarter(){
		if(this.getYear() == 0)
			return null;
		
		QuarterMonths qMonths = null;
		int nYear = this.year;
		
		if(quarter.equals(QuarterMonths.FOURTH)){
			qMonths = QuarterMonths.THIRD;
		}else if(quarter.equals(QuarterMonths.THIRD)){
			qMonths = QuarterMonths.SECOND;
		}else if(quarter.equals(QuarterMonths.SECOND)){
			qMonths = QuarterMonths.FIRST;
		}else if(quarter.equals(QuarterMonths.FIRST)){
			qMonths = QuarterMonths.FOURTH;
			nYear = nYear - 1;
		}
		
		return new Quarter(qMonths, nYear);
	}	
	
	/** 
	 * @return the initial date of the quarter.
	 */
	public Date getIniDate(){
		if(this.getYear() == 0 || this.quarter == null)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		
		c.set(Calendar.YEAR, this.getYear());
		c.set(Calendar.MONTH, this.getQuarter().getIniMonth());
		c.set(Calendar.DAY_OF_MONTH, this.getQuarter().getIniDay());
		
		return c.getTime();
	}
	
	/** 
	 * @return the final date of the quarter.
	 */
	public Date getEndDate(){
		if(this.getYear() == 0 || this.quarter == null)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		
		c.set(Calendar.YEAR, this.getYear());
		c.set(Calendar.MONTH, this.getQuarter().getEndMonth());
		c.set(Calendar.DAY_OF_MONTH, this.getQuarter().getEndDay());
		
		return c.getTime();
	}
	
	/**
	 * Compares if the passed quarter is the same quarter of the instanced including year.
	 */
	public boolean isTheSame(Quarter quarterP){
		return this.getYear() == quarterP.getYear() && this.getQuarter().ordinal() == quarterP.getQuarter().ordinal();
	} 
	
	public Quarter clone(){
		return new Quarter(this.quarter, this.year);
	}	
	
	/** 
	 * @return current quarter according to todays date
	 */
	public static Quarter getCurrentQuarter(){
		return Quarter.getQuarterByMonth(DateUtils.monthOf(DateUtils.getDate()), DateUtils.yearOf(DateUtils.getDate()));
	}
	
	/**
	 * Returns the quarter according to the month passed as parameter.
	 * @param month
	 * @return
	 */
	public static Quarter getQuarterByMonth(int month, int year){
		if(year == 0)
			return null;
		
		QuarterMonths qMonths = null;
		
		if(month == GregorianCalendar.JANUARY || month == GregorianCalendar.FEBRUARY || month == GregorianCalendar.MARCH)
			qMonths = QuarterMonths.FIRST;
		else if(month == GregorianCalendar.APRIL || month == GregorianCalendar.MAY || month == GregorianCalendar.JUNE)
			qMonths = QuarterMonths.SECOND;
		else if(month == GregorianCalendar.JULY || month == GregorianCalendar.AUGUST || month == GregorianCalendar.SEPTEMBER)
			qMonths = QuarterMonths.THIRD;
		else if(month == GregorianCalendar.OCTOBER || month == GregorianCalendar.NOVEMBER || month == GregorianCalendar.DECEMBER)
			qMonths = QuarterMonths.FOURTH;
		
		if(qMonths == null)
			return null;
		
		return new Quarter(qMonths, year);
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
	 * @return the quarter
	 */
	public QuarterMonths getQuarter() {
		return quarter;
	}

	/**
	 * @param quarter the quarter to set
	 */
	public void setQuarter(QuarterMonths quarter) {
		this.quarter = quarter;
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
