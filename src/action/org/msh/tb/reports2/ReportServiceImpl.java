package org.msh.tb.reports2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.Messages;
import org.msh.reports.IndicatorReport;
import org.msh.reports.datatable.Row;
import org.msh.reports.datatable.impl.DataTableImpl;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.indicator.DataTableIndicator;
import org.msh.reports.query.DataTableQuery;
import org.msh.reports.variables.Variable;
import org.msh.tb.client.shared.ReportService;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CItem;
import org.msh.tb.client.shared.model.CPatient;
import org.msh.tb.client.shared.model.CPatientList;
import org.msh.tb.client.shared.model.CReportData;
import org.msh.tb.client.shared.model.CReportUI;
import org.msh.tb.client.shared.model.CTable;
import org.msh.tb.client.shared.model.CVariable;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;
import org.msh.tb.reports2.variables.DateFieldVariable;
import org.msh.utils.date.DateUtils;


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
			List<Filter> repFilters = repGroup.getFilters();
			if (repFilters.size() > 0) {
				CFilter[] filters = new CFilter[repFilters.size()];
				int index = 0;
				for (Filter filter: repFilters) {
					CFilter f = new CFilter();
					f.setId(filter.getId());
					f.setName(filter.getLabel());
					f.setType(filter.getFilterType());
					// send options if they are not sent remotely
					if (!filter.isFilterLazyInitialized())
						f.setOptions(generateFilterOptions(filter, null));
					filters[index++] = f;
				}
				grp.setFilters(filters);
			}

			// create list of variables
			List<Variable> repVars = repGroup.getVariables();
			if (repVars.size() > 0) {
				CVariable[] vars = new CVariable[repVars.size()];
				int index = 0;
				for (Variable var: repVars) {
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
		// send the current date in the server to the client
		rep.setCurrentDate(DateUtils.getDate());

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

		// create indicator report
		ReportResources res = getReportResources();
		IndicatorReport rep = IndicatorReportFactory.instance().createCaseIndicator();

		List<Variable> variables = new ArrayList<Variable>();
		
		boolean bDuplicated = false;
		Variable duplvar = null;
		int dateVars = 0;

		// add variables to the columns of the report
		for (String varid: reportData.getColVariables()) {
			Variable var = res.findVariableById(varid);
			
			if (var != null) {
				if (var instanceof DateFieldVariable)
					dateVars++;
				
				if (variables.contains(var)) {
					bDuplicated = true;
					duplvar = var;
				}

				rep.addColumnVariable(var);
				variables.add(var);
			}
		}

		// add variables to the rows of the report
		for (String varid: reportData.getRowVariables()) {
			Variable var = res.findVariableById(varid);

			if (var != null) {
				if (var instanceof DateFieldVariable)
					dateVars++;
				
				if (variables.contains(var)) {
					bDuplicated = true;
					duplvar = var;
				}

				rep.addRowVariable(var);
				variables.add(var);
			}
		}

		// check unit used in the report
		Variable varUnitRef = null;
		for (Variable var: variables) {
			Object unitType = var.getUnitType();
			if (unitType != null) {
				if ((varUnitRef != null) && (!varUnitRef.getUnitType().equals(unitType))) {
					return returnError("manag.reportgen.error3", varUnitRef.getLabel(), var.getLabel());
				}
				
				if (varUnitRef == null)
					varUnitRef = var;
			}
		}
		
		// validate variables
		if (bDuplicated)
			return returnError("manag.reportgen.error1", duplvar.getLabel());
		
		if (dateVars > 1)
			return returnError("manag.reportgen.error2");

		// set filter values
		if (reportData.getFilters() != null) {
			for (String id: reportData.getFilters().keySet()) {
				Filter filter = res.findFilterById(id);
				if (filter != null) {
					String value = reportData.getFilters().get(id);
					Object filterValue = filter.filterValueFromString(value);
					rep.addFilter(filter, filterValue);
				}
			}
		}
		
		// execute the report
		rep.execute();
		DataTableIndicator tbl = rep.getResult();

//		displayTable(tbl);
		
		// there is any data returned
		if (tbl.getRowCount() == 0)
			return null;

		ClientTableGenerator gen = new ClientTableGenerator();
		CTable ctable = gen.execute(rep);

		// set the label used in the y-axis of the chart
		if (varUnitRef != null)
			ctable.setUnitTypeLabel(varUnitRef.getUnitTypeLabel());
		
		return ctable;
	}


	/**
	 * Return the instance of the {@link ReportResources} class
	 * @return
	 */
	protected ReportResources getReportResources() {
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace");
		if (ws.getExtension() != null) {
			ReportResources res = (ReportResources)Component.getInstance("reportResources." + ws.getExtension());
			if (res != null)
				return res;
		}
		return (ReportResources)Component.getInstance("reportResources");
	}

	
	/**
	 * Return an instance of the {@link CTable} containing an error message
	 * @param errorKey
	 * @return
	 */
	protected CTable returnError(String errorKey, Object... params) {
		CTable tbl = new CTable();
		String msg = Messages.instance().get(errorKey);
		if (params != null) 
			msg = MessageFormat.format(msg, params);
		tbl.setErrorMessage(msg);
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
	protected void displayTable(DataTableImpl table) {
/*		for (Row row: table.getRows()) {
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
*/	}

	
	/**
	 * Generate a list of filter options to be serialized to the client from a {@link Filter} instance
	 * @param filter instance of the {@link Filter} interface representing the filter
	 * @param param is a parameter recognized by the filter 
	 * @return List of {@link CItem} objects to be sent to the client as options to the filter
	 */
	protected ArrayList<CItem> generateFilterOptions(Filter filter, String param) {
		List<FilterOption> options = filter.getFilterOptions(param);
		if (options == null)
			return null;

		ArrayList<CItem> lst = new ArrayList<CItem>();
		for (FilterOption opt: options) {
			lst.add(new CItem(opt.getValue().toString(), opt.getLabel()));
		}
		return lst;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#getFilterOptions(java.lang.String, java.lang.String)
	 */
	@Override
	@WebRemote
	public ArrayList<CItem> getFilterOptions(String filterid, String param) {
		ReportResources res = getReportResources();
		Filter filter = res.findFilterById(filterid);
		if (filter == null)
			return null;

		return generateFilterOptions(filter, param);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.shared.ReportService#getPatients(java.util.HashMap, java.util.HashMap, java.util.HashMap, int)
	 */
	@Override
	@WebRemote
	public CPatientList getPatients(HashMap<String, String> filters, int page) {

		IndicatorReport rep = IndicatorReportFactory.instance().createCaseIndicator();
		ReportResources res = getReportResources();

		int pageSize = 20;

		// set filter values
		if (filters != null) {
			for (String id: filters.keySet()) {
				Filter var = res.findFilterById(id);
				if (var != null) {
					String value = filters.get(id);
					rep.addFilter((Filter)var, var.filterValueFromString( value ));
				}
			}
		}

		DataTableQuery tbl = rep.getDetailedReport("tbcase.id, patient.patient_name, patient.middlename, " +
				"patient.lastname, patient.gender, patient.recordnumber, tbcase.casenumber, tbcase.registrationCode, tbcase.suspectRegistrationCode",
				"patient_name", page, pageSize);

		// prepare mock tbcase to display the proper name and case number according to the configuration
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace");
		TbCase tbcase = new TbCase();
		Patient p = new Patient();
		tbcase.setPatient(p);
		tbcase.getPatient().setWorkspace(ws);

		ArrayList<CPatient> pacs = new ArrayList<CPatient>();
		for (Row row: tbl.getRows()) {
			Integer id = (Integer)row.getValue(0);
			p.setName((String)row.getValue(1));
			p.setMiddleName((String)row.getValue(2));
			p.setLastName((String)row.getValue(3));
			Integer gender = (Integer)row.getValue(4);
			p.setRecordNumber((Integer)row.getValue(5));
			tbcase.setCaseNumber((Integer)row.getValue(6));
			tbcase.setRegistrationCode((String)row.getValue(7));
			tbcase.setSuspectRegistrationCode((String)row.getValue(8));

			CPatient pac= new CPatient();
			pac.setId(id);
			
			pac.setName(p.getFullName());
			pac.setNumber(tbcase.getDisplayCaseNumber());
			pac.setGender(gender);
			pacs.add(pac);
		}
		
		CPatientList lst = new CPatientList();
		lst.setItems(pacs);
		lst.setRecordCount(rep.getRecordCount());
		lst.setPageSize(pageSize);
		
		return lst;
	}
}
