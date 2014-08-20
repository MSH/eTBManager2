package org.msh.tb.client.indicators;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import org.msh.tb.client.commons.StandardEventHandler;

/**
 * Abstract class that wraps an indicator display (ResultView).
 * Base class for the {@link org.msh.tb.client.indicators.IndicatorView} and
 * {@link org.msh.tb.client.indicators.IndicatorEditor} classes
 *
 * Created by Ricardo on 21/07/2014.
 */
public abstract class IndicatorWrapperPanel extends Composite {

    /**
     * Events fired by this class
     */
    public enum IndicatorEvent {
        REMOVE, CLOSE, EDIT
    }

    /**
     * The indicator to render the view
     */
    private IndicatorController controller;

    /**
     * The standard event handler
     */
    private StandardEventHandler eventHandler;


    /**
     * Update the indicator passing its controller
     * @param callback callback function called when the indicator is updated
     */
    public void update(AsyncCallback callback) {
        updateIndicator(callback);
    }

    /**
     * Called when it's necessary to update the indicator content
     */
    public abstract void updateIndicator(AsyncCallback<ResultView> callback);

    /**
     * Fire an event, calling the event handler that was registered to
     * receive events from this panel
     * @param event the indicator event
     */
    protected void fireIndicatorEvent(IndicatorEvent event) {
        if (eventHandler != null) {
            eventHandler.handleEvent(event, this);
        }
    }

    /**
     * Indicate if the panel wrapping the indicator has its own style. If it
     * returns true, the style will not be overwritten (for example, editor panel)
     * @return true if panel applies its own style
     */
    public boolean isProprietaryStyle() {
        return false;
    }

    public IndicatorController getController() {
        return controller;
    }

    public void setController(IndicatorController controller) {
        this.controller = controller;
    }

    public StandardEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(StandardEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}
