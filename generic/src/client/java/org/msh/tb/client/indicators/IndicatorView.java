package org.msh.tb.client.indicators;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Created by ricardo on 10/07/14.
 */
public class IndicatorView extends Composite {
    interface IndicatorViewUiBinder extends UiBinder<HTMLPanel, IndicatorView> {
    }
    private static IndicatorViewUiBinder ourUiBinder = GWT.create(IndicatorViewUiBinder.class);

    /**
     * The indicator to render the view
     */
    private IndicatorController controller;

//    @UiField ResultView resultView;
    @UiField Label txtTitle;


    /**
     * Default constructor
     */
    public IndicatorView() {
        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
    }

    /**
     * Update the content of the indicator view with the given indicator
     * @param controller instance of {@link org.msh.tb.client.indicators.IndicatorController}
     */
    public void update(IndicatorController controller) {
        this.controller = controller;
    }

    /**
     * Called when the user tries to remove this indicator from the list of indicators
     * @param evt
     */
    @UiHandler("btnRemove")
    public void btnRemoveClick(ClickEvent evt) {
        Window.alert("Not implemented");
    }
}