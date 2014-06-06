/**
 * 
 */
package org.msh.tb.client.reports;


import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CReport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Ricardo Memoria
 *
 */
public class SaveDlg extends Composite {
	private static final Binder binder = GWT.create(Binder.class);
	private static final SaveDlg myinstance = new SaveDlg();

	interface Binder extends UiBinder<DialogBox, SaveDlg> { };
	
	private boolean saveAs;
	private DialogBox dialogBox;
	
	@UiField TextBox edtTitle;
	@UiField Button btnSave;

	public SaveDlg() {
		dialogBox = binder.createAndBindUi(this);
		btnSave.addStyleName("btn-alt");
	}
	
	
	public static void openDialog(boolean saveAs) {
		myinstance.open(saveAs);
	}
	
	/**
	 * Open dialog box
	 */
	protected void open(boolean saveAs) {
		this.saveAs = saveAs;
		edtTitle.setText( MainPage.instance().getReport().getTitle() );
		dialogBox.center();
		dialogBox.show();
		edtTitle.setFocus(true);
	}
	
	/**
	 * Called when user clicks on the save button
	 * @param event
	 */
	@UiHandler("btnSave")
	public void btnSaveClick(ClickEvent event) {
		// get the report title
		String title = edtTitle.getText();
		if ((title == null) || (title.trim().isEmpty())) {
			Window.alert("The report name must be informed");
			edtTitle.setFocus(true);
			return;
		}
		
		// declare callback function to be called after report is saved
		StandardCallback callback = new StandardCallback<CReport>() {
			@Override
			public void onSuccess(CReport result) {
				dialogBox.hide();
			}
		};
		
		if (saveAs) {
			ReportCRUDServices.saveReportAs(title, callback);
		}
		else {
			ReportCRUDServices.saveNewReport(title, callback);
		}
	}
	
	@UiHandler("btnCancel")
	public void btnCancelClick(ClickEvent event) {
		dialogBox.hide();
	}
}
