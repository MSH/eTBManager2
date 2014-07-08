package org.msh.reports;

import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOperation;

public class FilterValue {

	private Filter filter;
	private FilterOperation comparator;
	private Object value;
	
	public FilterValue(Filter filter, FilterOperation comparator, Object value) {
		super();
		this.filter = filter;
		this.comparator = comparator;
		this.value = value;
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
	 * @return the comparator
	 */
	public FilterOperation getComparator() {
		return comparator;
	}

	/**
	 * @param comparator the comparator to set
	 */
	public void setComparator(FilterOperation comparator) {
		this.comparator = comparator;
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
