package org.msh.tb.client.dashboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.indicators.IndicatorController;
import org.msh.tb.client.indicators.IndicatorWrapperPanel;
import org.msh.tb.client.indicators.ResultView;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;

/**
 * Created by Ricardo on 23/07/2014.
 */
public class ReportPanel extends Composite{
    interface ReportPanelUiBinder extends UiBinder<HTMLPanel, ReportPanel> {
    }

    private static ReportPanelUiBinder ourUiBinder = GWT.create(ReportPanelUiBinder.class);

    @UiField Label txtTitle;
    @UiField FlowPanel pnlIndicators;

    private CReport report;

    /**
     * Default constructors
     */
    public ReportPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }


    /**
     * Update a report and send its data to
     * @param report the report to be updated
     * @param callback callback function called when the report is finished
     */
    public void update(CReport report, StandardCallback<Void> callback) {
        this.report = report;
        txtTitle.setText(report.getTitle());
        pnlIndicators.clear();
        updateIndicator(0, callback);
    }


    /**
     * Update an indicator of the report recursively and asynchronously
     * @param index the indicator index, inside the report (must be 0, indicating the first indicator)
     * @param callback callback function that will be called when all indicators are rendered
     */
    protected void updateIndicator(final int index, final AsyncCallback<Void> callback) {
        if ((report.getIndicators() == null) || (index >= report.getIndicators().size())) {
            callback.onSuccess(null);
            return;
        }

        IndicatorPanel view = new IndicatorPanel();
        pnlIndicators.add(view);

        if (index > 0) {
            CIndicator prevInd = report.getIndicators().get(index);
            if (prevInd.isSingleValue()) {
                view.addStyleName("clear-left");
            }
        }

        CIndicator ind = report.getIndicators().get(index);
        if ((ind.getSize() != null) && (ind.getSize() == 50)) {
            view.addStyleName("ind50");
        }
        IndicatorController controller = new IndicatorController(report, ind, null);

        view.update(controller, new StandardCallback<ResultView>() {
            @Override
            public void onSuccess(ResultView result) {
                updateIndicator(index + 1, callback);
            }
        });
    }

}