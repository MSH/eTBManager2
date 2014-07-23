/**
 * 
 */
package org.msh.tb.client.dashboard;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.msh.tb.client.AppModule;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.indicators.IndicatorController;
import org.msh.tb.client.shared.DashboardService;
import org.msh.tb.client.shared.DashboardServiceAsync;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;

import java.util.ArrayList;

/**
 * Main page that display the dashboard
 * 
 * @author Ricardo Memoria
 *
 */
public class DashboardMain extends Composite implements AppModule {

	interface Binder extends UiBinder<Widget, DashboardMain> { }
	private static final Binder binder = GWT.create(Binder.class);

	// service to display the dashboard
	public static final DashboardServiceAsync service = GWT.create(DashboardService.class);

    private int colindex;
    private ArrayList<CReport> reports;
	
	@UiField FlowPanel pnlContent;
	
	/**
	 * Default constructor
	 */
	public DashboardMain() {
		initWidget(binder.createAndBindUi(this));
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public void run() {
		service.initialize(new StandardCallback<ArrayList<CReport>>() {
			@Override
			public void onSuccess(ArrayList<CReport> result) {
				updateReports(result);
			}
		});
	}

	/**
	 * Update the list of reports that must be displayed in the dashboard
	 * @param reports list of reports to be rendered
	 */
	protected void updateReports(ArrayList<CReport> reports) {
        this.reports = reports;
		pnlContent.clear();
		updateReport(0);
	}

    /**
     * Callback function called when the report of given index is updated
     * @param index index of the report in the list of reports
     */
    protected void updateReport(final int index) {
        if (index >= reports.size()) {
            return;
        }

        ReportPanel pnl = new ReportPanel();
        pnlContent.add(pnl);

        CReport rep = reports.get(index);
        // update the report
        pnl.update(rep, new StandardCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateReport(index + 1);
            }
        });
    }

}
