package org.msh.tb.client.indicators;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.ChartPopup;
import org.msh.tb.client.reports.filters.FiltersPanel;
import org.msh.tb.client.reports.variables.VariablesPanel;
import org.msh.tb.client.shared.model.CChartType;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.ui.LabelEditor;

/**
 * Display an editor panel to change indicator parameters (filters, variables, layout, chart, etc)
 * Created by ricardo on 10/07/14.
 */
public class IndicatorEditor extends IndicatorWrapperPanel implements StandardEventHandler {
    interface IndicatorEditorUiBinder extends UiBinder<HTMLPanel, IndicatorEditor> {
    }

    private static IndicatorEditorUiBinder ourUiBinder = GWT.create(IndicatorEditorUiBinder.class);

    @UiField LabelEditor txtTitle;
    @UiField VariablesPanel pnlVariables;
    @UiField ListBox lbSize;
    @UiField Anchor lnkChart;
    @UiField Anchor lnkUpdate;
    @UiField ResultView resIndicator;
    @UiField FiltersPanel pnlFilters;
    @UiField HTMLPanel pnlCommands;

    private ChartPopup chartPopup;
    private boolean initialized;

    /**
     * Default constructor
     */
    public IndicatorEditor() {
        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);

        txtTitle.setEventHandler(new StandardEventHandler() {
            @Override
            public void handleEvent(Object eventType, Object data) {
                getController().getIndicator().setTitle(txtTitle.getText());
            }
        });

        lbSize.addItem("100%");
        lbSize.addItem("50%");
//        lbSize.addItem("25%");
        lbSize.setSelectedIndex(0);
        lbSize.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                sizeChangeHandler();
            }
        });

        pnlVariables.setEventHandler(this);
        txtTitle.setEventHandler(this);

        pnlFilters.setEventHandler(this);
    }

    /**
     * Called when the user clicks on the close icon at the top-right side of the editor
     * @param event contain information about the event
     */
    @UiHandler("btnRemove")
    public void btnRemoveClick(ClickEvent event) {
        fireIndicatorEvent(IndicatorEvent.REMOVE);
    }

    @Override
    public boolean isProprietaryStyle() {
        return true;
    }

    /**
     * Display the indicator to be displayed
     */
    @Override
    public void updateIndicator(AsyncCallback<ResultView> callback) {
        CIndicator ind = getController().getIndicator();

        if (!initialized) {
            updateTitle();

            pnlVariables.setColumnVariables(ind.getColVariables());
            pnlVariables.setRowVariables(ind.getRowVariables());
            pnlFilters.setFilters(ind.getFilters());
            updateCommandBar();

            updateChartButton();

            // update size indicator
            if (ind.getSize() != null) {
                switch (ind.getSize()) {
                    case 50: lbSize.setSelectedIndex(1);
                        break;
                    case 25: lbSize.setSelectedIndex(2);
                        break;
                    default: lbSize.setSelectedIndex(0);
                }
            }
            initialized = true;
        }

        resIndicator.update(getController(), callback);
    }


    /**
     * Called when the chart type is called
     */
    @UiHandler("lnkChart")
    public void lnkChartTypeClick(ClickEvent event) {
        if (chartPopup == null) {
            chartPopup = new ChartPopup();
            chartPopup.setEventHandler(new StandardEventHandler() {
                @Override
                public void handleEvent(Object eventType, Object data) {
                    chartChangeListener((CChartType)data);
                }
            });
        }

        Widget source = (Widget)event.getSource();
        chartPopup.showRelativeTo(source);
    }


    /**
     * Called when user clicks on the update link
     * @param event contain information about the click event
     */
    @UiHandler("lnkUpdate")
    public void lnkUpdateClick(ClickEvent event) {
        IndicatorController controller = getController();
        controller.clearData();
        controller.getIndicator().setFilters( pnlFilters.getFilters() );
        updateCommandBar();
        resIndicator.update(controller, null);
    }

    /**
     * Close the editor window
     * @param event contain information about the click event
     */
    @UiHandler("btnClose")
    public void lnkClose(ClickEvent event) {
        IndicatorController controller = getController();
        controller.getIndicator().setFilters(pnlFilters.getFilters());
        fireIndicatorEvent(IndicatorEvent.CLOSE);
    }

    /**
     * update the command bar according to the indicator
     */
    private void updateCommandBar() {
        CIndicator ind = getController().getIndicator();
        pnlCommands.setVisible(ind.getColVariablesCount() + ind.getRowVariablesCount() > 0);
    }

    /**
     * Update the indicator with the values selected in the user interface
     */
    protected void uiToIndicator() {
        CIndicator ind = getController().getIndicator();
        ind.setColVariables( pnlVariables.getColumnVariables() );
        ind.setRowVariables(pnlVariables.getRowVariables());
    }

    /**
     * Notify when the chart is changed
     * @param type the new chart type
     */
    protected void chartChangeListener(CChartType type) {
        getController().getIndicator().setChartType(type);
        updateChartButton();
        resIndicator.notifyChartChange();
    }


    /**
     * Called when the user changes the view size
     */
    protected void sizeChangeHandler() {
        int size = 100;
        switch (lbSize.getSelectedIndex()) {
            case 1: size = 50;
                break;
            case 2: size = 25;
                break;
        }
        getController().getIndicator().setSize(size);
        resIndicator.notifySizeChange();
    }


    /**
     * Update the image in the chart button according to the chart type in the indicator
     */
    protected void updateChartButton() {
        Element el = (Element)lnkChart.getElement();
        if (DOM.getChildCount(el) > 0) {
            el.removeChild(DOM.getFirstChild(el));
        }

        CChartType type = getController().getIndicator().getChartType();
        if (type == null) {
            return;
        }

        ImageResource res = ChartPopup.getChartImage(type);
        Image img = new Image(res);
        DOM.insertChild(el, img.getElement(), 0);
    }


    /**
     * Called when an event happens
     * @param eventType event type
     * @param data data assigned to the event (optional)
     */
    @Override
    public void handleEvent(Object eventType, Object data) {
        if (eventType == VariablesPanel.class) {
            uiToIndicator();
            return;
        }

        if (eventType == txtTitle) {
            getController().getIndicator().setTitle((String)data);
            resIndicator.notifyTitleChange();
            return;
        }

        if (eventType == FiltersPanel.Event.FILTERS_CHANGE) {
            getController().getIndicator().setFilters(pnlFilters.getFilters());
        }
    }


    /**
     * Update the title's content with the title in the indicator object
     */
    protected void updateTitle() {
        String s = getController().getIndicator().getTitle();
        txtTitle.setText(s);
    }
}