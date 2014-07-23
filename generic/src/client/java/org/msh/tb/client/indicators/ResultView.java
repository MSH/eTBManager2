package org.msh.tb.client.indicators;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.msh.tb.client.chart.ChartReport;
import org.msh.tb.client.chart.ChartView;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CChartType;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.tableview.TableView;
import org.msh.tb.client.ui.DivPanel;


/**
 * Panel that display a given indicator
 * Created by ricardo on 10/07/14.
 */
public class ResultView extends Composite {

    public enum Layout {  VERTICAL, HORIZONTAL  }

    private IndicatorController controller;
    private DivPanel pnlLayout;
    private ChartView chart;
    private TableView table;
    private Label txtTitle;

    private boolean singleValue;

    /**
     * Default constructor
     */
    public ResultView() {
        pnlLayout = new DivPanel();
        initWidget(pnlLayout);
    }


    /**
     * Update the content of the view
     */
    public void update(IndicatorController controller, final AsyncCallback<ResultView> callback) {
        this.controller = controller;

        // check how to update it
        CIndicator indicator = controller.getIndicator();
        singleValue = (indicator.getColVariablesCount() == 0) && (indicator.getRowVariablesCount() == 0);
        if (singleValue) {
            updateSingleValue();
        }
        else {
            updateCompositeValue();
        }

        // if data is not updated, then call it recursively
        if (controller.getData() == null) {
            // check if data is updated, otherwise, update the indicator data
            controller.update(new StandardCallback<IndicatorController>() {
                @Override
                public void onSuccess(IndicatorController controller) {
                    update(controller, callback);
                }
            });
        }
        else {
            // if indicator is properly displayed, then finish
            if (callback != null) {
                callback.onSuccess(this);
            }
        }
    }

    /**
     * Called to notify the view that the title has changed
     */
    public void notifyTitleChange() {
        txtTitle.setText(controller.getIndicator().getTitle());
    }

    /**
     * Notify the view that the size was changed
     */
    public void notifySizeChange() {
        updateSize();
        update(controller, null);
    }

    /**
     * Update the size of the view according to the value set in the indicator
     */
    private void updateSize() {
        if (singleValue) {
            return;
        }

        String s = "ind-res";
        Integer size = controller.getIndicator().getSize();
        if ((size != null) && (size < 100)) {
            s += " tbl" + Integer.toString(size);
        }
        pnlLayout.setStyleName(s);
    }


    /**
     * Notify about chart type change
     */
    public void notifyChartChange() {
        if (chart == null) {
            return;
        }
        CChartType type = controller.getIndicator().getChartType();
        chart.setSelectedChart(type);
        ChartReport.update(chart, controller.getData());
    }

    /**
     * Update the indicator when the value to update is a single value
     */
    protected void updateSingleValue() {
        pnlLayout.setStyleName("ind-res-single");
        pnlLayout.clear();

        // add the value
        if (controller.getResponse() == null) {
            addWaitIndicator();
        }
        else {
            Double val = controller.getResponse().getRows().get(0).getValues()[0];
            String sval = (val == null || val == 0)? "-": NumberFormat.getFormat("#,###,###").format(val);
            pnlLayout.addText(sval, "ind-value");
        }

        // add the title
        String title = controller.getIndicator().getTitle();
        txtTitle = pnlLayout.addText(title, "ind-title");
    }


    /**
     * Update the indicator when the value to update is a table with chart
     */
    protected void updateCompositeValue() {
        updateSize();
        pnlLayout.clear();

        txtTitle = pnlLayout.addText(controller.getIndicator().getTitle(), "title");

        if (controller.getResponse() == null) {
            addWaitIndicator();
            return;
        }

        addChart();

        addTable();
    }


    protected void addWaitIndicator() {
        pnlLayout.addText("", "wait-icon2");
    }

    /**
     * Insert a table in the layout table at the given column and row
     */
    protected void addTable() {
        table = new TableView();
        table.update(controller.getData());
        pnlLayout.add(table, "ind-table");
    }

    /**
     * Insert a chart to the table in the given position
     */
    protected void addChart() {
        chart = new ChartView();
        chart.setSelectedChart(controller.getIndicator().getChartType());
        pnlLayout.add(chart, "ind-chart");
        ChartReport.update(chart, controller.getData());
    }


}