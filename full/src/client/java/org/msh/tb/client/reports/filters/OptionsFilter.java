package org.msh.tb.client.reports.filters;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CItem;
import org.msh.tb.client.ui.MultiSelectionBox;

import java.util.List;

/**
 * Create a filter with a selection box where user can select one option
 * @author Ricardo Memoria
 *
 */
public class OptionsFilter extends FilterWidget {

//	private ListBox lbOptions;
    private MultiSelectionBox selbox;

	public OptionsFilter() {
        selbox = new MultiSelectionBox();
        selbox.setWidth("400px");
        selbox.addChangeHandler(new MultiSelectionBox.ChangeHandler() {
            @Override
            public void onChange(MultiSelectionBox box) {
                notifyFilterChange();
            }
        });

/*
		lbOptions = new ListBox();
		lbOptions.setVisibleItemCount(1);
		lbOptions.setWidth("300px");

        lbOptions.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                notifyFilterChange();
            }
        });
		initWidget(lbOptions);
*/
        initWidget(selbox);
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.filters.FilterWidget#initialize(org.msh.tb.client.shared.model.CFilter)
	 */
	@Override
	public void initialize(CFilter filter, String value) {
		super.initialize(filter, value);
		
		if (filter.getOptions() == null)
			return;
		fillOptions(filter.getOptions(), value);
	}

	
	/**
	 * Fill the options of the selection box from a list of options
	 * @param options
	 */
	protected void fillOptions(List<CItem> options, String value) {
		for (CItem item: options) {
            selbox.add(item.getLabel(), item.getValue());
        }
        //fillListOptions(lbOptions, options, value);
	}
	
	
	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.filters.FilterWidget#getValue()
	 */
	@Override
	public String getValue() {
        List<Object> sels = selbox.getSelectedValues();

        if (sels.size() == 0) {
            return null;
        }

        if (sels.size() == 1) {
            return sels.get(0).toString();
        }

        String s = "";
        for (Object val: sels) {
            if (!s.isEmpty()) {
                s += ";";
            }
            s += val.toString();
        }

        return s;
/*
		int index = lbOptions.getSelectedIndex();
		if (index <= 0)
			return null;
		else return lbOptions.getValue(index);
*/
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.filters.FilterWidget#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		if (value == null) {
            selbox.clearSelection();
			return;
		}

        String[] vals = value.split(";");

        selbox.selectValues(vals);
	}
	
}
