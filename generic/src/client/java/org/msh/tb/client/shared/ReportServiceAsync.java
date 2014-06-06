package org.msh.tb.client.shared;

import java.util.ArrayList;
import java.util.HashMap;

import org.msh.tb.client.shared.model.CItem;
import org.msh.tb.client.shared.model.CPatientList;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.shared.model.CReportRequest;
import org.msh.tb.client.shared.model.CInitializationData;
import org.msh.tb.client.shared.model.CReportResponse;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asyncronous service used by the client side wrapping the {@link ReportService} interface
 *  
 * @author Ricardo Memoria
 *
 */
public interface ReportServiceAsync {

	void initialize(AsyncCallback<CInitializationData> callback);

	void executeReport(CReportRequest reportData, AsyncCallback<CReportResponse> callback);

	void getFilterOptions(String filterid, String param,
			AsyncCallback<ArrayList<CItem>> callback);

	void getPatients(HashMap<String, String> filters, int page,
			AsyncCallback<CPatientList> callback);

	void loadReport(Integer id, AsyncCallback<CReport> callback);

	void deleteReport(Integer reportId, AsyncCallback<Void> callback);

	void saveReport(CReport report, AsyncCallback<Integer> callback);

}
