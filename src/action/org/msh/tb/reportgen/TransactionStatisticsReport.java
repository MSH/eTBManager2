package org.msh.tb.reportgen;

import java.util.Date;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Workspace;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;
import org.msh.utils.reportgen.layouts.ReportTableLayout;

@Name("reportTest")
public class TransactionStatisticsReport {

	private ReportQuery reportQuery;
	private Variable varUser = new UserLogVariable();
	private Variable varRole = new RoleVariable();
	private Variable varMonth = new MonthVariable();
	private Variable varAdminUnit = new AdminUnitVariable();
	private Variable varUnit = new UnitVariable();
	
	/**
	 * Initialize report
	 */
	public void initialize() {
		reportQuery = new ReportQuery("transactionlog");
		
		Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");
		reportQuery.addRestriction("workspacelog_id = :" + reportQuery.createParameter(workspace.getId()));
		
		reportQuery.addVariable(varAdminUnit);
		reportQuery.addVariable(varUnit);
		reportQuery.addVariable(varMonth);
//		reportQuery.addVariable(varRole);
	}
	
	
	public String getTableHtml() {
		Date dt = new Date();
		initialize();

		ReportTableLayout layout = new ReportTableLayout();
		layout.addQuery(reportQuery);
		layout.setStyleClass("table1");
		layout.setColumnVariable(varMonth);
		layout.setRowVariable(varUnit);
		layout.setRowGroupingVariable(varAdminUnit);

		String s = layout.generateHtml();
		System.out.println(dt);
		System.out.println(new Date());
		return s;
	}
}
