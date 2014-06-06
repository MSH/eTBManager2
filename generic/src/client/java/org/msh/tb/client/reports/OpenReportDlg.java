/**
 * 
 */
package org.msh.tb.client.reports;

import java.util.ArrayList;

import org.msh.tb.client.shared.model.CReport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author Ricardo Memoria
 *
 */
public class OpenReportDlg {
	private static final Binder binder = GWT.create(Binder.class);
	interface Binder extends UiBinder<DialogBox, OpenReportDlg> { };
	private static OpenReportDlg myinstance;

	private DialogBox dialogBox;
	int index = 0;
	
	@UiField FlexTable tblReports;

	private OpenReportDlg() {
		dialogBox = binder.createAndBindUi(this);
	}
	
	public static void openDialog() {
		if (myinstance == null) {
			myinstance = new OpenReportDlg();
		}
		
		myinstance.startOpening();
	}
	
	
	/**
	 * Open the dialog
	 */
	protected void startOpening() {
		if (index == 0) {
			ArrayList<CReport> lst = MainPage.instance().getReportUI().getReports();
			for (int i = 0; i < 30; i++) {
				addReport("New report", true, false);
				for (CReport rep: lst) {
					addReport(rep.getTitle(), false, rep.isMyReport());
				}
			}
		}
		dialogBox.center();
		dialogBox.show();
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
		Anchor lnk = new Anchor(html);
		
		int row = index / 2;
		int col = index % 2;
		
		tblReports.setWidget(row, col, lnk);
		index++;
	}
	
	/**
	 * Called when the user clicks on the cancel button 
	 * @param event instance of {@link ClickEvent} class
	 */
	@UiHandler("btnCancel")
	public void btnCancelClick(ClickEvent event) {
		dialogBox.hide();
	}
}
