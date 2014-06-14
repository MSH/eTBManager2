/**
 * 
 */
package org.msh.tb.client.reports;

import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.reports.chart.ChartType;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.shared.model.CReportRequest;

/**
 * @author Ricardo Memoria
 *
 */
public class ReportCRUDServices {

	/**
	 * Save a new report
	 * @param title is the title of the report
	 */
	public static void saveNewReport(String title, StandardCallback<CReport> callback) {
		CReport report = updateReportData();
		report.setTitle(title);
		saveReport(callback);
	}
	
	/**
	 * Save the current report
	 * @param callback is the callback function that will be called back when report is saved
	 */
	public static void saveReport(StandardCallback<CReport> callback) {
		CReport report = updateReportData();
		saveReportData(report, callback);
	}

	
	/**
	 * Update variables and filters used in the current report being displayed
	 */
	protected static CReport updateReportData() {
		CReport report = ReportMain.instance().getReport();
		CReportRequest req = ReportMain.instance().prepareReportRequest();
		report.setColumnVariables(req.getColVariables());
		report.setRowVariables(req.getRowVariables());
		report.setFilters(req.getFilters());
		ChartType chartType = ReportMain.instance().getChartType();
		report.setChartType(chartType != null? chartType.ordinal(): null);
		return report;
	}
	
	/**
	 * Clone the current report with a different name
	 * @param title
	 * @param callback
	 */
	public static void saveReportAs(String title, final StandardCallback<CReport> callback) {
		CReport report = updateReportData();
		report.setTitle(title);
		// force to save a new report
		report.setId(null);
		saveReportData(report, callback);
	}
	
	
	/**
	 * @param report
	 * @param callback
	 */
	protected static void saveReportData(final CReport report, final StandardCallback<CReport> callback) {
		ReportMain.instance().getService().saveReport(report, new StandardCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				ReportMain.instance().getReport().setId(result);
				ReportMain.instance().updateReport();
				if (callback != null) {
					callback.onSuccess(report);
				}
			}
		});
	}
}
