package org.msh.tb.client.reports.filters;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import org.msh.tb.client.reports.HoverPanel;
import org.msh.tb.client.shared.model.CFilter;

/**
 * Panel where the filter is displayed
 * 
 * @author Ricardo Memoria
 *
 */
public class FilterBox extends HoverPanel {

	private Label label;
	private CFilter filter;
	private FilterWidget filterWidget;
	private FiltersPanel filtersPanel;
	
	
	public FilterBox(FiltersPanel filtersPanel, CFilter filter, String value) {
		super();
		this.filter = filter;
		this.filtersPanel = filtersPanel;

		setWidth("800px");
		addStyleName("prop");
		label = new Label(filter.getName() + ":");
		label.setStyleName("filter_name");
		add(label);
		
		// create filter widget
		filterWidget = FilterFactory.createFilter(filter.getType());
		
		if (filterWidget != null) {
			filterWidget.initialize(filter, value);
			FlowPanel pnl = new FlowPanel();
			pnl.setStyleName("filter_value");
			pnl.add(filterWidget);
			add(pnl);
		}
	}

	/**
	 * @return the filter
	 */
	public CFilter getFilter() {
		return filter;
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void removePanel() {
		filtersPanel.removeFilterBox(this);
	}

	/**
	 * @return the filterWidget
	 */
	public FilterWidget getFilterWidget() {
		return filterWidget;
	}
	
	/**
	 * Return the value selected in the filter
	 * @return
	 */
	public String getFilterValue() {
		return filterWidget != null? filterWidget.getValue(): null;
	}

	
	/**
	 * Set the filter's content being displayed
	 * @param value is the string representation of the filter value
	 */
	public void setFilterValue(String value) {
		filterWidget.setValue(value);
	}
	
	/**
	 * @return the filtersPanel
	 */
	public FiltersPanel getFiltersPanel() {
		return filtersPanel;
	}

	/**
	 * @param filtersPanel the filtersPanel to set
	 */
	public void setFiltersPanel(FiltersPanel filtersPanel) {
		this.filtersPanel = filtersPanel;
	}
}
