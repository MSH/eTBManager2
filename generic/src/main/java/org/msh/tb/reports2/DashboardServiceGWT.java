/**
 * 
 */
package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.msh.reports.variables.Variable;
import org.msh.tb.client.shared.DashboardService;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.shared.model.CReportRequest;
import org.msh.tb.client.shared.model.CReportResponse;
import org.msh.tb.client.shared.model.CVariable;
import org.msh.tb.entities.Report;

/**
 * Implement the services exposed to the GWT client application
 * @author Ricardo Memoria
 *
 */
@Name("org.msh.tb.client.shared.DashboardService")
public class DashboardServiceGWT implements DashboardService {

	/** {@inheritDoc}
	 */
	@Override
	@WebRemote
	public ArrayList<Integer> initialize() {
		// get list of indicators
		ReportDAO dao = (ReportDAO)Component.getInstance("reportDAO");
		List<Report> reps = dao.getDashboardIndicators();
		
		ArrayList<Integer> lst = new ArrayList<Integer>();
		for (Report rep: reps) {
			lst.add(rep.getId());
		}
		return lst;
	}

	/** {@inheritDoc}
	 */
	@Override
	@WebRemote
	public CIndicator generateIndicator(Integer id) {
		// get information about the report in order to generate request 
		CReport rep = ReportGenerator.getReport(id);

		CReportRequest req = new CReportRequest();
		req.setColVariables(rep.getColumnVariables());
		req.setRowVariables(rep.getRowVariables());
		req.setFilters(rep.getFilters());

		// generate report response
		CReportResponse res = ReportGenerator.generateReport(req);

		// mount response to the client
		CIndicator indicator = new CIndicator();
		indicator.setTitle(rep.getTitle());
		indicator.setChartType(rep.getChartType());
		indicator.setReportResponse(res);
		
		// mount variable list
		ReportResources resources = ReportResources.instance();
		ArrayList<CVariable> colvars = new ArrayList<CVariable>();
		for (String colvar: rep.getColumnVariables()) {
			Variable var = resources.findVariableById(colvar);
			colvars.add(new CVariable(var.getId(), var.getLabel()));
		}
		indicator.setColVariables(colvars);
		
		ArrayList<CVariable> rowvars = new ArrayList<CVariable>();
		for (String rowvar: rep.getRowVariables()) {
			Variable var = resources.findVariableById(rowvar);
			rowvars.add(new CVariable(var.getId(), var.getLabel()));
		}
		indicator.setRowVariables(rowvars);
		
		return indicator;
	}

}
