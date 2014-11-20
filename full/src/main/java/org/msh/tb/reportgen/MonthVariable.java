package org.msh.tb.reportgen;

import org.jboss.seam.international.LocaleSelector;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

import java.text.DateFormatSymbols;

public class MonthVariable implements Variable {

	private final static String[] fields = {"year(transactionDate)", "month(transactionDate)"};
	
	private DateFormatSymbols dfs = new DateFormatSymbols(LocaleSelector.instance().getLocale());
	private boolean grouped = false;
	
	@Override
	public String getTitle() {
		return "Monthly distribution";
	}

	@Override
	public String[] createSQLSelectFields(ReportQuery reportQuery) {
		return fields;
	}

	@Override
	public String[] createSQLGroupByFields(ReportQuery reportQuery) {
		return fields;
	}

	@Override
	public Object translateValues(Object[] value) {
		int year = (Integer)value[0];
		int month = (Integer)value[1];
		return new Item(month, year);
	}

	@Override
	public Object translateSingleValue(Object value) {
		return null;
	}

	@Override
	public String getValueDisplayText(Object value) {
		if (value == null)
			return null;

		if (value instanceof Item) {
			Item item = (Item)value;
			String s = dfs.getShortMonths()[item.getMonth() - 1];
			
			if (!grouped)
				s += "-" + Integer.toString(item.getYear());
			return s;
		}
		else return value.toString();
	}

	@Override
	public Integer compareValues(Object val1, Object val2) {
		if (val1 instanceof Item) {
			Item v1 = (Item)val1;
			Item v2 = (Item)val2;

			return (v1 != null? v1.compareTo(v2): (v2 == null? 0: 1));
		}
		else return ((Integer)val1).compareTo((Integer)val2);
	}
	
	
	public class Item {
		private int month;
		private int year;

		public Item(int month, int year) {
			super();
			this.month = month;
			this.year = year;
		}

		public int compareTo(Item item) {
			Integer v1 = year * 100 + month;
			Integer v2 = item.getYear() * 100 + item.getMonth();
			return v1.compareTo(v2);
		}
		
		/**
		 * @return the month
		 */
		public Integer getMonth() {
			return month;
		}
		/**
		 * @return the year
		 */
		public Integer getYear() {
			return year;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + month;
			result = prime * result + year;
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Item other = (Item) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (month != other.month)
				return false;
			if (year != other.year)
				return false;
			return true;
		}

		private MonthVariable getOuterType() {
			return MonthVariable.this;
		}
	}


	@Override
	public Object getGroupData(Object val1) {
		return ((Item)val1).getYear();
	}

	@Override
	public boolean setGrouping(boolean value) {
		grouped = value;
		return true;
	}
}
