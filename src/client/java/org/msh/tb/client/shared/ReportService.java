package org.msh.tb.client.shared;


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.msh.tb.client.shared.model.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Report services exposed by the server side
 * 
 * @author Ricardo Memoria
 *
 */
@RemoteServiceRelativePath("../resources/gwtRpc")
public interface ReportService extends RemoteService {
	
	/**
	 * Initialize the report UI, return data to be displayed in the selection boxes
	 * @return
	 */
	CReportUIData initialize();

	/**
	 * Load report detailed data, like filters and variables in use
	 * @param id is the ID of the report
	 * @return instance of {@link CReport} class containing all data
	 */
	CReport loadReport(Integer id);
	
	/**
	 * Save a report. If the report ID is not specified, so a new report
	 * is saved
	 * @param report
	 * @return the report ID
	 */
	Integer saveReport(CReport report);
	
	/**
	 * Delete a report. Just the owner of a report can delete it
	 * @param reportId is the report ID
	 */
	void deleteReport(Integer reportId);

	
	/**
	 * Return the list of available org.msh.reports for the user
	 * @return list of {@link CReport} objects
	 */
	ArrayList<CReport> getReportList();
	
	/**
	 * Execute the report
	 * @param reportData
	 * @return
	 */
	CIndicatorResponse executeIndicator(CIndicatorRequest reportData);


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
