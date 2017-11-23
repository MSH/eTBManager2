package org.msh.tb.client.reports.filters;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.App;
import org.msh.tb.client.reports.ReportUtils;
import org.msh.tb.client.shared.model.CFilter;

import java.util.Date;

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
     * Default change handler for combo boxes
     */
    private ChangeHandler changeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            notifyFilterChange();
        }
    };
	
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
	@Override
	public void initialize(CFilter filter, String value) {
		super.initialize(filter, value);
		
		if (value != null) {
			setValue(value);
		}
		else {
			type = PeriodFilterType.MONTHYEAR;
			updatePeriodType();
		}
	}


	/**
	 * Update the display according to the period type selected
	 */
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

			pnlMonthYear.add(new Label(App.messages.until()));
			
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
            cbFixedOption.addChangeHandler(changeHandler);
			cbFixedOption.setVisibleItemCount(1);
			pnlFixedPeriod.add(cbFixedOption);
			cbFixedOption.addItem("-");
			cbFixedOption.addItem(App.messages.fixedPeriodLAST_3MONTHS(), "0");
			cbFixedOption.addItem(App.messages.fixedPeriodLAST_6MONTHS(), "1");
			cbFixedOption.addItem(App.messages.fixedPeriodLAST_12MONTHS(), "2");
			cbFixedOption.addItem(App.messages.fixedPeriodPREVIOUS_QUARTER(), "3");
			cbFixedOption.addItem(App.messages.fixedPeriodPREVIOUS_YEAR(), "4");
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
        lb.addChangeHandler(changeHandler);
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
        lb.addChangeHandler(changeHandler);
		lb.setVisibleItemCount(1);

		Date dt = ReportUtils.getReportUIData().getCurrentDate();
		String syear = DateTimeFormat.getFormat("yyyy").format(dt);
		int year = Integer.parseInt(syear);
		lb.addItem("-", "0");
		for (int i = year; i > year-20; i--) {
			String s = Integer.toString(i);
			lb.addItem(s, s);
		}
		
		return lb;
	}


	/** {@inheritDoc}
	 */
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
	
	/**
	 * Return the selected value of a given list box
	 * @param lb
	 * @return
	 */
	protected String getSelectedValue(ListBox lb) {
		return (lb.getSelectedIndex() > 0?  lb.getValue(lb.getSelectedIndex()) : "");
	}

	
	/**
	 * Set the select item of a list box by its value
	 * @param lb
	 * @param value
	 */
	protected void setSelectedValue(ListBox lb, String value) {
		if (value == null) {
			lb.setSelectedIndex(0);
			return;
		}

		for (int i = 0; i < lb.getItemCount(); i++) {
			if (value.equals(lb.getValue(i))) {
				lb.setSelectedIndex(i);
				return;
			}
		}
		
		lb.setSelectedIndex(0);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public void setValue(String value) {
		if (value == null) {
			return;
		}
		String[] s = value.split(",");
		type = "M".equals(s[0]) ? PeriodFilterType.MONTHYEAR: PeriodFilterType.FIXED;

		updatePeriodType();
		if (type == PeriodFilterType.FIXED) {
			setSelectedValue(cbFixedOption, s[1]);
		}
		else {
			setSelectedValue(iniMonth, s[1]);
			setSelectedValue(iniYear, s[2]);
			setSelectedValue(endMonth, s[3]);
			setSelectedValue(endYear, s[4]);
		}
	}

}
