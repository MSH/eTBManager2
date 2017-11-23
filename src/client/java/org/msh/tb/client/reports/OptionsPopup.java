/**
 * 
 */
package org.msh.tb.client.reports;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.ui.MenuPanel;

/**
 * Popup menu displaying the options of the report
 * @author Ricardo Memoria
 *
 */
public class OptionsPopup extends PopupPanel {
	private static final Binder binder = GWT.create(Binder.class);
	interface Binder extends UiBinder<MenuPanel, OptionsPopup> { };

    public enum Event { SAVE, SAVEAS, NEWREPORT, ADDINDICATOR, DELETE, SETTINGS };

    private StandardEventHandler eventHandler;

    @UiField MenuPanel pnlMenu;

	/**
	 * Default constructor
	 */
	public OptionsPopup() {
		binder.createAndBindUi(this);
	
		setAutoHideEnabled(true);
        add(pnlMenu);
	}


	/**
	 * Called when user clicks on the New report option
	 * @param event information about the click event
	 */
	@UiHandler("lnkNew")
	protected void cmdNewReport(ClickEvent event) {
        fireCommandEvent(Event.NEWREPORT);
	}

    /**
     * Called when user clicks on the Add Indicator option
     * @param event information about the click event
     */
    @UiHandler("lnkAddIndicator")
    protected void cmdAddIndicator(ClickEvent event) {
        fireCommandEvent(Event.ADDINDICATOR);
    }
	
	/**
	 * Called when user clicks on the save as item
	 * @param event instance of {@link ClickEvent}
	 */
	@UiHandler("lnkSaveAs")
	protected void cmdSaveAs(ClickEvent event) {
        fireCommandEvent(Event.SAVEAS);
	}
	
	/**
	 * Called when user clicks on the settings item
	 * @param event instance of {@link ClickEvent}
	 */
	@UiHandler("lnkSettings")
	protected void cmdSettings(ClickEvent event) {
        fireCommandEvent(Event.SETTINGS);
	}

    /**
     * Called when user clicks on the save command
     * @param event instance of {@link ClickEvent}
     */
    @UiHandler("lnkSave")
    protected void cmdSave(ClickEvent event) {
        fireCommandEvent(Event.SAVE);
    }

    /**
     * Called when user clicks on the delete command
     * @param event instance of {@link ClickEvent}
     */
    @UiHandler("lnkDelete")
    protected void cmdDelete(ClickEvent event) {
        fireCommandEvent(Event.DELETE);
    }

	/**
	 * Open the save dialog to save the current report
	 * @param saveAs indicate if it's going to save the report or save it as
	 */
	protected void saveReport(boolean saveAs) {
        fireCommandEvent(Event.SAVE);
	}

    /**
     * Fire event informing the event handler about a command event
     * @param evt
     */
    protected void fireCommandEvent(Event evt) {
        hide();
        if (eventHandler != null) {
            eventHandler.handleEvent(evt, this);
        }
    }

    public StandardEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(StandardEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}
