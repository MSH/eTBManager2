package org.msh.tb.reports2.variables;

import java.text.DateFormatSymbols;

import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

/**
 * Standard variable to be used in date fields
 * @author Ricardo Memoria
 *
 */
public class DateFieldVariable extends VariableImpl {

	private boolean yearOnly;

	private DateFormatSymbols dateFormatSymbols;

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
		def.addField("year(" + getFieldName() + ")");
		if (!yearOnly)
			def.addField("month(" + getFieldName() + ")");
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
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		if (value instanceof Integer) {
			int num = (Integer)value;
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
		if (value instanceof String)
			 p = stringToPeriod((String)value);
		else p = (Period)value;
		
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

	@Override
	public Object filterValueFromString(String value) {
		if (value == null)
			return null;
		
		if (value.contains(","))
			return stringToPeriod(value);
		
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

		if ("M".equals(s[0])) {
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

	@Override
	public String filterValueToString(Object value) {
		return (value == null? null: value.toString());
	}
}
