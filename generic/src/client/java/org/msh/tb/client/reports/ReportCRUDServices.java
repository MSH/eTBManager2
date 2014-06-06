/**
 * 
 */
package org.msh.tb.client.reports;

import org.msh.tb.client.commons.StandardCallback;
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
		CReport report = MainPage.instance().getReport();
		CReportRequest req = MainPage.instance().prepareReportData();
		report.setColumnVariables(req.getColVariables());
		report.setRowVariables(req.getColVariables());
		report.setFilters(req.getFilters());
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
		MainPage.instance().getService().saveReport(report, new StandardCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				MainPage.instance().getReport().setId(result);
				MainPage.instance().updateReport();
				if (callback != null) {
					callback.onSuccess(report);
				}
			}
		});
	}
}
