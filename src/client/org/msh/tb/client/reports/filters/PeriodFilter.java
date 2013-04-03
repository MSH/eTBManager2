package org.msh.tb.client.reports.filters;

import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class PeriodFilter extends FilterWidget {

	private HorizontalPanel panel = new HorizontalPanel();
	
	private ListBox iniMonth, iniYear, endMonth, endYear;
	
	public PeriodFilter(CFilter filter) {
		super(filter);

		iniMonth = createMonthListbox();
		panel.add(iniMonth);
		
		iniYear = createYearListbox();
		panel.add(iniYear);

		panel.add(new Label("até"));
		
		endMonth = createMonthListbox();
		panel.add(endMonth);

		endYear = createYearListbox();
		panel.add(endYear);

		initWidget(panel);
	}


	/**
	 * Create the list of months
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected ListBox createMonthListbox() {
		ListBox lb = new ListBox();
		lb.setVisibleItemCount(1);

		String[] months = LocaleInfo.getCurrentLocale().getDateTimeConstants().shortMonths();
		int index = 1;
		lb.addItem("-", "0");
		for (String month: months)
			lb.addItem(month, Integer.toString( index++ ));
		return lb;
	}

	
	/**
	 * Fill the list of years
	 * @return
	 */
	protected ListBox createYearListbox() {
		ListBox lb = new ListBox();
		lb.setVisibleItemCount(1);

		int year = 2013;
		lb.addItem("-", "0");
		for (int i = year; i > year-20; i--) {
			String s = Integer.toString(i);
			lb.addItem(s, s);
		}
		
		return lb;
	}
	
	@Override
	public String getValue() {
		return "M," + getSelectedValue(iniMonth) + "," + getSelectedValue(iniYear) + "," + 
				getSelectedValue(endMonth) + "," + getSelectedValue(endYear);
	}
	
	protected String getSelectedValue(ListBox lb) {
		return (lb.getSelectedIndex() > 0?  lb.getValue(lb.getSelectedIndex()) : "");
	}

}
