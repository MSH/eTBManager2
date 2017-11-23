/**
 * 
 */
package org.msh.tb.client.reports;


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
import org.msh.tb.client.AppResources;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CReport;

/**
 * @author Ricardo Memoria
 *
 */
public class SaveDlg extends Composite {
	private static final Binder binder = GWT.create(Binder.class);
	private static final SaveDlg myinstance = new SaveDlg();

    private static final String RESOURCE_KEY = "report.ui.savedlg";

    public enum SaveDlgEvent { SAVE, SAVEAS, CANCEL };

	interface Binder extends UiBinder<DialogBox, SaveDlg> { };
	
	private boolean saveAs;
	private DialogBox dialogBox;
    private StandardEventHandler eventHandler;
    private CReport report;

	@UiField TextBox edtTitle;
	@UiField Button btnSave;

    /**
     * Default constructor
     */
	public SaveDlg() {
		dialogBox = binder.createAndBindUi(this);
		btnSave.addStyleName("btn-alt");
	}


    /**
     * Create and open the dialog
     * @param report the instance of CReport to save
     * @param saveAs if true, the user will save the report as...
     * @param eventHandler event handle to be notified when it's done
     */
	public static void openDialog(CReport report, boolean saveAs, StandardEventHandler eventHandler) {
        SaveDlg dlg = (SaveDlg)AppResources.instance().get(RESOURCE_KEY);
        if (dlg == null){
            dlg = new SaveDlg();
            AppResources.instance().set(RESOURCE_KEY, dlg);
        }
        dlg.open(report, saveAs, eventHandler);
    }

    /**
     * Create and open the dialog
     * @param report the instance of CReport to save
     * @param saveAs if true, the user is saving the report as...
     * @param eventHandler event handle to be notified when it's done
     */
	protected void open(CReport report, boolean saveAs, StandardEventHandler eventHandler) {
        this.report = report;
		this.saveAs = saveAs;
        this.eventHandler = eventHandler;

		edtTitle.setText( report.getTitle() );
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

        report.setTitle(title);

        if (saveAs) {
            fireEvent(SaveDlgEvent.SAVEAS);
        }
        else {
            fireEvent(SaveDlgEvent.SAVE);
        }
    	dialogBox.hide();
	}
	
	@UiHandler("btnCancel")
	public void btnCancelClick(ClickEvent event) {
		dialogBox.hide();
        fireEvent(SaveDlgEvent.CANCEL);
	}

    /**
     * Notify the event handler about dialog event
     * @param evt type of event
     */
    private void fireEvent(SaveDlgEvent evt) {
        if (eventHandler != null) {
            eventHandler.handleEvent(evt, null);
        }
    }

    public CReport getReport() {
        return report;
    }

    public void setReport(CReport report) {
        this.report = report;
    }
}
