/**
 * 
 */
package org.msh.tb.client.reports;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CReport;

/**
 * Display dialog window with options about the report
 *
 * @author Ricardo Memoria
 *
 */
public class OptionsDlg extends Composite {
	private static final Binder binder = GWT.create(Binder.class);

	interface Binder extends UiBinder<DialogBox, OptionsDlg> { };

    public enum Event { OK, CANCEL};

	private DialogBox dialogBox;
	private CReport report;
    private StandardEventHandler eventHandler;
	
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
	 * @param report instance of the report to change options
     * @param eventHandler event handler that will be notified about dialog events
	 */
	public static void open(CReport report, StandardEventHandler eventHandler) {
        OptionsDlg dlg = (OptionsDlg) AppResources.instance().get("ui.OptionsDlg");
        if (dlg == null) {
            dlg = new OptionsDlg();
            AppResources.instance().set("ui.OptionsDlg", dlg);
        }

		dlg.openDialog(report, eventHandler);
	}
	
	/**
	 * Set the content and open the dialog
     * @param report instance of the report to change options
     * @param eventHandler event handler that will be notified about dialog events
	 */
	protected void openDialog(CReport report, StandardEventHandler eventHandler) {
		this.report = report;
        this.eventHandler = eventHandler;

		chkDashboard.setValue(report.isDashboard());
		chkPublished.setValue(report.isPublished());

		dialogBox.center();
		dialogBox.show();
	}

    /**
     * Called when user clicks on the save button
     * @param evt information about the click event
     */
	@UiHandler("btnSave")
	public void btnSaveClick(ClickEvent evt) {
		report.setPublished(chkPublished.getValue());
		report.setDashboard(chkDashboard.getValue());
		dialogBox.hide();
		eventHandler.handleEvent(Event.OK, OptionsDlg.class);
	}

    /**
     * Called when user clicks on the cancel button
     * @param evt information about the click event
     */
	@UiHandler("btnCancel")
	public void btnCancel(ClickEvent evt) {
		dialogBox.hide();
        eventHandler.handleEvent(Event.CANCEL, OptionsDlg.class);
    }
}
