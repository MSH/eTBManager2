package org.msh.tb.client.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.msh.tb.client.App;
import org.msh.tb.client.AppModule;
import org.msh.tb.client.commons.MessagePanel;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.chart.ChartReport;
import org.msh.tb.client.reports.chart.ChartType;
import org.msh.tb.client.reports.chart.ChartView;
import org.msh.tb.client.shared.ReportService;
import org.msh.tb.client.shared.ReportServiceAsync;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CInitializationData;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.shared.model.CReportRequest;
import org.msh.tb.client.shared.model.CReportResponse;
import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;
import org.msh.tb.client.shared.model.CVariable;
import org.msh.tb.client.tableview.TableData;
import org.msh.tb.client.tableview.TableData.TableSelection;
import org.msh.tb.client.tableview.TableView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents the main content of the report page. This class is the entry
 * point for the execution of the data analysis tool 
 *  
 * @author Ricardo Memoria
 *
 */
public class ReportMain extends Composite implements AppModule {

	private static final Binder binder = GWT.create(Binder.class);
	private static ReportMain singletonInstance;

	interface Binder extends UiBinder<Widget, ReportMain> { }

	@UiField VariablesPanel pnlRowVariables;
	@UiField VariablesPanel pnlColVariables;
	@UiField HTMLPanel pnlContent;
	@UiField Button btnAddFilter;
	@UiField FiltersPanel pnlFilters;
	@UiField Button btnGenerate;
	@UiField TableView tblResult;
	@UiField Anchor lnkChartType;
	@UiField MessagePanel pnlMessage;
	@UiField Label txtTitle;
	@UiField HTMLPanel pnlReport;
	@UiField ReportListPanel pnlReportList;
	@UiField TextBox edtTitle;
	
	private GroupFiltersPopup filtersPopup;
//	private GroupVariablesPopup varsPopup;
	private CInitializationData reportUI;
	private ChartPopup chartPopup;
	private OptionsPopup optionsPopup;
	private CaseListPopup patientListPopup;

	// as default, start with a new report
	private CReport report;
	
	/**
	 * Contains the data of the table to be rendered
	 */
	private TableData tableData = new TableData();

	/**
	 * RPC Report services
	 */
	private ReportServiceAsync service = GWT.create(ReportService.class);

	public static final Resources resources = GWT.create(Resources.class);

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
	private ReportMain() {
		super();
		singletonInstance = this;
		initWidget(binder.createAndBindUi(this));

		lnkChartType.addStyleName("chart-button");

		// select the first chart as default
		selectChart(ChartType.CHART_COLUMN);
		
		btnGenerate.addStyleName("btn-alt");
		
		pnlReport.setVisible(false);
		// map the title editing events
		edtTitle.setVisible(false);
		txtTitle.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				titleClick(event);
			}
		});
		edtTitle.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				changeEditTitle();
			}
		});
		edtTitle.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				editTitleKeyDown(event);
			}
		});
	}


	/**
	 * Called when the user press a key when editing the title
	 * @param event
	 */
	protected void editTitleKeyDown(KeyDownEvent event) {
		// user pressed the enter key ?
		if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
			changeEditTitle();
		}
		
		// user pressed the esc key ?
		if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
			edtTitle.setVisible(false);
			txtTitle.setVisible(true);
		}
	}


	/**
	 * @param event
	 */
	protected void changeEditTitle() {
		String s = edtTitle.getText().trim();
		if (s.isEmpty()) {
			edtTitle.setFocus(true);
			return;
		}
		
		report.setTitle(s);
		txtTitle.setText(s);
		edtTitle.setVisible(false);
		txtTitle.setVisible(true);
	}


	/**
	 * Called when user clicks on the title of the report, in order to
	 * change its name
	 * @param event instance of {@link ClickEvent}
	 */
	protected void titleClick(ClickEvent event) {
		txtTitle.setVisible(false);
		edtTitle.setText(txtTitle.getText());
		edtTitle.setVisible(true);
		edtTitle.setFocus(true);
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
	 * Called when user clicks on the options button
	 * @param event
	 */
	@UiHandler("lnkMenu")
	public void lnkMenuClick(ClickEvent event) {
		if (optionsPopup == null) {
			optionsPopup = new OptionsPopup();
			pnlContent.add(optionsPopup);
		}
		
		Widget source = (Widget)event.getSource();
		optionsPopup.showRelativeTo(source);
	}

	
	/**
	 * Called when clicking on the open button beside the report title
	 * @param event
	 */
	@UiHandler("lnkOpen")
	public void lnkOpen(ClickEvent event) {
		openReportList(false);
	}

	/**
	 * Save report
	 * @param event
	 */
	@UiHandler("lnkSave")
	public void lnkSaveClick(ClickEvent event) {
		if (isNewReport()) {
			SaveDlg.openDialog(false);
		}
		else {
			ReportCRUDServices.saveReport(null);
		}
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
					pnlFilters.addFilter((CFilter)data, null);
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
		CReportRequest data = prepareReportRequest();
		if (data == null)
			return;
		
		tblResult.setVisible(false);
		chart.clear();

		btnGenerate.setEnabled(false);
		service.executeReport(data, new StandardCallback<CReportResponse>() {
			@Override
			public void onSuccess(CReportResponse res) {
				btnGenerate.setEnabled(true);
				// did the server sent any data ?
				if (res == null) {
					showErrorMessage(App.messages.noResultFound());
					return;
				}
				// there was an error message?
				if (res.getErrorMessage() != null) {
					showErrorMessage(res.getErrorMessage());
				}
				else {
					tableData.update(res);
					updateReportResponse();
				}
			}

			@Override
			public void onFailure(Throwable except) {
				super.onFailure(except);
				btnGenerate.setEnabled(true);
			}
		});
	}
	

	/**
	 * Display an error message in the main report page
	 * @param msg is the message to be displayed
	 */
	public void showErrorMessage(String msg) {
		pnlMessage.setVisible(true);
		pnlMessage.setText(msg);
	}
	
	/**
	 * Prepare the data of the report (variables and filters) to be sent to the server
	 * @return instance of {@link CReportRequest} or null if there are validation errors
	 */
	public CReportRequest prepareReportRequest() {
		tableData.setColVariables( pnlColVariables.getVariables() );
		tableData.setRowVariables( pnlRowVariables.getVariables() );

		// mount list of variables
		ArrayList<String> rows = pnlRowVariables.getDeclaredVariables();
		ArrayList<String> cols = pnlColVariables.getDeclaredVariables();
		
		if ((rows.size() == 0) || (cols.size() == 0)) {
			showErrorMessage(App.messages.noVariableDefined());
			return null;
		}
		else pnlMessage.setVisible(false);
		
		CReportRequest data = new CReportRequest();
		data.setRowVariables(rows);
		data.setColVariables(cols);

		// mount list of filters
		HashMap<String, String> filters = pnlFilters.getDeclaredFilters();
//		mountFilterValues(filters);

		// just send something if any filter was set
		if (filters.size() > 0)
			data.setFilters(filters);
		
		return data;
	}
	

	/**
	 * Update report data with table sent from the server
	 * @param table
	 */
	protected void updateReportResponse() {
		tblResult.update(tableData);
		updateChart();
	}



	/**
	 * Update the chart applying the selected type and values
	 */
	protected void updateChart() {
		ChartReport.update(chart, tableData);
/*		if (tableData.getTable() == null)
			return;

		chart.clear();

		String title = tableData.getTable().getUnitTypeLabel();
		if (title == null)
			title = App.messages.numberOfCases();
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
				chart.setSubTitle(App.messages.total());
				ChartSeries series = chart.addSeries(App.messages.total());
				int index = 0;
				for (double val: tableData.getTotalRow()) {
					series.addValue(tableData.getColumn(index++).getTitle(), val);
				}
			}
			else {
				// create title of the report
				CTableRow row = tableData.getTable().getRows().get(selectedCell);
				String colTitle = tableData.getColVariables().get(0).getName();
				chart.setTitle(App.messages.numberOfCasesBy() + " " + colTitle);
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
				chart.setSubTitle(App.messages.total());
				ChartSeries series = chart.addSeries(App.messages.total());
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
				chart.setTitle(App.messages.numberOfCasesBy() + " " + colTitle);
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
*/	}


	/**
	 * Initialize the report page loading the content from the server	 
	 */
	public void run() {
		service.initialize(new StandardCallback<CInitializationData>() {
			@Override
			public void onSuccess(CInitializationData rep) {
				reportUI = rep;
				
				if ((reportUI.getReports() != null) && (reportUI.getReports().size() > 0)) {
					openReportList(false);
				}
				else {
					closeReportList();
					newReport();
				}
			}
		});
	}

	
	/**
	 * Show popup window with the list of patients of the clicked cell
	 * @param c
	 * @param r
	 */
	public void showPatientList(int c, int r) {
		HashMap<String, String> filters = pnlFilters.getDeclaredFilters();

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
	 * Return the list of groups of information
	 * @return
	 */
	public List<CGroup> getGroups() {
		return (reportUI != null? reportUI.getGroups(): null);
	}
	
	/**
	 * @return
	 */
	public static ReportMain instance() {
		if (singletonInstance == null)
			new ReportMain();
		return singletonInstance;
	}
	
	
	/**
	 * Select the chart from a number of 0 to the max number of charttypes array 
	 * @param chartindex
	 */
	public void selectChart(ChartType chartType) {
		Image img = new Image(chartImgs[chartType.ordinal()]);
		Element el = (Element)lnkChartType.getElement();
		if (DOM.getChildCount(el) > 0)
			el.removeChild(DOM.getFirstChild(el));
		DOM.insertChild(el, img.getElement(), 0);

		chart.setSelectedChart(chartType);
		updateChart();
	}

	
	/**
	 * Return the selected chart type
	 * @return
	 */
	public ChartType getChartType() {
		return chart.getSelectedChart();
	}

	/**
	 * @param selectedCol the selectedCol to set
	 */
	public void setSelectedCol(int selectedCol) {
		tableData.setSelection(TableSelection.COLUMN);
		tableData.setSelectedCell(selectedCol);
		updateChart();
	}


	/**
	 * @param selectedRow the selectedRow to set
	 */
	public void setSelectedRow(int selectedRow) {
		tableData.setSelection(TableSelection.ROW);
		tableData.setSelectedCell(selectedRow);
		updateChart();
	}


	/**
	 * @return the service
	 */
	public ReportServiceAsync getService() {
		return service;
	}

	
	/**
	 * Return information about the report sent from the server
	 * @return instance of {@link CInitializationData} class
	 */
	public CInitializationData getReportUI() {
		return reportUI;
	}


	/**
	 * @return the report
	 */
	public CReport getReport() {
		return report;
	}
	
	
	/**
	 * Update the current report being displayed by the system
	 * @param report instance of {@link CReport} class
	 */
	public void updateReport() {
		txtTitle.setText(report.getTitle());
		pnlColVariables.update(report.getColumnVariables());
		pnlRowVariables.update(report.getRowVariables());
		pnlFilters.update(report.getFilters());

		// update chart type
		Integer val = report.getChartType();
		if ((val != null) && (val >= 0) && (val < ChartType.values().length)) {
			selectChart(ChartType.values()[val]);
		}

		// hide the current report being displayed
		tblResult.setVisible(false);
		chart.clear();
	}
	
	
	/**
	 * Check if the report being displayed is a new one
	 * @return true if it's a new report, or false if it's an existing report
	 */
	public boolean isNewReport() {
		return report.getId() == null;
	}
	
	/**
	 * Close the report list, if open
	 */
	public void closeReportList() {
		pnlReportList.hide();
		pnlReport.setVisible(true);
	}
	
	
	/**
	 * Show the report list
	 */
	public void openReportList(boolean reload) {
		pnlReportList.show(reload);
		pnlReport.setVisible(false);
	}
	
	
	/**
	 * Create a new report
	 */
	public void newReport() {
		report = new CReport();
		report.setTitle("New report");

		updateReport();
		closeReportList();
	}
	
	
	/**
	 * Open an existing report
	 * @param id is the report identification
	 */
	public void openReport(Integer id) {
		closeReportList();
		// hide the current report being displayed
		tblResult.setVisible(false);
		chart.clear();
		
		getService().loadReport(id, new StandardCallback<CReport>() {
			@Override
			public void onSuccess(CReport result) {
				report = result;
				// set default selection of table series
				if (report.getTblSelectedCell() != null) {
					tableData.setSelectedCell(report.getTblSelectedCell());
				}
				if (report.getTblSelection() != null) {
					tableData.setSelection( TableSelection.values()[ report.getTblSelection() ]);
				}
				updateReport();
				// run the report
				btnGenerate.click();
			}
		});
	}
	
	/**
	 * Delete the current report
	 */
	public void deleteReport() {
		
	}
	
	
	/**
	 * Search for a variable by its ID
	 * @param id the variable's ID
	 * @return instance of {@link CVariable}, or null if variable is not found
	 */
	public CVariable findVariableById(String id) {
		for (CGroup grp: reportUI.getGroups()) {
			if (grp.getVariables() != null) {
				for (CVariable var: grp.getVariables()) {
					if (var.getId().equals(id)) {
						return var;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Search for a filter by its ID
	 * @param id is the filter's ID
	 * @return instance of {@link CVariable}, or null if variable is not found
	 */
	public CFilter findFilterById(String id) {
		for (CGroup grp: reportUI.getGroups()) {
			if (grp.getFilters() != null) {
				for (CFilter filter: grp.getFilters()) {
					if (filter.getId().equals(id)) {
						return filter;
					}
				}
			}
		}
		return null;
	}


	/**
	 * @return the tableData
	 */
	public TableData getTableData() {
		return tableData;
	}
}
