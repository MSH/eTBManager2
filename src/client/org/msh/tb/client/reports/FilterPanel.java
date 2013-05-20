package org.msh.tb.client.reports;

import org.msh.tb.client.reports.filters.FilterFactory;
import org.msh.tb.client.reports.filters.FilterWidget;
import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class FilterPanel extends HoverPanel {

	private Label label;
	private CFilter filter;
	private FilterWidget filterWidget;

	public FilterPanel(CFilter filter) {
		super();
		this.filter = filter;

		setWidth("800px");
		addStyleName("prop");
		label = new Label(filter.getName() + ":");
		label.setStyleName("name");
		add(label);
		
		// create filter widget
		filterWidget = FilterFactory.createFilter(filter.getType());
		
		if (filterWidget != null) {
			filterWidget.initialize(filter);
			FlowPanel pnl = new FlowPanel();
			pnl.setStyleName("value");
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

	@Override
	protected void removePanel() {
		MainPage.instance().removeFilter(this);
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
}
