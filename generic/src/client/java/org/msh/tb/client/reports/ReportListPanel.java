/**
 * 
 */
package org.msh.tb.client.reports;

import java.util.ArrayList;

import org.msh.tb.client.commons.AnchorData;
import org.msh.tb.client.shared.model.CReport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Ricardo Memoria
 *
 */
public class ReportListPanel extends Composite {
	private static final Binder binder = GWT.create(Binder.class);
	interface Binder extends UiBinder<VerticalPanel, ReportListPanel> { };

	private VerticalPanel content;
	private int index = 0;
	private ClickHandler reportClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			reportClickHandler(event);
		}
	};
	
	@UiField FlexTable tblReports;
	@UiField Button btnClose;
	
	
	/**
	 * Default constructor
	 */
	private ReportListPanel() {
		content = binder.createAndBindUi(this);
		initWidget(content);
		content.setVisible(false);
		btnClose.addStyleName("btn-alt");
	}
	
	
	/**
	 * Display the panel and show the list of reports
	 */
	public void show() {
		tblReports.removeAllRows();
		index = 0;
		if (index == 0) {
			ArrayList<CReport> lst = MainPage.instance().getReportUI().getReports();
			addReport("New report", true, false);
			for (CReport rep: lst) {
				addReport(rep.getTitle(), false, rep.isMyReport());
			}
		}
		content.setVisible(true);
	}
	
	
	/**
	 * Hide the panel
	 */
	public void hide() {
		content.setVisible(false);
	}
	
	/**
	 * Add a new report to the list of reports
	 * @param title
	 * @param newRep
	 * @param myReport
	 */
	protected void addReport(String title, boolean newRep, boolean myReport) {
		String sicon = "<i class='";
		if (newRep) {
			sicon += "icon-file-alt";
		}
		else {
			sicon += "icon-table";
		}
		sicon += " repsymbol'></i>";
		
		SafeHtml html = SafeHtmlUtils.fromTrustedString(sicon + title);
		AnchorData lnk = new AnchorData(html);
		
		lnk.addClickHandler(reportClickHandler);
		lnk.setData(index);
		
		int row = index / 2;
		int col = index % 2;
		
		tblReports.setWidget(row, col, lnk);
		index++;
	}
	
	/**
	 * Called when the user clicks on the cancel button 
	 * @param event instance of {@link ClickEvent} class
	 */
	@UiHandler("btnClose")
	public void btnCancelClick(ClickEvent event) {
		MainPage.instance().closeReportList();
	}
	
	
	public void reportClickHandler(ClickEvent event) {
		AnchorData lnk = (AnchorData)event.getSource();
		Integer value = (Integer)lnk.getData();
		if (value == 0) {
			MainPage.instance().newReport();
		}
	}
}
