package org.msh.tb.client.shared;


import org.msh.tb.client.shared.model.CReportData;
import org.msh.tb.client.shared.model.CReportUI;
import org.msh.tb.client.shared.model.CTable;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Report services exposed by the server side
 * 
 * @author Ricardo Memoria
 *
 */
@RemoteServiceRelativePath("seam/resource/gwt2")
public interface ReportService extends RemoteService {

	/**
	 * Initialize a report. If no report identifier is given,
	 * a new report data will be sent.
	 *  
	 * @param repid
	 * @return instance of {@link CReportData} containing data about the report
	 */
	CReportData initReport(Integer repid);
	
	/**
	 * Initialize the report UI, return data to be displayed in the selection boxes
	 * @return
	 */
	CReportUI initialize();

	/**
	 * Execute the report
	 * @param reportData
	 * @return
	 */
	CTable executeReport(CReportData reportData);
}
