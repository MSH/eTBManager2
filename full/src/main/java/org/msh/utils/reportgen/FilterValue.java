package org.msh.utils.reportgen;

/**
 * Keep the value of a filter entered by the user
 * @author Ricardo Memoria
 *
 */
public class FilterValue {

	private Filter filter;
	private Object value;

	/**
	 * Check if the filter value is null
	 * @return
	 */
	public boolean isNull() {
		if (value == null)
			return true;
		
		return (value instanceof String? value.toString().trim().isEmpty(): false);
	}

	public String createSQLCondition(ReportQuery reportQuery) {
		return filter.createSQLCondition(value, reportQuery);
	}
	
	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}
	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
