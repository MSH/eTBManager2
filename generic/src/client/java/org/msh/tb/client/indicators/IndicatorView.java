package org.msh.tb.client.indicators;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * Created by ricardo on 10/07/14.
 */
public class IndicatorView extends IndicatorWrapper {
    interface IndicatorViewUiBinder extends UiBinder<FocusPanel, IndicatorView> {
    }
    private static IndicatorViewUiBinder ourUiBinder = GWT.create(IndicatorViewUiBinder.class);

    @UiField ResultView resIndicator;
    @UiField HorizontalPanel pnlButtons;


    /**
     * Default constructor
     */
    public IndicatorView() {
        final FocusPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);

        pnlButtons.setVisible(false);

        // event handler
        rootElement.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                mouseIn();
            }
        });

        rootElement.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                mouseOut();
            }
        });
    }

    /**
     * Update the content of the indicator view with the given indicator
     */
    public void updateIndicator() {
        resIndicator.update(getController());
    }


    /**
     * Called when mouse is out of the panel
     */
    protected void mouseOut() {
        pnlButtons.setVisible(false);
    }

    /**
     * Called when mouse is over the panel
     */
    protected void mouseIn() {
        pnlButtons.setVisible(true);
    }

    /**
     * Called when the user tries to remove this indicator from the list of indicators
     * @param evt
     */
    @UiHandler("btnRemove")
    public void btnRemoveClick(ClickEvent evt) {
        fireIndicatorEvent(IndicatorEvent.REMOVE);
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent evt) {
        fireIndicatorEvent(IndicatorWrapper.IndicatorEvent.EDIT);
    }
}