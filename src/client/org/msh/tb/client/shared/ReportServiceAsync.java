package org.msh.tb.client.shared;

import java.util.ArrayList;
import java.util.HashMap;

import org.msh.tb.client.shared.model.CItem;
import org.msh.tb.client.shared.model.CPatient;
import org.msh.tb.client.shared.model.CPatientList;
import org.msh.tb.client.shared.model.CReportData;
import org.msh.tb.client.shared.model.CReportUI;
import org.msh.tb.client.shared.model.CTable;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asyncronous service used by the client side wrapping the {@link ReportService} interface
 *  
 * @author Ricardo Memoria
 *
 */
public interface ReportServiceAsync {

	void initReport(Integer repid, AsyncCallback<CReportData> callback);

	void initialize(AsyncCallback<CReportUI> callback);

	void executeReport(CReportData reportData, AsyncCallback<CTable> callback);

	void getFilterOptions(String filterid, String param,
			AsyncCallback<ArrayList<CItem>> callback);

	void getPatients(HashMap<String, String> filters, int page,
			AsyncCallback<CPatientList> callback);

}
