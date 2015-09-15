package org.msh.tb.client.reports.filters;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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

	private ListBox lbOptions;
    private MultiSelectionBox selbox;
    private boolean multisel;

	public OptionsFilter(boolean multisel) {
        this.multisel = multisel;

        if (multisel) {
            selbox = new MultiSelectionBox();
            selbox.setWidth("400px");
            selbox.addChangeHandler(new MultiSelectionBox.ChangeHandler() {
                @Override
                public void onChange(MultiSelectionBox box) {
                    notifyFilterChange();
                }
            });
            initWidget(selbox);
        }
        else {
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
        }
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
        if (multisel) {
            for (CItem item: options) {
                selbox.add(item.getLabel(), item.getValue());
            }

            // select values, if any
            if (value != null) {
                String[] vals = value.split(";");
                selbox.selectValues(vals);
            }
        }
        else {
            fillListOptions(lbOptions, options, value);
        }
	}
	
	
	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.filters.FilterWidget#getValue()
	 */
	@Override
	public String getValue() {
        if (multisel) {
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
        }
        else {
            int index = lbOptions.getSelectedIndex();
            if (index <= 0)
                return null;
            else return lbOptions.getValue(index);
        }
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.filters.FilterWidget#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
        if (multisel) {
            if (value == null) {
                selbox.clearSelection();
                return;
            }

            String[] vals = value.split(";");

            selbox.selectValues(vals);
        }
        else {
            if (value == null) {
                lbOptions.setSelectedIndex(0);
                return;
            }

            for (int i = 0; i < lbOptions.getItemCount(); i++) {
                if (lbOptions.getValue(i).equals(value)) {
                    lbOptions.setSelectedIndex(i);
                    break;
                }
            }
        }
	}
	
}
