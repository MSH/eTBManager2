package org.msh.tb.client.reports.filters;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.ReportServiceAsync;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CItem;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterWidget extends Composite {

	private CFilter filter;
    private StandardEventHandler eventHandler;

    private boolean initializing;

	/**
	 * Initialize the filter
	 * @param filterData is the instance of {@link CFilter} class
	 * @param iniValue is the value of the filter. Enter null if no filter is selected
	 */
	public void initialize(CFilter filterData, String iniValue) {
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
	
	/**
	 * Fill a {@link ListBox} widget with a given list of options
	 * @param lb instance of {@link ListBox} to be filled
	 * @param options list of {@link CItem} options
	 */
	protected void fillListOptions(ListBox lb, List<CItem> options, String value) {
        setInitializing(true);
        try {
            lb.clear();
            lb.addItem("-");
            for (CItem opt: options) {
                lb.addItem(opt.getLabel(), opt.getValue());
                if ((value != null) && (value.equals(opt.getValue()))) {
                    lb.setSelectedIndex(lb.getItemCount() - 1);
                }
            }
        }
        finally {
            setInitializing(false);
        }
	}
	
	/**
	 * Retrieve a list of options from the server. The param argument will be sent to the server
	 * and depends on the filter type and server interpretation
	 * @param param is the parameter to be sent to the server
	 * @param callback {@link AsyncCallback} function that will be called when the server responds
	 */
	protected void loadServerOptions(String param, AsyncCallback<ArrayList<CItem>> callback) {
		ReportServiceAsync srv = AppResources.reportServices();
		srv.getFilterOptions(filter.getId(), param, callback);
	}

    /**
     * Called when the filter changes its content
     */
    protected void notifyFilterChange() {
        if ((eventHandler != null) || (initializing)) {
            eventHandler.handleEvent(this, null);
        }
    }

    public StandardEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(StandardEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public boolean isInitializing() {
        return initializing;
    }

    public void setInitializing(boolean initializing) {
        this.initializing = initializing;
    }
}
