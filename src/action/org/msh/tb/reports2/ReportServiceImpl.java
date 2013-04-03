package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.international.Messages;
import org.msh.reports.IndicatorReport;
import org.msh.reports.datatable.Cell;
import org.msh.reports.datatable.DataTable;
import org.msh.reports.datatable.Row;
import org.msh.tb.client.shared.ReportService;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CFilterType;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CReportData;
import org.msh.tb.client.shared.model.CReportUI;
import org.msh.tb.client.shared.model.CTable;
import org.msh.tb.client.shared.model.CVariable;
import org.msh.tb.reports2.variables.DateFieldVariable;

/**
 * Implementation of the {@link ReportService} interface, with a set of
 * services exposed to support report generation in GWT user interface
 * 
 * @author Ricardo Memoria
 *
 */
@Name("org.msh.tb.client.shared.ReportService")
public class ReportServiceImpl implements ReportService {

	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#initReport(java.lang.Integer)
	 */
	@Override
	@WebRemote
	public CReportData initReport(Integer repid) {
		CReportData rep = new CReportData();

		return rep;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#initialize()
	 */
	@Override
	@WebRemote
	public CReportUI initialize() {
		// get resources containing groups, filters and variables
		ReportResources res = getReportResources();

		ArrayList<CGroup> lst = new ArrayList<CGroup>();

		// create list of groups
		for (ReportGroup repGroup: res.getGroups()) {
			CGroup grp = new CGroup();
			grp.setName(repGroup.getDisplayText());

			// create list of filters
			List<VariableImpl> repFilters = repGroup.getFilters();
			if (repFilters.size() > 0) {
				CFilter[] filters = new CFilter[repFilters.size()];
				int index = 0;
				for (VariableImpl filter: repFilters) {
					CFilter f = new CFilter();
					f.setId(filter.getId());
					f.setName(filter.getLabel());
					f.setType(CFilterType.OPTIONS);
					filters[index++] = f;
				}
				grp.setFilters(filters);
			}

			// create list of variables
			List<VariableImpl> repVars = repGroup.getVariables();
			if (repVars.size() > 0) {
				CVariable[] vars = new CVariable[repVars.size()];
				int index = 0;
				for (VariableImpl var: repVars) {
					CVariable v = new CVariable();
					v.setId(var.getId());
					v.setName(var.getLabel());
					vars[index++] = v;
				}
				grp.setVariables(vars);
			}
			
			lst.add(grp);
		}

		CReportUI rep = new CReportUI();
		rep.setGroups( lst );

		return rep;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#executeReport(org.msh.tb.client.shared.model.CReportData)
	 */
	@Override
	@WebRemote
	public CTable executeReport(CReportData reportData) {
		// check validation rules
		if ((reportData == null) || (reportData.getColVariables() == null) || (reportData.getRowVariables() == null) ||
			(reportData.getColVariables().size() == 0) || (reportData.getRowVariables().size() == 0))
			return null;

//		int numFixedRows = reportData.getColVariables().size();

		// create indicator report
		ReportResources res = getReportResources();
		IndicatorReport rep = IndicatorReportFactory.instance().createCaseIndicator();

		List<VariableImpl> variables = new ArrayList<VariableImpl>();
		
		boolean bDuplicated = false;
		int dateVars = 0;
		// add variables to the columns of the report
		for (String varid: reportData.getColVariables()) {
			VariableImpl var = res.findVariableById(varid);
			
			if (var != null) {
				if (var instanceof DateFieldVariable)
					dateVars++;
				
				if (variables.contains(var))
					bDuplicated = true;

				rep.addColumnVariable(var);
				variables.add(var);
			}
		}

		// add variables to the rows of the report
		for (String varid: reportData.getRowVariables()) {
			VariableImpl var = res.findVariableById(varid);

			if (var != null) {
				if (var instanceof DateFieldVariable)
					dateVars++;
				
				if (variables.contains(var))
					bDuplicated = true;

				rep.addRowVariable(var);
				variables.add(var);
			}
		}
		
		// validate variables
		if (bDuplicated)
			return returnError("manag.reportgen.error1");
		
		if (dateVars > 1)
			return returnError("manag.reportgen.error2");
		
		// execute the report
		DataTable tbl = rep.getResult();

//		displayTable(tbl);
		
		// there is any data returned
		if (tbl.getRowCount() == 0)
			return null;

		ClientTableGenerator gen = new ClientTableGenerator();
		CTable ctable = gen.execute(rep);
		
		return ctable;
	}


	/**
	 * Return the instance of the {@link ReportResources} class
	 * @return
	 */
	protected ReportResources getReportResources() {
		return (ReportResources)Component.getInstance("reportResources");
	}

	
	/**
	 * Return an instance of the {@link CTable} containing an error message
	 * @param errorKey
	 * @return
	 */
	protected CTable returnError(String errorKey) {
		CTable tbl = new CTable();
		tbl.setErrorMessage(Messages.instance().get(errorKey));
		return tbl;
	}

	protected String pad(Object val, int len) {
		String s = (val != null? val.toString() : null);
		if ((s != null) && (s.length() > len))
			s = s.substring(0, len);
		return String.format("%1$-" + len + "s", s);
	}


	/**
	 * Show table data
	 */
	protected void displayTable(DataTable table) {
		for (Row row: table.getRows()) {
			String s = "|";
			for (Cell cell: row.getCellsToRender()) {
				Object value = cell.getValue();
				String txt = value != null? value.toString() : "";
				int dx = 16 * cell.getStyle().getColSpan() + (3 * (cell.getStyle().getColSpan() - 1));
				s += pad(txt, dx) + " | ";
			}
			System.out.println(s);
		}
		System.out.println("\n");
	}

}
