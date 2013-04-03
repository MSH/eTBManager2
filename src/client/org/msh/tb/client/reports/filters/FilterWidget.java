package org.msh.tb.client.reports.filters;

import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.user.client.ui.Composite;

public abstract class FilterWidget extends Composite {

	private CFilter filter;

	public FilterWidget(CFilter filter) {
		super();
		this.filter = filter;
	}
	
	/**
	 * Get the value set in the filter
	 * @return
	 */
	public abstract String getValue();


	/**
	 * @return the filter
	 */
	public CFilter getFilter() {
		return filter;
	}
}
