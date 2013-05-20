package org.msh.tb.client.reports.filters;

import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.user.client.ui.Composite;

public abstract class FilterWidget extends Composite {

	private CFilter filter;

	/**
	 * Initialize the filter
	 * @param filterData
	 */
	public void initialize(CFilter filterData) {
		this.filter = filterData;
	}
	
	/**
	 * Set the value selected in the filter
	 * @param value is the String representation of the filter selection
	 */
	public abstract void setValue(String value);
	
	/**
	 * Return the value of the selected filter
	 * @return
	 */
	public abstract String getValue();

	/**
	 * Return the filter assigned to this widget
	 * @return the filter
	 */
	public CFilter getFilter() {
		return filter;
	}
}
