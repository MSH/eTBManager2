/**
 * 
 */
package org.msh.tb.client.reports;

import java.util.ArrayList;

import org.msh.tb.client.commons.AnchorData;
import org.msh.tb.client.commons.StandardCallback;
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
	 * Display the panel and show the list of org.msh.reports
	 * @param reload informs the panel to reload the list of org.msh.reports from the server
	 */
	public void show(boolean reload) {
		tblReports.removeAllRows();
		if (reload) {
			ArrayList<CReport> lst = ReportMain.instance().getReportUI().getReports();
			updateList(lst);
		}
		else {
			// call server for the list of org.msh.reports
			ReportMain.instance().getService().getReportList(new StandardCallback<ArrayList<CReport>>() {
				@Override
				public void onSuccess(ArrayList<CReport> result) {
					updateList(result);
				}
			});
		}
		content.setVisible(true);
	}
	
	
	/**
	 * Update the list of org.msh.reports displayed in the table
	 * @param lst
	 */
	protected void updateList(ArrayList<CReport> lst) {
		tblReports.removeAllRows();
		index = 0;
		
		addReport(null);
		for (CReport rep: lst) {
			addReport(rep);
		}
	}
	
	
	/**
	 * Hide the panel
	 */
	public void hide() {
		content.setVisible(false);
	}
	
	/**
	 * Add a new report to the list of org.msh.reports
	 * @param title
	 * @param newRep
	 * @param myReport
	 */
	protected void addReport(CReport rep) {
		String sicon = "<i class='";
		if (rep == null) {
			sicon += "icon-file-alt";
		}
		else {
			sicon += "icon-table";
		}
		sicon += " repsymbol'></i>";

		String title = rep != null? rep.getTitle(): "New report";
		SafeHtml html = SafeHtmlUtils.fromTrustedString(sicon + title);
		AnchorData lnk = new AnchorData(html);
		
		lnk.addClickHandler(reportClickHandler);
		if (rep != null) {
			lnk.setData(rep.getId());
		}
		
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
		ReportMain.instance().closeReportList();
	}
	
	
	/**
	 * Called when the user clicks on a report link
	 * @param event instance of {@link ClickEvent}
	 */
	public void reportClickHandler(ClickEvent event) {
		AnchorData lnk = (AnchorData)event.getSource();
		Integer repId = (Integer)lnk.getData();
		if (repId == null) {
			ReportMain.instance().newReport();
		}
		else {
			ReportMain.instance().openReport(repId);
		}
	}
}
