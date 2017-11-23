package org.msh.tb.client.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.msh.tb.client.shared.model.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Asyncronous service used by the client side wrapping the {@link ReportService} interface
 *  
 * @author Ricardo Memoria
 *
 */
public interface ReportServiceAsync {

	void initialize(AsyncCallback<CReportUIData> callback);

	void executeIndicator(CIndicatorRequest reportData, AsyncCallback<CIndicatorResponse> callback);

	void getFilterOptions(String filterid, String param,
			AsyncCallback<ArrayList<CItem>> callback);

	void getPatients(HashMap<String, String> filters, int page,
			AsyncCallback<CPatientList> callback);

	void loadReport(Integer id, AsyncCallback<CReport> callback);

	void deleteReport(Integer reportId, AsyncCallback<Void> callback);

	void saveReport(CReport report, AsyncCallback<Integer> callback);

	void getReportList(AsyncCallback<ArrayList<CReport>> callback);

}
