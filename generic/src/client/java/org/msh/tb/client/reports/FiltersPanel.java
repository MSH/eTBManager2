/**
 * 
 */
package org.msh.tb.client.reports;

import java.util.HashMap;

import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Ricardo Memoria
 *
 */
public class FiltersPanel extends Composite {

	private VerticalPanel pnlFilters;
	
	/**
	 * Default filter
	 */
	public FiltersPanel() {
		super();
		pnlFilters = new VerticalPanel();
		initWidget(pnlFilters);
	}
	
	/**
	 * Add a new filter
	 * @param filter instance of CFilter
	 */
	public void addFilter(CFilter filter, String value) {
		FilterBox box = new FilterBox(this, filter, value);
		pnlFilters.add(box);
	}

	
	/**
	 * Remove a filter and its box from the panel
	 * @param box instance of the {@link FilterBox} class
	 */
	public void removeFilterBox(FilterBox box) {
		pnlFilters.remove(box);
	}
	
	/**
	 * Update the list of filters from a list of filter IDs and its value in string format
	 * @param filters map of filter IDs and its value
	 */
	public void update(HashMap<String, String> filters) {
		pnlFilters.clear();
		if (filters != null) {
			for (String filterid: filters.keySet()) {
				CFilter filter = MainPage.instance().findFilterById(filterid);
				if (filter != null) {
					addFilter(filter, null);
				}
			}
		}
	}
	
	
	/**
	 * Return the list of declared filters
	 * @return map of filter IDs and its value
	 */
	public HashMap<String, String> getDeclaredFilters() {
		HashMap<String, String> filters = new HashMap<String, String>();

		for (int i = 0; i < pnlFilters.getWidgetCount(); i++) {
			FilterBox pnl = (FilterBox)pnlFilters.getWidget(i);
			String val = pnl.getFilterValue();
			if (val != null) {
				filters.put(pnl.getFilter().getId(), val);
			}
		}
		return filters;
	}
}
