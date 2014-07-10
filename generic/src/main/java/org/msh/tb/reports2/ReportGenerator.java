/**
 * 
 */
package org.msh.tb.reports2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;
import org.msh.reports.IndicatorReport;
import org.msh.reports.datatable.Row;
import org.msh.reports.filters.Filter;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.indicator.DataTableIndicator;
import org.msh.reports.query.DataTableQuery;
import org.msh.reports.variables.Variable;
import org.msh.tb.application.App;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CInitializationData;
import org.msh.tb.client.shared.model.CItem;
import org.msh.tb.client.shared.model.CPatient;
import org.msh.tb.client.shared.model.CPatientList;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.shared.model.CReportRequest;
import org.msh.tb.client.shared.model.CReportResponse;
import org.msh.tb.client.shared.model.CVariable;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.Report;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.User;
import org.msh.tb.entities.Workspace;
import org.msh.tb.login.UserSession;
import org.msh.tb.reports2.variables.DateFieldVariable;
import org.msh.tb.reports2.variables.EmptyVariable;
import org.msh.utils.date.DateUtils;

/**
 * Simple helper class that generates the report and pack it inside a {@link CReportResponse}
 * class, ready to be sent back to the client
 * 
 * @author Ricardo Memoria
 *
 */
public class ReportGenerator {

	
	/**
	 * Return initialized data to be sent back to the client, containing all
	 * variables, filters and org.msh.reports available for the current user and current workspace
	 * @return instance of {@link CInitializationData} 
	 */
	public static CInitializationData createInitializationData() {
		// get resources containing groups, filters and variables
		ReportResources res = ReportResources.instance();

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
					CFilter f = new CFilter(filter.getId(), filter.getLabel(), filter.getFilterType(), null);
					if (!filter.isFilterLazyInitialized())
						f.setOptions(generateFilterOptions(filter, null));
					// send options if they are not sent remotely
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
					CVariable v = new CVariable(var.getId(), var.getLabel());
					vars[index++] = v;
				}
				grp.setVariables(vars);
			}
			
			lst.add(grp);
		}

		CInitializationData rep = new CInitializationData();
		rep.setGroups( lst );
		
		rep.setReports(getReportList());
		
		// send the current date in the server to the client
		rep.setCurrentDate(DateUtils.getDate());

		return rep;
	}
	

	/**
	 * Return the list of available org.msh.reports, ready to be sent back to the client
	 * @return instance of {@link ArrayList} containing {@link CReport} data
	 */
	public static ArrayList<CReport> getReportList() {
		// get the list of available org.msh.reports
		ReportDAO dao = (ReportDAO)App.getComponent("reportDAO");
		List<Report> lst = dao.getReportList();
		
		// get the current user
		User user = UserSession.getUser();
		
		ArrayList<CReport> reps = new ArrayList<CReport>();
		
		for (Report rep: lst) {
			CReport item = new CReport();
			item.setId(rep.getId());
			item.setTitle(rep.getTitle());
			item.setMyReport(rep.getOwner().getId().equals(user.getId()));
			
			reps.add(item);
		}
		
		return reps;
	}
	
	
	/**
	 * Generate a report from an instance of {@link CReportRequest} class
	 * @param reportData instance of {@link CReportRequest} containing the data
	 * @return the report content in a {@link CReportResponse} class
	 */
	public static CReportResponse generateReport(CReportRequest reportData) {
		// check validation rules
        if (reportData == null) {
            return null;
        }

        // get report variables and filters
        ReportResources res = ReportResources.instance();

        // check column variables
        if ((reportData.getColVariables() == null) || (reportData.getColVariables().size() == 0)) {
            reportData.setColVariables(new ArrayList<String>());
            reportData.getColVariables().add("emptyCol");
            // add an empty variable for the column
            res.addVariable(res.getGroups().get(0), new EmptyVariable("emptyCol"));
        }

        // check row variables
		if ((reportData.getRowVariables() == null) || (reportData.getRowVariables().size() == 0)) {
            reportData.setRowVariables(new ArrayList<String>());
            reportData.getRowVariables().add("emptyRow");
            // add an empty variable for the row
            res.addVariable(res.getGroups().get(0), new EmptyVariable("emptyRow"));
        }

        // create indicator report
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
		CReportResponse ctable = gen.execute(rep);

		// set the label used in the y-axis of the chart
		if (varUnitRef != null)
			ctable.setUnitTypeLabel(varUnitRef.getUnitTypeLabel());
		
		return ctable;
	}
	
	/**
	 * Return an instance of the {@link CReportResponse} containing an error message
	 * @param errorKey
	 * @return
	 */
	protected static CReportResponse returnError(String errorKey, Object... params) {
		CReportResponse tbl = new CReportResponse();
		String msg = Messages.instance().get(errorKey);
		if (params != null) 
			msg = MessageFormat.format(msg, params);
		tbl.setErrorMessage(msg);
		return tbl;
	}
	
	/**
	 * Return the list of patients for a given list of filters
	 * @param filters the filters and its values
	 * @param page 0-based page index for the list of patients
	 * @return
	 */
	public static CPatientList getPatients(HashMap<String, String> filters, int page) {
		IndicatorReport rep = IndicatorReportFactory.instance().createCaseIndicator();
		ReportResources res = ReportResources.instance();

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
	
	/**
	 * Generate a list of filter options to be serialized to the client from a {@link Filter} instance
	 * @param filter instance of the {@link Filter} interface representing the filter
	 * @param param is a parameter recognized by the filter 
	 * @return List of {@link CItem} objects to be sent to the client as options to the filter
	 */
	public static ArrayList<CItem> generateFilterOptions(Filter filter, String param) {
		List<FilterOption> options = filter.getFilterOptions(param);
		if (options == null)
			return null;

		ArrayList<CItem> lst = new ArrayList<CItem>();
		for (FilterOption opt: options) {
			lst.add(new CItem(opt.getValue().toString(), opt.getLabel()));
		}
		return lst;
	}

	
	/**
	 * Save a report sent from the client
	 * @param crep
	 */
	public static Integer saveReport(CReport crep) {
		ReportDAO dao = (ReportDAO)App.getComponent("reportDAO");

		Report report;
		if (crep.getId() != null) {
			report = dao.getReport(crep.getId());
		}
		else {
			report = new Report();
		}

		ReportJson.convertFromClient(crep, report);
		
		report = dao.saveReport(report);
		
		return report.getId();
	}
	
	
	/**
	 * Load the data of a report and include it in a {@link CReport} class
	 * @param id
	 * @return
	 */
	public static CReport getReport(Integer id) {
		// load report
		ReportDAO dao = (ReportDAO)App.getComponent("reportDAO");
		Report rep = dao.getReport(id);

		// load current user
		User user = UserSession.getUser();
		
		CReport report = ReportJson.convertToClient(rep);
		
		report.setMyReport( user.getId().equals(rep.getOwner().getId()) );
		
		return report;
	}
	
	
	/**
	 * Delete a report
	 * @param id
	 */
	public static void deleteReport(Integer id) {
		ReportDAO dao = (ReportDAO)App.getComponent("reportDAO");
		dao.deleteReport(id);
	}
}
