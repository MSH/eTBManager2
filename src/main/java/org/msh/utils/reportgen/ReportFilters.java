package org.msh.utils.reportgen;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintain a list of filter values to be used to construct a report
 * @author Ricardo Memoria
 *
 */
public class ReportFilters {

	private List<FilterValue> filterValues;
	
	/**
	 * Set a filter value by its id
	 * @param filterId
	 * @param value
	 */
	public void setFilterValue(String filterId, Object value) {
		FilterValue filterValue = getFilterById(filterId);
		if (filterValue == null)
			throw new RuntimeException("No filter found");
		filterValue.setValue(value);
	}
	
	/**
	 * @param filter
	 * @param value
	 */
	public void setFilterValue(Filter filter, Object value) {
		FilterValue filterValue = searchFilterValue(filter);
		if (filterValue == null)
			throw new RuntimeException("No filter found");
		filterValue.setValue(value);
	}

	
	/**
	 * Search a filter value by its filter
	 * @param filter
	 * @return
	 */
	public FilterValue searchFilterValue(Filter filter) {
		for (FilterValue filterValue: getFilterValues()) {
			if (filter.equals( filterValue.getFilter().getId() ))
				return filterValue;
		}
		
		return null;
	}
	
	/**
	 * Return a filter by its id
	 * @param id
	 * @return
	 */
	public FilterValue getFilterById(String id) {
		for (FilterValue filterValue: getFilterValues()) {
			if (id.equals( filterValue.getFilter().getId() ))
				return filterValue;
		}
		
		return null;
	}
	
	/**
	 * Return the list of the filters to be available to the report
	 * @return
	 */
	protected List<Filter> getFilters() {
		return new ArrayList<Filter>();
	}
	
	/**
	 * Create a list of instances of the class {@link FilterValue}
	 * @return
	 */
	protected List<FilterValue> createFilterValues() {
		filterValues = new ArrayList<FilterValue>();

		return filterValues;
	}
	
	/**
	 * Return a list of instances of the class {@link FilterValue} containing the filters and values given by the user
	 * @return
	 */
	public List<FilterValue> getFilterValues() {
		if (filterValues == null)
			filterValues = createFilterValues();
		
		return filterValues;
	}
}
