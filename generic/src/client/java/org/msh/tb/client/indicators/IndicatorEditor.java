package org.msh.tb.client.indicators;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.shared.model.CChartType;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.ChartPopup;
import org.msh.tb.client.reports.variables.VariablesPanel;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.ui.LabelEditor;

/**
 * Display an editor panel to change indicator parameters (filters, variables, layout, chart, etc)
 * Created by ricardo on 10/07/14.
 */
public class IndicatorEditor extends Composite implements StandardEventHandler {
    interface IndicatorEditorUiBinder extends UiBinder<HTMLPanel, IndicatorEditor> {
    }

    private static IndicatorEditorUiBinder ourUiBinder = GWT.create(IndicatorEditorUiBinder.class);

    private IndicatorController controller;

    @UiField LabelEditor txtTitle;
    @UiField VariablesPanel pnlVariables;
    @UiField ListBox lbSize;
    @UiField Anchor lnkChart;
    @UiField Anchor lnkUpdate;
    @UiField ResultView resIndicator;

    private ChartPopup chartPopup;

    /**
     * Default constructor
     */
    public IndicatorEditor() {
        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);

        txtTitle.setEventHandler(new StandardEventHandler() {
            @Override
            public void handleEvent(Object eventType, Object data) {
                controller.getIndicator().setTitle(txtTitle.getText());
            }
        });

        lbSize.addItem("100%");
        lbSize.addItem("50%");
        lbSize.addItem("25%");
        lbSize.setSelectedIndex(0);

        pnlVariables.setEventHandler(this);
        txtTitle.setEventHandler(this);
    }


    /**
     * Set the indicator to be displayed
     * @param controller
     */
    public void update(IndicatorController controller) {
        this.controller = controller;
        updateTitle();
        txtTitle.setText(controller.getIndicator().getTitle());

        pnlVariables.setColumnVariables(controller.getIndicator().getColVariables());
        pnlVariables.setRowVariables(controller.getIndicator().getRowVariables());

        updateChartButton();

        resIndicator.update(controller);
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
     * @param event
     */
    @UiHandler("lnkUpdate")
    public void lnkUpdateClick(ClickEvent event) {
        controller.setData(null);
        resIndicator.update(controller);
    }

    /**
     * Update the indicator with the values selected in the user interface
     */
    protected void uiToIndicator() {
        CIndicator ind = controller.getIndicator();
        ind.setColVariables( pnlVariables.getColumnVariables() );
        ind.setRowVariables(pnlVariables.getRowVariables());
    }

    /**
     * Notify when the chart is changed
     * @param type
     */
    protected void chartChangeListener(CChartType type) {
        controller.getIndicator().setChartType(type);
        updateChartButton();
        resIndicator.notifyChartChange();
    }


    /**
     * Update the image in the chart button according to the chart type in the indicator
     */
    protected void updateChartButton() {
        Element el = (Element)lnkChart.getElement();
        if (DOM.getChildCount(el) > 0) {
            el.removeChild(DOM.getFirstChild(el));
        }

        CChartType type = controller.getIndicator().getChartType();
        if (type == null) {
            return;
        }

        ImageResource res = ChartPopup.getChartImage(type);
        Image img = new Image(res);
        DOM.insertChild(el, img.getElement(), 0);
    }


    /**
     * Called when an event happens
     * @param eventType
     * @param data
     */
    @Override
    public void handleEvent(Object eventType, Object data) {
        if (eventType == VariablesPanel.class) {
            uiToIndicator();
            return;
        }

        if (eventType == txtTitle) {
            controller.getIndicator().setTitle((String)data);
            resIndicator.notifyTitleChange();
        }
    }


    public void updateChart() {

    }

    public void updateTable() {

    }

    public void updateIndicator() {

    }

    /**
     * Update the title's content with the title in the indicator object
     */
    protected void updateTitle() {
        String s = controller.getIndicator().getTitle();
        txtTitle.setText(s);
    }

}