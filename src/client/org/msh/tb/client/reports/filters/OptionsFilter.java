package org.msh.tb.client.reports.filters;

import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.user.client.ui.ListBox;

public class OptionsFilter extends FilterWidget {

	private ListBox lbOptions;
	
	public OptionsFilter(CFilter filter) {
		super(filter);
		lbOptions = new ListBox();
		lbOptions.setVisibleItemCount(1);
		lbOptions.setWidth("300px");
		initWidget(lbOptions);
	}

	
	@Override
	public String getValue() {
		return null;
	}
	
}
