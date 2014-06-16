/**
 * 
 */
package org.msh.tb.client.reports;

import org.msh.tb.client.shared.model.CReport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author Ricardo Memoria
 *
 */
public class OptionsDlg extends Composite {
	private static final Binder binder = GWT.create(Binder.class);
	private static OptionsDlg myinstance;

	interface Binder extends UiBinder<DialogBox, OptionsDlg> { };

	private DialogBox dialogBox;
	private CReport report;
	
	@UiField CheckBox chkPublished;
	@UiField CheckBox chkDashboard;
	@UiField Button btnSave;
	
	/**
	 * Default constructor
	 */
	public OptionsDlg() {
		dialogBox = binder.createAndBindUi(this);
		btnSave.addStyleName("btn-alt");
	}
	
	/**
	 * Create the dialog, if necessary, and open it
	 * @param report
	 */
	public static void open(CReport report) {
		if (myinstance == null) {
			myinstance = new OptionsDlg();
		}
		
		myinstance.openDialog(report);
	}
	
	/**
	 * Set the content and open the dialog
	 * @param report
	 */
	protected void openDialog(CReport report) {
		this.report = report;

		chkDashboard.setValue(report.isDashboard());
		chkPublished.setValue(report.isPublished());

		dialogBox.center();
		dialogBox.show();
	}
	
	@UiHandler("btnSave")
	public void btnSaveClick(ClickEvent evt) {
		report.setPublished(chkPublished.getValue());
		report.setDashboard(chkDashboard.getValue());
		dialogBox.hide();
		ReportMain.instance().lnkSaveClick(null);
	}
	
	@UiHandler("btnCancel")
	public void btnCancel(ClickEvent evt) {
		dialogBox.hide();
	}
}
