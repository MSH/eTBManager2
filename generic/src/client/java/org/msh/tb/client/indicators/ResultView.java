package org.msh.tb.client.indicators;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import org.msh.tb.client.chart.ChartReport;
import org.msh.tb.client.chart.ChartView;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CChartType;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.tableview.TableView;
import org.msh.utils.reportgen.highchart.ChartType;


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
    private boolean singleValue;

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
        singleValue = (indicator.getColVariablesCount() == 0) && (indicator.getRowVariablesCount() == 0);
        if (singleValue) {
            updateSingleValue();
        }
        else {
            updateCompositeValue();
        }
    }


    public void notifyTitleChange() {
        updateSingleValue();
    }

    /**
     * Notify the view that the size was changed
     */
    public void notifySizeChange() {
        updateSize();
        update(controller);
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
        tblLayout.setStyleName(s);
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
        tblLayout.setStyleName("ind-res-single");
        tblLayout.removeAllRows();
        Double val = controller.getResponse().getRows().get(0).getValues()[0];
        String sval = (val == null || val == 0)? "-": NumberFormat.getFormat("#,###,###").format(val);
        String title = controller.getIndicator().getTitle();

        tblLayout.getCellFormatter().setStyleName(0, 0, "ind-value");
        tblLayout.setText(0, 0, sval);

        tblLayout.getCellFormatter().setStyleName(1, 0, "ind-title");
        tblLayout.setText(1, 0, title);
    }


    /**
     * Update the indicator when the value to update is a table with chart
     */
    protected void updateCompositeValue() {
        updateSize();
//        tblLayout.setStyleName("ind-res");
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
                addTable(0, row + 1);
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
        chart = new ChartView();
        chart.setSelectedChart(controller.getIndicator().getChartType());
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