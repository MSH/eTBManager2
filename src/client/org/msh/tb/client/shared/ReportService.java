package org.msh.tb.client.shared;


import java.util.ArrayList;
import java.util.HashMap;

import org.msh.tb.client.shared.model.CItem;
import org.msh.tb.client.shared.model.CPatientList;
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


	/**
	 * Return the options of a given filter by its id and an optional parameter
	 * @param filterid the id of the filter
	 * @param param is an optional parameter specified by the filter
	 * @return list of options of the {@link CItem} class
	 */
	ArrayList<CItem> getFilterOptions(String filterid, String param);


	/**
	 * Return the list of patients based on the values of variables and filters
	 * @param filters include all filters that narrows the result, including variables key values
	 * @param page the initial page of patient list, starting in 0
	 * @return
	 */
	CPatientList getPatients(HashMap<String, String> filters, int page);
}
