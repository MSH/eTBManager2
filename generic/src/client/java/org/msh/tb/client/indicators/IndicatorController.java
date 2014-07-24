package org.msh.tb.client.indicators;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.shared.ReportServiceAsync;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CIndicatorRequest;
import org.msh.tb.client.shared.model.CIndicatorResponse;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.tableview.TableData;

import java.util.HashMap;

/**
 * Control the indicator data based on the report and indicator variables and filters
 * Created by ricardo on 10/07/14.
 */
public class IndicatorController {

    private CReport report;
    private CIndicator indicator;
    private CIndicatorResponse response;
    private TableData data;

    /**
     * Default constructor
     * @param report report of the indicator
     * @param indicator instance of the indicator that will be controlled
     * @param response indicator data, containing all values to render the indicator table
     */
    public IndicatorController(CReport report, CIndicator indicator, CIndicatorResponse response) {
        this.report = report;
        this.indicator = indicator;
        updateResponse(response);
    }

    /**
     * Update the indicator data
     * @param callback
     */
    public void update(final AsyncCallback<IndicatorController> callback) {
        ReportServiceAsync srv = AppResources.reportServices();
        final IndicatorController me = this;
        srv.executeIndicator(createRequest(), new AsyncCallback<CIndicatorResponse>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(CIndicatorResponse response) {
                me.updateResponse(response);
                callback.onSuccess(me);
            }
        });
    }

    /**
     * Clear the indicator data, forcing it to reload information from server next time
     */
    public void clearData() {
        response = null;
        data = null;
    }

    /**
     * Called when the response data is changed
     * @param resp
     */
    protected void updateResponse(CIndicatorResponse resp) {
        this.response = resp;

        if (resp == null) {
            data = null;
            return;
        }

        this.response = resp;
        data = new TableData();
        data.update(indicator, resp);
    }


    /**
     * Create a new request from the indicator data
     * @return
     */
    public CIndicatorRequest createRequest() {
        CIndicatorRequest req = new CIndicatorRequest();
        req.setColVariables(indicator.getColVariables());
        req.setRowVariables(indicator.getRowVariables());

        // create list of filters
        HashMap<String, String> filters = new HashMap<String, String>();
        // first -> report filters
        if (report.getFilters() != null) {
            filters.putAll(report.getFilters());
        }
        // second -> indicator filters
        if (indicator.getFilters() != null) {
            filters.putAll(indicator.getFilters());
        }
        req.setFilters(filters);

        return req;
    }


    public CIndicator getIndicator() {
        return indicator;
    }

    public void setIndicator(CIndicator indicator) {
        this.indicator = indicator;
    }

    public CReport getReport() {
        return report;
    }

    public void setReport(CReport report) {
        this.report = report;
    }

    public CIndicatorResponse getResponse() {
        return response;
    }

    public void setResponse(CIndicatorResponse response) {
        this.response = response;
    }

    public TableData getData() {
        return data;
    }

    public void setData(TableData data) {
        this.data = data;
    }
}
