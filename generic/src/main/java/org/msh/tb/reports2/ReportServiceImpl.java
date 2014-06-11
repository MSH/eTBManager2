package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.HashMap;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.annotations.security.Restrict;
import org.msh.reports.filters.Filter;
import org.msh.tb.client.shared.ReportService;
import org.msh.tb.client.shared.model.CInitializationData;
import org.msh.tb.client.shared.model.CItem;
import org.msh.tb.client.shared.model.CPatientList;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.shared.model.CReportRequest;
import org.msh.tb.client.shared.model.CReportResponse;


/**
 * Implementation of the {@link ReportService} interface, with a set of
 * services exposed to support report generation in GWT user interface
 * 
 * @author Ricardo Memoria
 *
 */
@Name("org.msh.tb.client.shared.ReportService")
@Restrict("#{s:hasRole('DATA_ANALYSIS')}")
public class ReportServiceImpl implements ReportService {

	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#initialize()
	 */
	@Override
	@WebRemote
	public CInitializationData initialize() {
		return ReportGenerator.createInitializationData();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#executeReport(org.msh.tb.client.shared.model.CReportData)
	 */
	@Override
	@WebRemote
	public CReportResponse executeReport(CReportRequest reportData) {
		return ReportGenerator.generateReport(reportData);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#getFilterOptions(java.lang.String, java.lang.String)
	 */
	@Override
	@WebRemote
	public ArrayList<CItem> getFilterOptions(String filterid, String param) {
		ReportResources res = ReportResources.instance();
		Filter filter = res.findFilterById(filterid);
		if (filter == null)
			return null;

		return ReportGenerator.generateFilterOptions(filter, param);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#getPatients(java.util.HashMap, java.util.HashMap, java.util.HashMap, int)
	 */
	@Override
	@WebRemote
	public CPatientList getPatients(HashMap<String, String> filters, int page) {
		return ReportGenerator.getPatients(filters,  page);
	}


	/** {@inheritDoc}
	 */
	@Override
	@WebRemote
	public CReport loadReport(Integer id) {
		return ReportGenerator.getReport(id);
	}


	/** {@inheritDoc}
	 */
	@Override
	@WebRemote
	public Integer saveReport(CReport report) {
		return ReportGenerator.saveReport(report);
	}


	/** {@inheritDoc}
	 */
	@Override
	@WebRemote
	public void deleteReport(Integer reportId) {
		ReportGenerator.deleteReport(reportId);
	}


	/** {@inheritDoc}
	 */
	@Override
	@WebRemote
	public ArrayList<CReport> getReportList() {
		return ReportGenerator.getReportList();
	}
}
