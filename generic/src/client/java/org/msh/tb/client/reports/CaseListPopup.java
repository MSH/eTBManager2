package org.msh.tb.client.reports;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.App;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CPatient;
import org.msh.tb.client.shared.model.CPatientList;

import java.util.HashMap;

/**
 * Display the list of cases in a popup window
 * 
 * @author Ricardo Memoria
 *
 */
public class CaseListPopup extends PopupPanel {

	interface Binder extends UiBinder<VerticalPanel, CaseListPopup> { }
	private static final Binder binder = GWT.create(Binder.class);

    private static final String RESOURCE_KEY = "report.ui.caseListPopup";

	private VerticalPanel panel;
	@UiField HTML txtResult;
	@UiField FlexTable table;
	@UiField HTMLPanel pnlWait;
	@UiField HTMLPanel pnlContent;

	
	private int page;
	private long recordCount;
	private int pageSize;
	private HashMap<String, String> filters;

	
	public CaseListPopup() {
		super(true);
		panel = binder.createAndBindUi(this);

		setStyleName("caselist-popup");
		
		add(panel);
	}
	
	/**
	 * Open the popup window and send a request to the server using the filters
	 * passed as argument
	 * @param filters is the list of filter and values to search for the cases
	 */
	public void showPatients(HashMap<String, String> filters) {
		this.filters = filters;
		page = 0;
		pnlContent.setVisible(false);
		center();
		updatePageContent();
	}

	
	/**
	 * Called when the user clicks on the close icon of the pop up window
	 * @param event the {@link ClickEvent} instance
	 */
	@UiHandler("lnkClose")
	public void lnkCloseClick(ClickEvent event) {
		hide();
	}
	
	/**
	 * Update the page content by calling the server
	 */
	private void updatePageContent() {
		table.removeAllRows();
		pnlWait.setVisible(true);
        AppResources.reportServices().getPatients(filters, page, new StandardCallback<CPatientList>() {
			@Override
			public void onSuccess(CPatientList lst) {
				updatePatientList(lst);
				if (page == 0)
					center();
			}
		});
	}

	/**
	 * Update the list of patients being displayed
	 * @param lst
	 */
	protected void updatePatientList(CPatientList lst) {
		pnlWait.setVisible(false);
		pnlContent.setVisible(true);

		table.setVisible(lst != null);
		if (lst == null) {
			return;
		}

		int col = 0;
		int row = 0;
		for (CPatient pat: lst.getItems()) {
			String genderIcon;
			if (pat.getGender() != null) {
				genderIcon = "<div class='" + (pat.getGender() == 0? "male" : "female") + "-icon'></div>"; 
			}
			else genderIcon = "";
			table.setHTML(row, col, genderIcon + pat.getName() + "<div class='text-small'>" + patientNumber(pat) + "</div>");
			col++;
			if (col > 1) {
				col = 0;
				row++;
			}
		}

		recordCount = lst.getRecordCount();
		pageSize = lst.getPageSize();
		NumberFormat nf = NumberFormat.getDecimalFormat();
		int iniResult = page * pageSize + 1;
		long lastResult = iniResult + (pageSize - 1);
		if (lastResult > recordCount)
			lastResult = recordCount;
		String s = nf.format(iniResult) + " - " + nf.format(lastResult) + 
				" " + App.messages.of() + " <b>" + nf.format(recordCount) + "</b>";
		
		txtResult.setHTML(s);
	}
	
	
	/**
	 * Called when the user clicks on the previous page link
	 * @param event
	 */
	@UiHandler("lnkNext")
	public void lnkNextClick(ClickEvent event) {
		if (pageSize * (page + 1) > recordCount)
			return;
		page++;
		updatePageContent();
	}
	
	/**
	 * Called when the user clicks on the next page link
	 * @param event
	 */
	@UiHandler("lnkPrev")
	public void lnkPrevClick(ClickEvent event) {
		if (page == 0)
			return;
		page--;
		updatePageContent();
	}
	
	/**
	 * Return the patient number
	 * @param pat
	 * @return
	 */
	private final String patientNumber(CPatient pat) {
		if (pat.getNumber() == null)
			 return App.messages.unnumbered();
		else return pat.getNumber();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#hide()
	 */
	@Override
	public void hide() {
		super.hide();
	}

    /**
     * Return the singleton instance of the class
     * @return instance of CaseListPopup
     */
    public static CaseListPopup instance() {
        CaseListPopup popup = (CaseListPopup)AppResources.instance().get(RESOURCE_KEY);
        if (popup == null) {
            popup = new CaseListPopup();
            AppResources.instance().set(RESOURCE_KEY, popup);
        }
        return popup;
    }
}
