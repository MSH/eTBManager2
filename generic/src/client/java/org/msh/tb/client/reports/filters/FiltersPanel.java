/**
 * 
 */
package org.msh.tb.client.reports.filters;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.ReportUtils;
import org.msh.tb.client.shared.model.CFilter;

import java.util.HashMap;

/**
 * Panel that displays selected filters
 * @author Ricardo Memoria
 *
 */
public class FiltersPanel extends Composite {

    private static final Binder binder = GWT.create(Binder.class);
    interface Binder extends UiBinder<Widget, FiltersPanel> { }

	@UiField VerticalPanel pnlFilters;

	/**
	 * Default filter
	 */
	public FiltersPanel() {
		super();
		initWidget(binder.createAndBindUi(this));
	}


	/**
	 * Add a new filter
	 * @param filter instance of CFilter
	 */
	public void addFilter(CFilter filter, String value) {
		FilterBox box = new FilterBox(this, filter, value);
		pnlFilters.add(box);
	}


    @UiHandler("btnAddFilter")
    public void btnAddFilterClick(ClickEvent event) {
        StandardEventHandler listener = new StandardEventHandler() {
            @Override
            public void handleEvent(Object eventType, Object data) {
                addFilter((CFilter)data, null);
            }
        };
        GroupFiltersPopup popup = GroupFiltersPopup.instance();
        popup.setEventHandler(listener);
        popup.showPopup((Widget)event.getSource());
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
	public void setFilters(HashMap<String, String> filters) {
		pnlFilters.clear();

        if (filters == null) {
            return;
        }

		if (filters != null) {
			for (String filterid: filters.keySet()) {
				CFilter filter = ReportUtils.findFilterById(filterid);
				if (filter != null) {
					addFilter(filter, filters.get(filterid));
				}
			}
		}
	}
	
	
	/**
	 * Return the list of declared filters
	 * @return map of filter IDs and its value
	 */
	public HashMap<String, String> getFilters() {
        HashMap<String, String> filters = new HashMap<String, String>();
        for (int i = 0; i < pnlFilters.getWidgetCount(); i++) {
            FilterBox fb = (FilterBox)pnlFilters.getWidget(i);
            String val = fb.getFilterValue();
            if (val != null) {
                filters.put(fb.getFilter().getId(), val);
            }
        }
		return filters.keySet().size() > 0? filters: null;
	}

}
