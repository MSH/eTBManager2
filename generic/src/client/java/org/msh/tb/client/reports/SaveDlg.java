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
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.commons.StandardEvent;
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
     * @param eventHandler event handle to be notified when it's done
     */
	public static void openDialog(CReport report, StandardEventHandler eventHandler) {
        SaveDlg dlg = (SaveDlg)AppResources.instance().get(RESOURCE_KEY);
        if (dlg == null){
            dlg = new SaveDlg();
            AppResources.instance().set(RESOURCE_KEY, dlg);
        }
        dlg.open(report, eventHandler);
    }

    /**
     * Create and open the dialog
     * @param report the instance of CReport to save
     * @param eventHandler event handle to be notified when it's done
     */
	protected void open(CReport report, StandardEventHandler eventHandler) {
		this.saveAs = report.getId() == null;
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
		
		// declare callback function to be called after report is saved
		StandardCallback callback = new StandardCallback<CReport>() {
			@Override
			public void onSuccess(CReport result) {
				dialogBox.hide();
                fireEvent(StandardEvent.OK);
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
        fireEvent(StandardEvent.CANCEL);
	}

    /**
     * Notify the event handler about dialog event
     * @param evt type of event
     */
    private void fireEvent(StandardEvent evt) {
        if (eventHandler != null) {
            eventHandler.handleEvent(this.getClass(), evt);
        }
    }

    public CReport getReport() {
        return report;
    }

    public void setReport(CReport report) {
        this.report = report;
    }
}
