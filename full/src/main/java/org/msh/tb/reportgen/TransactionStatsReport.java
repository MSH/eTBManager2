package org.msh.tb.reportgen;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.ViewService;
import org.msh.tb.entities.Workspace;
import org.msh.tb.reports.ReportSelection;
import org.msh.utils.date.DateUtils;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;
import org.msh.utils.reportgen.data.DataTableTransform;
import org.msh.utils.reportgen.data.TableItem;
import org.msh.utils.reportgen.highchart.ChartCreator;
import org.msh.utils.reportgen.highchart.ChartOptions;
import org.msh.utils.reportgen.highchart.ChartType;
import org.msh.utils.reportgen.highchart.Series;
import org.msh.utils.reportgen.layouts.ReportTableLayout;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Name("transactionStatsReport")
public class TransactionStatsReport {

	@In(create=true) ReportSelection reportSelection;
	
	private ReportQuery reportQuery;
	private Variable varUser = new UserLogVariable();
	private Variable varRole = new RoleVariable();
	private Variable varMonth = new MonthVariable();
	private Variable varAdminUnit = new AdminUnitVariable();
	private Variable varUnit = new UnitVariable();
	
	private List<SelectItem> options;
	private List<SelectItem> groupOptions;
	private List<Variable> variables;
	
	private Integer rowVariableIndex;
	private Integer groupRowVariableIndex;
	
	private Variable rowVariable;
	private Variable groupRowVariable;
	
	private ChartCreator chartCreator;
	private ReportTableLayout layout;

	/**
	 * Initialize variables of the report... Default values. Called when page is rendered.
	 */
	public void initVariables() {
		if (rowVariableIndex == null)
			rowVariableIndex = 3;

		if (!ViewService.instance().isFormPost()) {
			if (reportSelection.getIniDate() == null) {
				Date dt = DateUtils.incYears(new Date(), -1);
				reportSelection.setIniMonth(DateUtils.monthOf(dt));
				reportSelection.setIniYear(DateUtils.yearOf(dt));
			}

			if (reportSelection.getEndDate() == null) {
				Date dt = new Date();
				reportSelection.setEndMonth(DateUtils.monthOf(dt));
				reportSelection.setEndYear(DateUtils.yearOf(dt));
			}
		}
	}


	/**
	 * Initialize report
	 */
	public void initialize() {
		reportQuery = new ReportQuery("transactionlog");
		
		Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");
		reportQuery.addRestriction("workspacelog_id = :" + reportQuery.createParameter(workspace.getId()));
		
		ReportSelection sels = (ReportSelection)Component.getInstance("reportSelection");
		if (sels != null) {
			Date iniDate = sels.getIniDate();
			if (iniDate != null)
				reportQuery.addRestriction("transactiondate >= :" + reportQuery.createParameter(iniDate));

			Date endDate = sels.getDayAfterEndDate();
			if (endDate != null)
				reportQuery.addRestriction("transactiondate < :" + reportQuery.createParameter(endDate));
			
			if (sels.getUserLog() != null)
				reportQuery.addRestriction("userlog_id = :" + reportQuery.createParameter(sels.getUserLog().getId()));
			
			if (sels.getUserRole() != null)
				reportQuery.addRestriction("role_id = :" + reportQuery.createParameter(sels.getUserRole().getId()));
		}
		
		reportQuery.addVariable(rowVariable);
		if (groupRowVariable != null)
			reportQuery.addVariable(groupRowVariable);
		reportQuery.addVariable(varMonth);
	}
	
	
	/**
	 * Return the HTML
	 * @return
	 */
	public String getTableHtml() {
		rowVariable = (rowVariableIndex != null? getVariables().get(rowVariableIndex) : null);
		groupRowVariable = (groupRowVariableIndex != null? getVariables().get(groupRowVariableIndex) : null);

		if (rowVariable == null)
			return "No variable selected for row";
		
		initialize();

		layout = new ReportTableLayout();
		layout.setId("tblreport");
		layout.addQuery(reportQuery);
		layout.setStyleClass("table2");
		layout.setColumnVariable(varMonth);
		layout.setRowVariable(rowVariable);
		layout.setGroupRowVariable(groupRowVariable);

		return layout.generateHtml();
	}
	
	
	/**
	 * Return chart data
	 * @return
	 */
	public String getChartData() {
		if (chartCreator == null) 
			createChartData();
		
		return chartCreator.getJsonOptions();
	}
	
	
	/**
	 * Create data to be generated by the chart
	 */
	protected void createChartData() {
		if (layout == null)
			return;
		
		chartCreator = new ChartCreator();
		ChartOptions opt = chartCreator.getOptions();
		opt.getChart().setRenderTo("chartdiv");
		opt.getChart().setType(ChartType.AREASPLINE);
		Series series = opt.addSeries();

		DataTableTransform tbl = layout.getDataTable();
		series.setName(tbl.getRowTotal().getTitle());

		for (TableItem col: tbl.getColumns()) { 
			series.addNewValue((double)tbl.getRowTotal().getColumnValue(col));
			opt.getxAxis().getCategories().add(col.getTitle());
		}

		opt.getTitle().setText( Messages.instance().get("admin.reports.transactionstats"));
	}

	
	/**
	 * Return the list of options for the variable selection
	 * @return
	 */
	public List<SelectItem> getOptions() {
		if (options == null)
			options = createListOptions(false);
		return options;
	}
	
	/**
	 * Create list variables
	 * @return
	 */
	protected List<SelectItem> createListOptions(boolean group) {
		List<SelectItem> lst = new ArrayList<SelectItem>();

		lst.add(new SelectItem(null, "-"));
		for (Variable var: getVariables()) {
			if ((!group) || ((group) && (var != varUser) && (var != varRole)))
				lst.add(new SelectItem(variables.indexOf(var), var.getTitle()));
		}
		
		return lst;
	}

	
	/**
	 * Return the list of variables available
	 * @return
	 */
	public List<Variable> getVariables() {
		if (variables == null) {
			variables = new ArrayList<Variable>();
			variables.add(varUser);
			variables.add(varRole);
			variables.add(varUnit);
			variables.add(varAdminUnit);
		}
		return variables;
	}

	/**
	 * @return the rowVariableIndex
	 */
	public Integer getRowVariableIndex() {
		return rowVariableIndex;
	}


	/**
	 * @param rowVariableIndex the rowVariableIndex to set
	 */
	public void setRowVariableIndex(Integer rowVariableIndex) {
		this.rowVariableIndex = rowVariableIndex;
	}


	/**
	 * @return the groupRowVariableIndex
	 */
	public Integer getGroupRowVariableIndex() {
		return groupRowVariableIndex;
	}


	/**
	 * @param groupRowVariableIndex the groupRowVariableIndex to set
	 */
	public void setGroupRowVariableIndex(Integer groupRowVariableIndex) {
		this.groupRowVariableIndex = groupRowVariableIndex;
	}


	/**
	 * @return the groupOptions
	 */
	public List<SelectItem> getGroupOptions() {
		if (groupOptions == null) 
			groupOptions = createListOptions(true);
		return groupOptions;
	}

}