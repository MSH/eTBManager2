package org.msh.tb.client.indicators;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;

/**
 * Controller to handle common report operations
 *
 * Created by Ricardo on 22/07/2014.
 */
public class ReportController {
    private CReport report;

    /**
     * Default constructor
     * @param report the report object under control
     */
    public ReportController(CReport report) {
        this.report = report;
    }

    /**
     * Update all indicators of the report and, as the indicator is updated,
     * the callback function is called
     * @param callback callback function to be notified when an indicator is updated
     */
    public void update(AsyncCallback<IndicatorController> callback) {
        if (report.getIndicators() == null) {
            return;
        }

        // update them recursively
        updateIndicator(0, callback);
    }

    /**
     * Update an indicator asynchronously and recursively
     * @param index the indicator index in the report
     * @param callback callback to be called when indicator is updated
     */
    private void updateIndicator(final int index, final AsyncCallback<IndicatorController> callback) {
        // if index is higher than the total number of indicators, finish it
        if (index >= report.getIndicators().size()) {
            callback.onSuccess(null);
            return;
        }

        CIndicator indicator = report.getIndicators().get(index);
        IndicatorController controller = new IndicatorController(report, indicator, null);

        controller.update(new StandardCallback<IndicatorController>() {
            @Override
            public void onSuccess(IndicatorController result) {
                callback.onSuccess(result);
                updateIndicator(index + 1, callback);
            }
        });
    }
}
