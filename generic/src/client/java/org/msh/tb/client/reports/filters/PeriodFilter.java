package org.msh.tb.client.reports.filters;

import java.util.Date;

import org.msh.tb.client.reports.MainPage;
import org.msh.tb.client.shared.model.CFilter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Filter that enable the user to select a period of initial and final date,
 * selecting the month and year
 * 
 * @author Ricardo Memoria
 *
 */
public class PeriodFilter extends FilterWidget {

	public enum PeriodFilterType { MONTHYEAR, FIXED };
	
	private HorizontalPanel panel = new HorizontalPanel();
	
	private PeriodFilterType type = PeriodFilterType.MONTHYEAR;
	private HorizontalPanel pnlMonthYear;
	private HorizontalPanel pnlFixedPeriod;
	
	private ListBox iniMonth, iniYear, endMonth, endYear, cbFixedOption;
	
	/**
	 * Default constructor
	 */
	public PeriodFilter() {
		super();

		Anchor lnkToggle = new Anchor(SafeHtmlUtils.fromTrustedString( "<i class='icon-th-large' />"));
		lnkToggle.setStyleName("toggle-button");
		lnkToggle.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clickToggleButton();
			}
		});
		panel.add(lnkToggle);
		
		initWidget(panel);
	}
	
	/**
	 * Called when the user clicks on the button to switch the period selection
	 */
	protected void clickToggleButton() {
		if (type == PeriodFilterType.MONTHYEAR) {
			type = PeriodFilterType.FIXED;
		}
		else {
			type = PeriodFilterType.MONTHYEAR;
		}
		updatePeriodType();
	}


	/** {@inheritDoc}
	 */
	public void initialize(CFilter filter) {
		super.initialize(filter);
		updatePeriodType();
	}


	protected void updatePeriodType() {
		if (type == PeriodFilterType.FIXED) {
			displayFixedFilter();
		}
		else {
			displayMonthYearFilter();
		}
	}


	/**
	 * Display the month/year selection filter
	 */
	private void displayMonthYearFilter() {
		if (pnlMonthYear == null) {
			pnlMonthYear = new HorizontalPanel();
			iniMonth = createMonthListbox();
			pnlMonthYear.add(iniMonth);
			
			iniYear = createYearListbox();
			pnlMonthYear.add(iniYear);

			pnlMonthYear.add(new Label(MainPage.getMessages().until()));
			
			endMonth = createMonthListbox();
			pnlMonthYear.add(endMonth);

			endYear = createYearListbox();
			pnlMonthYear.add(endYear);
		}
		setPeriodPanel(pnlMonthYear);
	}

	
	/**
	 * Set the new filter panel displayed in the filter
	 * @param pnl the widget to be displayed
	 */
	protected void setPeriodPanel(Widget pnl) {
		if (panel.getWidgetCount() == 2) {
			if (panel.getWidget(0) == pnl) {
				return;
			}
			panel.remove(0);
		}
		
		panel.insert(pnl, 0);
	}
	
	/**
	 * Display the fixed options filter
	 */
	private void displayFixedFilter() {
		if (pnlFixedPeriod == null) {
			pnlFixedPeriod = new HorizontalPanel();
			
			cbFixedOption = new ListBox();
			cbFixedOption.setVisibleItemCount(1);
			pnlFixedPeriod.add(cbFixedOption);
			cbFixedOption.addItem("-");
			cbFixedOption.addItem(MainPage.getMessages().fixedPeriodLAST_3MONTHS(), "0");
			cbFixedOption.addItem(MainPage.getMessages().fixedPeriodLAST_6MONTHS(), "1");
			cbFixedOption.addItem(MainPage.getMessages().fixedPeriodLAST_12MONTHS(), "2");
			cbFixedOption.addItem(MainPage.getMessages().fixedPeriodPREVIOUS_QUARTER(), "3");
			cbFixedOption.addItem(MainPage.getMessages().fixedPeriodPREVIOUS_YEAR(), "4");
		}
		setPeriodPanel(pnlFixedPeriod);
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
		if (type == PeriodFilterType.MONTHYEAR) {
			return "M," + getSelectedValue(iniMonth) + "," + getSelectedValue(iniYear) + "," + 
					getSelectedValue(endMonth) + "," + getSelectedValue(endYear);
		}
		else {
			return "F," + getSelectedValue(cbFixedOption);
		}
	}
	
	protected String getSelectedValue(ListBox lb) {
		return (lb.getSelectedIndex() > 0?  lb.getValue(lb.getSelectedIndex()) : "");
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		
	}

}
