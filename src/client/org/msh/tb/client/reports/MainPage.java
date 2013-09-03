package org.msh.tb.client.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.msh.tb.client.commons.MessagePanel;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.TableData.HeaderLabel;
import org.msh.tb.client.reports.chart.ChartSeries;
import org.msh.tb.client.reports.chart.ChartType;
import org.msh.tb.client.reports.chart.ChartView;
import org.msh.tb.client.reports.resources.ReportConstants;
import org.msh.tb.client.shared.ReportService;
import org.msh.tb.client.shared.ReportServiceAsync;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CReportData;
import org.msh.tb.client.shared.model.CReportUI;
import org.msh.tb.client.shared.model.CTable;
import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;
import org.msh.tb.client.shared.model.CVariable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents the main content of the report page. This class is the entry
 * point for the execution of the data analysis tool 
 *  
 * @author Ricardo Memoria
 *
 */
public class MainPage extends Composite implements StandardEventHandler {

	private static final Binder binder = GWT.create(Binder.class);
	private static MainPage singletonInstance;

	interface Binder extends UiBinder<Widget, MainPage> { }

	@UiField VerticalPanel pnlRowVariables;
	@UiField VerticalPanel pnlColVariables;
	@UiField HTMLPanel pnlContent;
	@UiField Button btnAddFilter;
	@UiField VerticalPanel pnlFilters;
	@UiField Button btnGenerate;
	@UiField TableView tblResult;
	@UiField Anchor lnkChartType;
	@UiField MessagePanel pnlMessage;
	
	private GroupFiltersPopup filtersPopup;
	private GroupVariablesPopup varsPopup;
	private CReportUI reportUI;
	private ChartPopup chartPopup;
	private CaseListPopup patientListPopup;
	// selected column and row to draw the chart
	private int selectedCell = TableData.CELL_TITLE;
	private boolean rowSelected = true;

	/**
	 * Contains the data of the table to be rendered
	 */
	private TableData tableData = new TableData();

	/**
	 * RPC Report services
	 */
	private ReportServiceAsync service = GWT.create(ReportService.class);

	public static final Resources resources = GWT.create(Resources.class);
	
	public static final ReportConstants messages = GWT.create(ReportConstants.class);

	/**
	 * List of chart images to be displayed for chart selection
	 */
	public static ImageResource[] chartImgs = {resources.imgChartLine(), resources.imgChartSpline(), 
		resources.imgChartArea(), resources.imgChartAreaSpline(), resources.imgChartColumn(),
		resources.imgChartBar(), resources.imgChartPie() };

	private ChartView chart = new ChartView();
	
	
	/**
	 * Default constructor
	 */
	private MainPage() {
		super();
		singletonInstance = this;
		initWidget(binder.createAndBindUi(this));

		lnkChartType.addStyleName("chart-button");

		// select the first chart as default
		selectChart(ChartType.CHART_COLUMN);
	}


	/**
	 * Called when the chart type is called
	 */
	@UiHandler("lnkChartType")
	public void lnkChartTypeClick(ClickEvent event) {
		if (chartPopup == null) {
			chartPopup = new ChartPopup();
			pnlContent.add(chartPopup);
		}
		
		Widget source = (Widget)event.getSource();
		chartPopup.showRelativeTo(source);
	}


	/**
	 * Show a pop-up window displaying the filters to be selected
	 * @param clickEvent
	 */
	@UiHandler("btnAddFilter")
	public void btnAddFilterClick(ClickEvent clickEvent) {
		if (filtersPopup == null) {
			StandardEventHandler listener = new StandardEventHandler() {
				@Override
				public void eventHandler(Object eventType, Object data) {
					includeNewFilter((CFilter)data);
				}
			};
			filtersPopup = new GroupFiltersPopup(listener);
			filtersPopup.setGroups(reportUI.getGroups());
			pnlContent.add(filtersPopup);
		}
		Widget source = (Widget)clickEvent.getSource();
		filtersPopup.showPopup(source);
	}


	/**
	 * Called when the user clicks on the generate report button
	 * @param clickEvent
	 */
	@UiHandler("btnGenerate")
	public void btnGenerateClick(ClickEvent clickEvent) {
		CReportData data = prepareReportData();
		if (data == null)
			return;
		
		tblResult.setVisible(false);
		chart.clear();

		btnGenerate.setEnabled(false);
		service.executeReport(data, new StandardCallback<CTable>() {
			@Override
			public void onSuccess(CTable res) {
				btnGenerate.setEnabled(true);
				// did the server sent any data ?
				if (res == null) {
					showErrorMessage(messages.noResultFound());
					return;
				}
				// there was an error message?
				if (res.getErrorMessage() != null) {
					showErrorMessage(res.getErrorMessage());
				}
				else {
					tableData.update(res);
					updateReport();
				}
			}

			@Override
			public void onFailure(Throwable except) {
				super.onFailure(except);
				btnGenerate.setEnabled(true);
			}
		});
	}


	public void showErrorMessage(String msg) {
		pnlMessage.setVisible(true);
		pnlMessage.setText(msg);
	}
	
	/**
	 * Prepare the data of the report (variables and filters) to be sent to the server
	 * @return instance of {@link CReportData} or null if there are validation errors
	 */
	private CReportData prepareReportData() {
		tableData.setColVariables( getVariables(pnlColVariables) );
		tableData.setRowVariables( getVariables(pnlRowVariables) );

		// mount list of variables
		ArrayList<String> rows = getSelectedVariables(tableData.getRowVariables());
		ArrayList<String> cols = getSelectedVariables(tableData.getColVariables());
		
		if ((rows.size() == 0) || (cols.size() == 0)) {
			showErrorMessage(messages.noVariableDefined());
			return null;
		}
		else pnlMessage.setVisible(false);
		
		CReportData data = new CReportData();
		data.setRowVariables(rows);
		data.setColVariables(cols);

		// mount list of filters
		HashMap<String, String> filters = new HashMap<String, String>();
		mountFilterValues(filters);

		// just send something if any filter was set
		if (filters.size() > 0)
			data.setFilters(filters);
		
		return data;
	}
	
	
	/**
	 * Get the values of each filter on the page and put them in a map with
	 * its filter id and value
	 * @param filters the instance of the {@link HashMap} class that will receive 
	 * the filter values
	 */
	protected void mountFilterValues(HashMap<String, String> filters) {
		for (int i = 0; i < pnlFilters.getWidgetCount(); i++) {
			FilterPanel pnl = (FilterPanel)pnlFilters.getWidget(i);
			String val = pnl.getFilterValue();
			if (val != null) {
				filters.put(pnl.getFilter().getId(), val);
			}
		}
	}
	
	/**
	 * Return the list of variables declared in the panel
	 * @param pnl
	 * @return
	 */
	private ArrayList<String> getSelectedVariables(List<CVariable> vars) {
		ArrayList<String> varNames = new ArrayList<String>();

		for (CVariable var: vars) {
			varNames.add(var.getId());
		}
		return varNames;
	}

	
	/**
	 * Return the list of variables in a panel
	 * @param pnl
	 * @return
	 */
	protected List<CVariable> getVariables(VerticalPanel pnl) {
		ArrayList<CVariable> vars = new ArrayList<CVariable>();
		for (int i = 0; i < pnl.getWidgetCount(); i++) {
			CVariable var = ((VariablePanel)pnl.getWidget(i)).getVariable();
			if (var != null) {
				vars.add(var);
			}
		}
		return vars;
	}


	/**
	 * Update report data with table sent from the server
	 * @param table
	 */
	protected void updateReport() {
		tblResult.update(tableData);
		updateChart();
	}



	/**
	 * Update the chart applying the selected type and values
	 */
	protected void updateChart() {
		if (tableData.getTable() == null)
			return;

		chart.clear();

		String title = tableData.getTable().getUnitTypeLabel();
		if (title == null)
			title = messages.numberOfCases();
		chart.setyAxisTitle(title);

		// is row selected ?
		if (rowSelected) {
			// clicked on the position of the table 0,0 ?
			if (selectedCell == TableData.CELL_TITLE) {
				chart.setTitle(tableData.getRowVariables().get(0).getName() + " x " + tableData.getColVariables().get(0).getName());
				chart.setSubTitle(null);
				
				List<CTableRow> rows = tableData.getTable().getRows();
				for (CTableRow row: rows) {
					ChartSeries series = chart.addSeries(row.getTitle());
					for (int i = 0; i < row.getValues().length; i++) {
						series.addValue(tableData.getColumn(i).getTitle(), row.getValues()[i]);
					}
				}
			}
			else if (selectedCell == TableData.CELL_TOTAL) {
				chart.setTitle(tableData.getColVariables().get(0).getName());
				chart.setSubTitle(messages.total());
				ChartSeries series = chart.addSeries(messages.total());
				int index = 0;
				for (double val: tableData.getTotalRow()) {
					series.addValue(tableData.getColumn(index++).getTitle(), val);
				}
			}
			else {
				// create title of the report
				CTableRow row = tableData.getTable().getRows().get(selectedCell);
				String colTitle = tableData.getColVariables().get(0).getName();
				chart.setTitle(messages.numberOfCasesBy() + " " + colTitle);
				chart.setSubTitle( tableData.getRowVariables().get(row.getVarIndex()).getName() + ": " + row.getTitle());

				// is grouped ?
				if (tableData.isColumnGrouped()) {
					List<HeaderLabel> labels = tableData.mountColumnHeaderLabels();
					
					String prevLabel = "";
					ChartSeries series = null;
					int index = 0;
					for (HeaderLabel label: labels) {
						if ((series == null) || (!label.getGroupLabel().equals(prevLabel))) {
							series = chart.addSeries(label.getGroupLabel());
							prevLabel = label.getGroupLabel();
						}
						series.addValue(label.getItemLabel(), row.getValues()[index]);
						index++;
					}
				}
				else {
					// handle single row selection
					List<CTableColumn> cols = tableData.getRowsHeader().get(0);
					ChartSeries series = chart.addSeries(row.getTitle());
					int i = 0;
					for (CTableColumn col: cols) {
						series.addValue(col.getTitle(), row.getValues()[i++]);
					}
				}
			}
		}
		else {
			// clicked on the position of the table 0,0 ?
			if (selectedCell == TableData.CELL_TITLE) {
				chart.setTitle(tableData.getRowVariables().get(0).getName() + " x " + tableData.getColVariables().get(0).getName());
				chart.setSubTitle(null);
				
				List<CTableRow> rows = tableData.getTable().getRows();
				int colindex = 0;
				for (CTableColumn col: tableData.getHeaderColumns()) {
					ChartSeries series = chart.addSeries(col.getTitle());
					for (CTableRow row: rows) {
						series.addValue(row.getTitle(), row.getValues()[colindex]);
					}
					colindex++;
				}
			}
			else 
			if (selectedCell == TableData.CELL_TOTAL) {
				chart.setTitle(tableData.getRowVariables().get(0).getName());
				chart.setSubTitle(messages.total());
				ChartSeries series = chart.addSeries(messages.total());
				int index = 0;
				for (double val: tableData.getTotalColumn()) {
					series.addValue(tableData.getTable().getRows().get(index++).getTitle(), val);
				}
			}
			else {
				// column was selected
				CTableColumn col = tableData.getColumn(selectedCell);
				String s = col.getTitle();
				
				String colTitle = tableData.getRowVariables().get(0).getName();
				chart.setTitle(messages.numberOfCasesBy() + " " + colTitle);
				chart.setSubTitle(tableData.getColumnDisplaySelection(selectedCell));

				// the rows are grouped ?
				if (tableData.isRowGrouped()) {
					List<HeaderLabel> lst = tableData.mountRowHeaderLabels();

					ChartSeries series = null;
					String prevName = null;
					for (HeaderLabel lbl: lst) {
						if ((prevName == null) || (!prevName.equals(lbl.getGroupLabel()))) {
							series = chart.addSeries(lbl.getGroupLabel());
							prevName = lbl.getGroupLabel();
						}
						series.addValue(lbl.getItemLabel(), lbl.getRow().getValues()[selectedCell]);
					}
				}
				else {
					// just one variable selected for the row
					ChartSeries series = chart.addSeries(s);
					for (CTableRow row: tableData.getTable().getRows()) {
						series.addValue(row.getTitle(), row.getValues()[selectedCell]);
					}
				}
			}
		}

		chart.update();
	}


	/**
	 * Include a new filter in the list of filters
	 * @param item
	 */
	protected void includeNewFilter(CFilter item) {
		pnlFilters.add(new FilterPanel(item));
	}


	/**
	 * Initialize the report page loading the content from the server	 
	 */
	public void initialize() {
		service.initialize(new StandardCallback<CReportUI>() {
			@Override
			public void onSuccess(CReportUI rep) {
				reportUI = rep;
				updateReportData();
			}
		});
	}

	
	/**
	 * Show popup window with the list of patients of the clicked cell
	 * @param c
	 * @param r
	 */
	public void showPatientList(int c, int r) {
		HashMap<String, String> filters = new HashMap<String, String>();
		
		mountFilterValues(filters);

		// get variables from the row
		int index = r;
		int level = tableData.getTable().getRows().get(r).getLevel();

		// get key values from rows
		while (index >= 0) {
			CTableRow row = tableData.getTable().getRows().get(index);
			if (row.getLevel() == level) {
				CVariable var = tableData.getRowVariables().get(row.getVarIndex());
				String prevkey = filters.get(var.getId());
				if (prevkey != null)
					 filters.put(var.getId(), row.getKey() + ";" + prevkey);
				else filters.put(var.getId(), row.getKey());
				level--;
				if (level == -1)
					break;
			}
			index--;
		}
		
		// get key values from columns
		CTableColumn col = tableData.getHeaderColumns().get(c);
		while (col != null) {
			CVariable var = tableData.getColVariables().get(col.getLevel());
			filters.put(var.getId(), col.getKey());
			col = col.getParent();
		}

		// create popup if not available
		if (patientListPopup == null) {
			patientListPopup = new CaseListPopup();
		}

		// show popup window
		patientListPopup.showPatients(filters);
	}

	/**
	 * Add initial variables to the report
	 */
	protected void updateReportData() {
		// add initial variables
		pnlRowVariables.add( new VariablePanel(false) );
		pnlColVariables.add( new VariablePanel(false) );
	}
	
	
	/**
	 * Remove a variable panel
	 * @param pnl
	 */
	protected void removeVariablePanel(VariablePanel pnl) {
		VerticalPanel pnlvars = (VerticalPanel)pnl.getParent();
		pnlvars.remove(pnl);
	}

	/**
	 * Remove a filter from the panel
	 * @param pnl
	 */
	protected void removeFilter(FilterPanel pnl) {
		VerticalPanel pnlfilters = (VerticalPanel)pnl.getParent();
		pnlfilters.remove(pnl);
		// there is only one variable left ?
		if (pnlfilters.getWidgetCount() == 1)
			((VariablePanel)pnlFilters.getWidget(0)).setRemoveEnabled(false);
	}

	/**
	 * Called when the variable is changed
	 * @param pnl
	 */
	public void variableChanged(VariablePanel pnl) {
		VerticalPanel vertpnl = (VerticalPanel)pnl.getParent();
		int index = vertpnl.getWidgetIndex(pnl);
		// is the last variable ?
		if (index == vertpnl.getWidgetCount() - 1) {
			// add a new vertical panel
			vertpnl.add( new VariablePanel(false) );
			pnl.setRemoveEnabled(true);
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.commons.StandardEventHandler#eventHandler(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void eventHandler(Object eventType, Object data) {
		if (VariablePanel.VARIABLE_DELETE.equals(eventType)) {
			VariablePanel pnl = (VariablePanel)data;
			VerticalPanel pnlvars = (VerticalPanel)pnl.getParent();
			pnlvars.remove(pnl);
		}
	}
	
	
	/**
	 * Return the list of groups of information
	 * @return
	 */
	public List<CGroup> getGroups() {
		return (reportUI != null? reportUI.getGroups(): null);
	}
	
	/**
	 * @return
	 */
	public static MainPage instance() {
		if (singletonInstance == null)
			new MainPage();
		return singletonInstance;
	}
	
	
	/**
	 * @return
	 */
	public GroupVariablesPopup getVariablePopup() {
		if (varsPopup == null) {
			varsPopup = new GroupVariablesPopup(this);
			varsPopup.setGroups(reportUI.getGroups());
		}
		return varsPopup;
	}
	
	
	/**
	 * Select the chart from a number of 0 to the max number of charttypes array 
	 * @param chartindex
	 */
	public void selectChart(ChartType chartType) {
		Image img = new Image(chartImgs[chartType.ordinal()]);
		if (DOM.getChildCount(lnkChartType.getElement()) > 0)
			DOM.removeChild(lnkChartType.getElement(), DOM.getFirstChild(lnkChartType.getElement()));
		DOM.insertChild(lnkChartType.getElement(), img.getElement(), 0);

		chart.setSelectedChart(chartType);
		updateChart();
	}


	/**
	 * @param selectedCol the selectedCol to set
	 */
	public void setSelectedCol(int selectedCol) {
		this.selectedCell = selectedCol;
		rowSelected = false;
		updateChart();
	}


	/**
	 * @param selectedRow the selectedRow to set
	 */
	public void setSelectedRow(int selectedRow) {
		this.selectedCell = selectedRow;
		rowSelected = true;
		updateChart();
	}


	/**
	 * @return the service
	 */
	public ReportServiceAsync getService() {
		return service;
	}


	/**
	 * @return the messages
	 */
	public static ReportConstants getMessages() {
		return messages;
	}
	
}
