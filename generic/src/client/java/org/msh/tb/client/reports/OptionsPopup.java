/**
 * 
 */
package org.msh.tb.client.reports;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Popup menu displaying the options of the report
 * @author Ricardo Memoria
 *
 */
public class OptionsPopup extends PopupPanel {
	private static final Binder binder = GWT.create(Binder.class);
	interface Binder extends UiBinder<HTMLPanel, OptionsPopup> { };

	@UiField HTMLPanel pnl; 
	
	/**
	 * Default constructor
	 */
	public OptionsPopup() {
		binder.createAndBindUi(this);
	
		add(pnl);
		setAutoHideEnabled(true);
		setStyleName("");
	}

	
	/**
	 * Called when user clicks on the New report option
	 * @param event
	 */
	@UiHandler("lnkNew")
	protected void cmdNewReport(ClickEvent event) {
		hide();
		MainPage.instance().newReport();
	}
	
	/**
	 * Called when user clicks on the save as item
	 * @param event instance of {@link ClickEvent}
	 */
	@UiHandler("lnkSaveAs")
	protected void cmdSaveAs(ClickEvent event) {
		saveReport(true);
	}
	
	/**
	 * Open the save dialog to save the current report
	 * @param saveAs indicate if it's going to save the report or save it as
	 */
	protected void saveReport(boolean saveAs) {
		hide();
		SaveDlg.openDialog(saveAs);
	}
}
