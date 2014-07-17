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
import org.msh.tb.client.chart.ChartType;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.ChartPopup;
import org.msh.tb.client.reports.variables.VariablesPanel;
import org.msh.tb.client.ui.LabelEditor;

/**
 * Display an editor panel to change indicator parameters (filters, variables, layout, chart, etc)
 * Created by ricardo on 10/07/14.
 */
public class IndicatorEditor extends Composite {
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
                    chartChangeListener((ChartType)data);
                }
            });
        }

        Widget source = (Widget)event.getSource();
        chartPopup.showRelativeTo(source);
    }

    /**
     * Notify when the chart is changed
     * @param type
     */
    protected void chartChangeListener(ChartType type) {
        controller.getIndicator().setChartType(type.ordinal());

        updateChartButton();

        updateChart();
    }


    /**
     * Update the image in the chart button according to the chart type in the indicator
     */
    protected void updateChartButton() {
        Element el = (Element)lnkChart.getElement();
        if (DOM.getChildCount(el) > 0) {
            el.removeChild(DOM.getFirstChild(el));
        }

        Integer type = controller.getIndicator().getChartType();
        if (type == null) {
            return;
        }

        ChartType ct = ChartType.values()[type];
        ImageResource res = ChartPopup.getChartImage(ct);
        Image img = new Image(res);
        DOM.insertChild(el, img.getElement(), 0);
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