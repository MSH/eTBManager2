package org.msh.tb.client.reports.filters;

import java.util.List;

import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CItem;

import com.google.gwt.user.client.ui.ListBox;

/**
 * Create a filter with a selection box where user can select one option
 * @author Ricardo Memoria
 *
 */
public class OptionsFilter extends FilterWidget {

	private ListBox lbOptions;

	public OptionsFilter() {
		lbOptions = new ListBox();
		lbOptions.setVisibleItemCount(1);
		lbOptions.setWidth("300px");
		initWidget(lbOptions);
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.client.reports.filters.FilterWidget#initialize(org.msh.tb.client.shared.model.CFilter)
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
		fillListOptions(lbOptions, options, value);
	}
	
	
	/* (non-Javadoc)
	 * @see org.msh.tb.client.reports.filters.FilterWidget#getValue()
	 */
	@Override
	public String getValue() {
		int index = lbOptions.getSelectedIndex();
		if (index <= 0)
			return null;
		else return lbOptions.getValue(index);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.client.reports.filters.FilterWidget#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		if (value == null) {
			lbOptions.setSelectedIndex(0);
			return;
		}

		for (int i = 0; i < lbOptions.getItemCount(); i++)
			if (lbOptions.getValue(i).equals(value)) {
				lbOptions.setSelectedIndex(i);
				break;
			}
	}
	
}
