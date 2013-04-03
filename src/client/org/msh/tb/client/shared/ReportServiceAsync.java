package org.msh.tb.client.shared;

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

}
