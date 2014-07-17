package org.msh.tb.client.indicators;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import org.msh.tb.client.chart.ChartReport;
import org.msh.tb.client.chart.ChartView;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.tableview.TableView;


/**
 * Created by ricardo on 10/07/14.
 */
public class ResultView extends Composite {

    public enum Layout {  VERTICAL, HORIZONTAL;  }

    private IndicatorController controller;
    private FlexTable tblLayout;
    private ChartView chart;
    private TableView table;
    private Layout layout = Layout.VERTICAL;

    private boolean showChart = true;
    private boolean showTable = true;
    private boolean showTitle = true;

    /**
     * Default constructor
     */
    public ResultView() {
        tblLayout = new FlexTable();
        initWidget(tblLayout);
    }


    /**
     * Update the content of the view
     */
    public void update(IndicatorController controller) {
        this.controller = controller;
        if (controller.getData() == null) {
            // check if data is updated, otherwise, update the indicator data
            controller.update(new StandardCallback<IndicatorController>() {
                @Override
                public void onSuccess(IndicatorController controller) {
                    update(controller);
                }
            });
            return;
        }

        // check how to update it
        CIndicator indicator = controller.getIndicator();
        if ((indicator.getColVariables().size() == 0) || (indicator.getRowVariables().size() == 0)) {
            updateSingleValue();
        }
        else {
            updateCompositeValue();
        }
    }


    /**
     * Update the indicator when the value to update is a single value
     */
    protected void updateSingleValue() {
        tblLayout.setStyleName("ind-res-single");
        tblLayout.removeAllRows();
        Double val = controller.getResponse().getRows().get(0).getValues()[0];
        String sval = (val == null || val == 0)? "-": NumberFormat.getFormat("#,###,###").format(val);
        String title = controller.getIndicator().getTitle();

        tblLayout.getCellFormatter().setStyleName(0,0, "value");
        tblLayout.setText(0, 0, sval);

        tblLayout.getCellFormatter().setStyleName(0, 1, "title");
        tblLayout.setText(0, 1, title);
    }


    /**
     * Update the indicator when the value to update is a table with chart
     */
    protected void updateCompositeValue() {
        tblLayout.setStyleName("ind-res");
        tblLayout.removeAllRows();

        int row = 0;
        if (showTitle) {
            tblLayout.getCellFormatter().setStyleName(0, 0, "title");
            tblLayout.setText(0, 0, controller.getIndicator().getTitle());
            row++;
        }

        if (showChart && showTable) {
            addChart(0, row);
            if (layout == Layout.VERTICAL) {
                addTable(0, row);
            }
            else {
                addTable(1, row);
            }
        }
        else {
            if (showChart) {
                addChart(0, row);
            }
            else {
                if (showTable) {
                    addTable(0, row + 1);
                }
            }
        }
    }


    /**
     * Insert a table in the layout table at the given column and row
     * @param col
     * @param row
     */
    protected void addTable(int col, int row) {
        TableView tbl = new TableView();
        tbl.update(controller.getData());
        tblLayout.setWidget(row, col, tbl);
    }

    /**
     * Insert a chart to the table in the given position
     * @param col column of the layout table
     * @param row row of the layout table
     */
    protected void addChart(int col, int row) {
        ChartView chart = new ChartView();
        tblLayout.setWidget(row, col, chart);
        ChartReport.update(chart, controller.getData());
    }


    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public boolean isShowChart() {
        return showChart;
    }

    public void setShowChart(boolean showChart) {
        this.showChart = showChart;
    }

    public boolean isShowTable() {
        return showTable;
    }

    public void setShowTable(boolean showTable) {
        this.showTable = showTable;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }
}