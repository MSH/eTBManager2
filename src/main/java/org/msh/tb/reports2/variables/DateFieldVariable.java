package org.msh.tb.reports2.variables;

import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

/**
 * Standard variable to be used in date fields
 * @author Ricardo Memoria
 *
 */
public class DateFieldVariable extends VariableImpl {

	public static final String PERIOD_MONTHYEAR = "M";
	public static final String PERIOD_FIXED = "F";
	
	/**
	 * Possible values for fixed periods
	 * @author Ricardo Memoria
	 *
	 */
	public enum FixedPeriod {
		LAST_3MONTHS,
		LAST_6MONTHS,
		LAST_12MONTHS,
		PREVIOUS_QUARTER,
		PREVIOUS_YEAR;
	};
	
	private boolean yearOnly;

	private DateFormatSymbols dateFormatSymbols;

	/**
	 * @param id
	 * @param label
	 * @param fieldName
	 * @param yearOnly
	 */
	public DateFieldVariable(String id, String label, String fieldName, boolean yearOnly) {
		super(id, label, fieldName);
		this.yearOnly = yearOnly;
	}

	
	/**
	 * Return the month name by its month index
	 * @param monthindex is the month index starting in 1-january
	 * @return the short name of the month
	 */
	protected String getMonthName(int monthindex) {
		if (dateFormatSymbols == null)
			dateFormatSymbols = new DateFormatSymbols(LocaleSelector.instance().getLocale());
		return dateFormatSymbols.getShortMonths()[monthindex - 1];
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.select("year(" + getFieldName() + ")");
		if (!yearOnly)
			def.select("month(" + getFieldName() + ")");
		def.addRestriction(getFieldName() + " is not null");
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if (!yearOnly) {
			Object[] vals = (Object[])values;
			Integer year = (Integer)vals[0];
			Integer month = (Integer)vals[1];

			return year * 100 + month;
		}

		return values;
	}


	/**
	 * @return the yearOnly
	 */
	public boolean isYearOnly() {
		return yearOnly;
	}


	/**
	 * Toggle the generation to year or year and month 
	 * @param yearOnly the yearOnly to set
	 */
	public void setYearOnly(boolean yearOnly) {
		this.yearOnly = yearOnly;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues(Object val1, Object val2) {
		return ((Integer)val1).compareTo((Integer)val2);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDomain()
	 */
	@Override
	public Object[] getDomain() {
		return null;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		Object v = filterValueFromString(value.asString());

		if (v instanceof Integer) {
			int num = (Integer)v;
			if (num < 9999) {
				def.addRestriction("year(" + getFieldName() + ") = " + num);
			}
			else {
				int month = num % 100;
				int year = num / 100;
				def.addRestriction("month(" + getFieldName() + ") = " + month);
				def.addRestriction("year(" + getFieldName() + ") = " + year);
			}
			return;
		}
		
		Period p;
		if (v instanceof String)
			 p = stringToPeriod((String)v);
		else p = (Period)v;
		
		if (p == null)
			return;

		String fname = getFieldName();
		String pname = getId();

		if (!p.isEmpty()) {
			def.addRestriction(fname + " between :" + pname + "1 and :" + pname + "2");
			def.addParameter(pname + "1", p.getIniDate());
			def.addParameter(pname + "2", p.getEndDate());
			return;
		}

		if (p.getIniDate() != null) {
			def.addRestriction(fname + " >= :" + pname + "1");
			def.addParameter(pname + "1", p.getIniDate());
			return;
		}

		if (p.getEndDate() != null) {
			def.addRestriction(fname + " <= :" + pname + "1");
			def.addParameter(pname + "1", p.getEndDate());
			return;
		}
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if (key == null)
			return Messages.instance().get("global.notdef");

		if (!yearOnly) {
			int val = (Integer)key;
			int month = val % 100;
			return getMonthName(month);
		}
		else return key.toString();
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#isGrouped()
	 */
	@Override
	public boolean isGrouped() {
		return !yearOnly;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createGroupKey(java.lang.Object)
	 */
	@Override
	public Object createGroupKey(Object values) {
		if (values.getClass().isArray()) {
			Object[] vals = (Object[])values;
			return vals[0];
		}
		else return values;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getGroupDisplayText(java.lang.Object)
	 */
	@Override
	public String getGroupDisplayText(Object key) {
		return super.getGroupDisplayText(key);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return "period";
	}


    /**
     * Convert a filter value to its period representation
     * @param value
     * @return
     */
	public Object convertToFilterValue(String value) {
		if (value == null)
			return null;
		
		if (value.contains(","))
			return stringToPeriod(value);

		// if it's grouped, so both values from the filter is sent from the table
		if (isGrouped()) {
			String[] vals = value.split(";");
			if (vals.length > 1)
				value = vals[vals.length - 1];
		}
		
		return Integer.parseInt(value);
	}
	
	/**
	 * Convert a string to an instance of the {@link Period} class
	 * @param value
	 * @return
	 */
	public Period stringToPeriod(String value) {
		String s[] = value.split(",");
		if (s.length <= 1)
			return null;
		
		Period p = new Period();

		if (PERIOD_FIXED.equals(s[0])) {
			return fixedPeriodStringToPeriod(s[1]);
		}
		else
		if (PERIOD_MONTHYEAR.equals(s[0])) {
			int month = Integer.parseInt(s[1]) - 1;
			int year = Integer.parseInt(s[2]);
			p.setIniDate(DateUtils.newDate(year, month, 1));
			if (s.length > 3) {
				month = Integer.parseInt(s[3]) - 1;
				year = Integer.parseInt(s[4]);
				p.setEndDate(DateUtils.newDate(year, month, DateUtils.daysInAMonth(year, month)));
			}
		}
		else throw new IllegalArgumentException("Not valid value for filter " + getId());
		
		return p;
	}
	
	/**
	 * Convert a string containing the fixed period to its period.
	 * The string contains an integer indicating the position of the {@link FixedPeriod} enumeration
	 * @param s string indicating a 0-index position of the {@link FixedPeriod} enumeration
	 * @return instance of {@link Period} containing the period
	 */
	protected Period fixedPeriodStringToPeriod(String s) {
		int index = Integer.parseInt(s);
		FixedPeriod fp = FixedPeriod.values()[index];
		
		Period p = new Period();
		Date dt = DateUtils.getDate();
		switch (fp) {
		case LAST_3MONTHS:
			p.setIniDate(createIniDate(DateUtils.incMonths(dt, -3)));
			p.setEndDate(createEndDate(dt));
			return p;

		case LAST_6MONTHS:
			p.setIniDate(createIniDate(DateUtils.incMonths(dt, -6)));
			p.setEndDate(createEndDate(dt));
			return p;
			
		case LAST_12MONTHS:
			p.setIniDate(createIniDate(DateUtils.incMonths(dt, -12)));
			p.setEndDate(createEndDate(dt));
			return p;
		
		case PREVIOUS_QUARTER:
			return calcPreviousQuarter(dt);
			
		case PREVIOUS_YEAR:
			return calcPreviousYear(dt);
		}

		throw new RuntimeException("Fixed period not supported: " + fp);
	}
	
	
	/**
	 * Calculate the previous year 
	 * @param dt is the reference date to calculate the previous year
	 * @return period within the previous year
	 */
	private Period calcPreviousYear(Date dt) {
		int year = DateUtils.yearOf(dt) - 1;
		
		Date ini = DateUtils.newDate(year, 0, 1);
		Date end = DateUtils.newDate(year, 11, DateUtils.daysInAMonth(year, 11));
		return new Period(ini, end);
	}


	/**
	 * Calculate the previous quarter from the given date
	 * @param dt is the reference date to calculate the previous quarter
	 * @return period of the previous quarter
	 */
	protected Period calcPreviousQuarter(Date dt) {
		int month = DateUtils.monthOf(dt);
		int year = DateUtils.yearOf(dt);
		// calculate the previous quarter
		int quarter = (month / 4) - 1;
		if (quarter == -1) {
			quarter = 3;
			year--;
		}
		// calculate the initial and final date of the quarter
		Date ini = DateUtils.newDate(year, quarter * 3, 1);
		Date end = DateUtils.newDate(year, (quarter * 3) + 2, 1);
		
		return new Period(ini, createEndDate(end));
	}


	/**
	 * Return the initial date of a period just setting the day of the month
	 * of the given date to 1
	 * @param date date of the initial date
	 * @return date set its day of month to 1
	 */
	protected Date createIniDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return DateUtils.getDatePart(c.getTime());
	}
	
	/**
	 * Return the final date of the period. Using the given date, it returns
	 * the date with the last day of the month 
	 * @param date
	 * @return
	 */
	protected Date createEndDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, DateUtils.daysInAMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH)));
		return c.getTime();
	}
	
	
	/**
	 * Convert a month/year string representation to a period
	 * @param vals the initial and final month/year, with the following position:
	 * 	 [1] = initial month, [2] = initial year, [3] = final month, [4] = final year
	 * @return instance of {@link Period} containing the period
	 */
	protected Period monthYearStringToPeriod(String[] vals) {
		Period p = new Period();
		int month = Integer.parseInt(vals[1]) - 1;
		int year = Integer.parseInt(vals[2]);
		p.setIniDate(DateUtils.newDate(year, month, 1));
		if (vals.length > 3) {
			month = Integer.parseInt(vals[3]) - 1;
			year = Integer.parseInt(vals[4]);
			p.setEndDate(DateUtils.newDate(year, month, DateUtils.daysInAMonth(year, month)));
		}
		return p;
	}

	@Override
	public String filterValueToString(Object value) {
		return (value == null? null: value.toString());
	}
}
