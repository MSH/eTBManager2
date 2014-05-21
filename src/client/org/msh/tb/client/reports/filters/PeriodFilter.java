package org.msh.tb.client.reports.filters;

import java.util.Date;

import org.msh.tb.client.reports.MainPage;
import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Filter that enable the user to select a period of initial and final date,
 * selecting the month and year
 * 
 * @author Ricardo Memoria
 *
 */
public class PeriodFilter extends FilterWidget {

	private HorizontalPanel panel = new HorizontalPanel();
	
	private ListBox iniMonth, iniYear, endMonth, endYear;
	
	/**
	 * Default constructor
	 */
	public PeriodFilter() {
		super();

		iniMonth = createMonthListbox();
		panel.add(iniMonth);
		
		iniYear = createYearListbox();
		panel.add(iniYear);

		panel.add(new Label(MainPage.getMessages().until()));
		
		endMonth = createMonthListbox();
		panel.add(endMonth);

		endYear = createYearListbox();
		panel.add(endYear);

		initWidget(panel);
	}
	
	/** {@inheritDoc}
	 */
	public void initialize(CFilter filter) {
		super.initialize(filter);
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

		Date dt = MainPage.instance().getReportUI().getCurrentDate();
		String syear = DateTimeFormat.getFormat("yyyy").format(dt);
		int year = Integer.parseInt(syear);
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

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		
	}

}
