package org.msh.tb.client.reports;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.AppModule;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.commons.StandardEvent;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.indicators.IndicatorController;
import org.msh.tb.client.indicators.IndicatorEditor;
import org.msh.tb.client.indicators.IndicatorView;
import org.msh.tb.client.indicators.IndicatorWrapper;
import org.msh.tb.client.reports.filters.FiltersPanel;
import org.msh.tb.client.shared.model.CChartType;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.client.shared.model.CReportUIData;
import org.msh.tb.client.ui.LabelEditor;
import org.msh.tb.client.ui.MessagePanel;

import java.util.ArrayList;

/**
 * Represents the main content of the report page. This class is the entry
 * point for the execution of the data analysis tool 
 *  
 * @author Ricardo Memoria
 *
 */
public class ReportMain extends Composite implements AppModule, StandardEventHandler {

	private static final Binder binder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, ReportMain> { }

	@UiField HTMLPanel pnlContent;
	@UiField FiltersPanel pnlGlobalFilters;
	@UiField Button btnGenerate;
	@UiField MessagePanel pnlMessage;
	@UiField HTMLPanel pnlReport;
	@UiField ReportListPanel pnlReportList;
    @UiField FlowPanel pnlIndicators;
    @UiField LabelEditor txtTitle;

    private OptionsPopup popupOptions;

	// as default, start with a new report
	private CReport report;
	

	/**
	 * Default constructor
	 */
	public ReportMain() {
		super();
//		singletonInstance = this;
		initWidget(binder.createAndBindUi(this));

		// select the first chart as default
//		selectChart(CChartType.CHART_COLUMN);

		btnGenerate.addStyleName("btn-alt");
		
		pnlReport.setVisible(false);
		// map the title editing events
        txtTitle.setEventHandler(new StandardEventHandler() {
            @Override
            public void handleEvent(Object eventType, Object data) {
                changeTitle();
            }
        });

        popupOptions = new OptionsPopup();
	}


    /**
     * Initialize the report page loading the content from the server
     */
    public void run() {
        AppResources.reportServices().initialize(new StandardCallback<CReportUIData>() {
            @Override
            public void onSuccess(CReportUIData rep) {
                ReportUtils.setReportUIData(rep);

                if ((rep.getReports() != null) && (rep.getReports().size() > 0)) {
                    openReportList(false);
                } else {
                    closeReportList();
                    newReport();
                }
            }
        });
    }



	/**
	 */
	protected void changeTitle() {
		String s = txtTitle.getText().trim();
        report.setTitle(s);
	}



    @UiHandler("lnkAddIndicator")
    public void lnkAddIndicator(ClickEvent event) {
        newIndicator();
    }

	/**
	 * Called when user clicks on the options button
	 * @param event
	 */
	@UiHandler("lnkMenu")
	public void lnkMenuClick(ClickEvent event) {
        popupOptions.showRelativeTo((Widget) event.getSource());
	}

	
	/**
	 * Called when clicking on the open button beside the report title
	 * @param event object containing information about the click event
	 */
	@UiHandler("lnkOpen")
	public void lnkOpen(ClickEvent event) {
		openReportList(false);
	}


	/**
	 * Save report
	 * @param event contain information about the click event
	 */
	@UiHandler("lnkSave")
	public void lnkSaveClick(ClickEvent event) {
		if (isNewReport()) {
			SaveDlg.openDialog(report, this);
		}
		else {
            saveReport();
		}
	}


	/**
	 * Called when the user clicks on the generate report button
	 * @param clickEvent
	 */
	@UiHandler("btnGenerate")
	public void btnGenerateClick(ClickEvent clickEvent) {
        showMessage("Not implemented yet", MessagePanel.MessageType.INFO);
/*
		CIndicatorRequest data = prepareReportRequest();
		if (data == null)
			return;
		
		tblResult.setVisible(false);
		chart.clear();

		btnGenerate.setEnabled(false);
        pnlMessage.setVisible(false);

        service.executeIndicator(data, new StandardCallback<CIndicatorResponse>() {
            @Override
            public void onSuccess(CIndicatorResponse res) {
                btnGenerate.setEnabled(true);
                // did the server sent any data ?
                if (res == null) {
                    showErrorMessage(App.messages.noResultFound());
                    return;
                }
                // there was an error message?
                if (res.getErrorMessage() != null) {
                    showErrorMessage(res.getErrorMessage());
                } else {
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
*/
	}


    /**
     * Show a message in the report panel
     * @param msg the message to be displayed
     * @param type message type - ERROR, WARN or INFO
     */
    public void showMessage(String msg, MessagePanel.MessageType type) {
        pnlMessage.setVisible(true);
        pnlMessage.setText(msg);
        pnlMessage.setType(type);
    }
	
	/**
	 * Prepare the data of the report (variables and filters) to be sent to the server
	 * @return instance of {@link org.msh.tb.client.shared.model.CIndicatorRequest} or null if there are validation errors
	 */
/*
	public CIndicatorRequest prepareReportRequest() {
		tableData.setColVariables( pnlColVariables.getVariables() );
		tableData.setRowVariables( pnlRowVariables.getVariables() );

		// mount list of variables
		ArrayList<String> rows = pnlRowVariables.getDeclaredVariables();
		ArrayList<String> cols = pnlColVariables.getDeclaredVariables();

		CIndicatorRequest data = new CIndicatorRequest();
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
*/


	/**
	 * Show popup window with the list of patients of the clicked cell
	 * @param c
	 * @param r
	 */
/*
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
*/

	
	/**
	 * Select the chart from a number of 0 to the max number of charttypes array 
	 * @param chartType identify the type of chart to display
	 */
/*
	public void selectChart(CChartType chartType) {
		Image img = new Image(chartImgs[chartType.ordinal()]);
		Element el = (Element)lnkChartType.getElement();
		if (DOM.getChildCount(el) > 0)
			el.removeChild(DOM.getFirstChild(el));
		DOM.insertChild(el, img.getElement(), 0);

		chart.setSelectedChart(chartType);
		updateChart();
	}
*/

	
	/**
	 * Return the selected chart type
	 * @return
	 */
/*
	public CChartType getChartType() {
		return chart.getSelectedChart();
	}
*/

	/**
	 * @param selectedCol the selectedCol to set
	 */
/*
	public void setSelectedCol(int selectedCol) {
		tableData.setSelection(TableSelection.COLUMN);
		tableData.setSelectedCell(selectedCol);
		updateChart();
	}
*/


	/**
	 * @param selectedRow the selectedRow to set
	 */
/*
	public void setSelectedRow(int selectedRow) {
		tableData.setSelection(TableSelection.ROW);
		tableData.setSelectedCell(selectedRow);
		updateChart();
	}
*/


	/**
	 * @return the service
	 */
/*
	public ReportServiceAsync getService() {
		return service;
	}
*/


	/**
	 * Return information about the report sent from the server
	 * @return instance of {@link org.msh.tb.client.shared.model.CReportUIData} class
	 */
/*
	public CReportUIData getReportUI() {
		return reportUI;
	}
*/


	/**
	 * @return the report
	 */
	public CReport getReport() {
		return report;
	}


    /**
     * Update the report title
     */
    public void updateTitle() {
        txtTitle.setText(report.getTitle());
    }


	/**
	 * Update the current report being displayed by the system
	 */
	public void updateReport() {
/*
        updateTitle();
		pnlColVariables.update(report.getColumnVariables());
		pnlRowVariables.update(report.getRowVariables());
		pnlFilters.update(report.getFilters());

		// update chart type
		Integer val = report.getChartType();
		if ((val != null) && (val >= 0) && (val < CChartType.values().length)) {
			selectChart(CChartType.values()[val]);
		}

		// hide the current report being displayed
		tblResult.setVisible(false);
		chart.clear();
*/
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
		pnlReportList.show(reload, this);
		pnlReport.setVisible(false);
	}
	
	
	/**
	 * Create a new report
	 */
	public void newReport() {
		report = new CReport();
		report.setTitle("Report title (click here to change)");
        report.setIndicators(new ArrayList<CIndicator>());

        updateTitle();
        pnlIndicators.clear();

        newIndicator();

		closeReportList();
	}

    /**
     * Add a new indicator to the report
     */
    public void newIndicator() {
        CIndicator indicator = new CIndicator();

        // create a new name to the indicator
        int index = 1;
        while (true) {
            String s = "Indicator " + Integer.toString(index);
            boolean newName = true;
            for (CIndicator ind: report.getIndicators()) {
                if (s.equals(ind.getTitle())) {
                    newName = false;
                    break;
                }
            }
            if (newName) {
                indicator.setTitle(s);
                break;
            }
            index++;
        }

        indicator.setChartType(CChartType.CHART_BAR);
        report.getIndicators().add(indicator);

        IndicatorController controller = new IndicatorController(report, indicator, null);
        // display the indicator
        addIndicatorPanel(controller, true, pnlIndicators.getWidgetCount());
    }


    /**
     * Add a new indicator panel to the list of indicators
     * @param controller the indicator controller to be displayed in the indicator panel list
     * @param editingMode if true, indicator will be displayed in edit mode, otherwise will be displayed in view mode
     */
    public void addIndicatorPanel(IndicatorController controller, boolean editingMode, int index) {
        IndicatorWrapper pnlIndicator;
        if (editingMode) {
            pnlIndicator = new IndicatorEditor();
        }
        else {
            pnlIndicator = new IndicatorView();
        }
        pnlIndicator.setEventHandler(this);
        pnlIndicators.insert(pnlIndicator, index);
        pnlIndicator.update(controller);
    }
	
	/**
	 * Open an existing report
	 * @param id is the report identification
	 */
	public void openReport(Integer id) {
		closeReportList();

        AppResources.reportServices().loadReport(id, new StandardCallback<CReport>() {
            @Override
            public void onSuccess(CReport result) {
                report = result;
                displayLoadedReport();
            }
        });
	}

    /**
     * Display the report that was just loaded from the server
     */
    public void displayLoadedReport() {
        updateTitle();
        pnlIndicators.clear();
        if (report.getIndicators() != null) {
            for (CIndicator ind: report.getIndicators()) {
                addIndicatorPanel(new IndicatorController(report, ind, null), false, pnlIndicators.getWidgetCount());
            }
        }
        // run the report
        btnGenerate.click();
    }


	/**
	 * Delete the current report
	 */
	public void deleteReport() {
		
	}


	/**
	 * @return the tableData
	 */
/*
	public TableData getTableData() {
		return tableData;
	}
*/

    @Override
    public void handleEvent(Object eventType, Object data) {
        if (eventType == ReportListPanel.ReportListEvent.CLOSE) {
            closeReportList();
            return;
        }

        if (eventType == ReportListPanel.ReportListEvent.OPEN) {
            openReport((Integer)data);
            return;
        }

        if (eventType == ReportListPanel.ReportListEvent.NEW_REPORT) {
            newReport();
            return;
        }

        if (eventType == IndicatorWrapper.IndicatorEvent.CLOSE) {
            closeEditor((IndicatorEditor)data);
            return;
        }

        if (eventType == IndicatorWrapper.IndicatorEvent.EDIT) {
            editIndicator((IndicatorView)data);
            return;
        }

        if (eventType == IndicatorWrapper.IndicatorEvent.REMOVE) {
            removeIndicator((IndicatorWrapper)data);
            return;
        }

        if (eventType == SaveDlg.SaveDlgEvent.SAVE) {
            saveReport();
            return;
        }

        if (eventType == SaveDlg.SaveDlgEvent.SAVEAS) {
            saveReportAs();
            return;
        }

        if (eventType == SaveDlg.SaveDlgEvent.CANCEL) {
            return;
        }

        Window.alert("Not handled: " + eventType + " = " + data);
    }

    /**
     * Close the editor and displays the indicator view in its place
     * @param editor the instance of {@link org.msh.tb.client.indicators.IndicatorEditor} to close
     */
    protected void closeEditor(IndicatorEditor editor) {
        int index = pnlIndicators.getWidgetIndex(editor);
        pnlIndicators.remove(index);
        addIndicatorPanel(editor.getController(), false, index);
    }

    /**
     * Open the indicator editor of the given indicator being displayed
     * @param view the indicator view
     */
    protected void editIndicator(IndicatorView view) {
        int index = pnlIndicators.getWidgetIndex(view);
        pnlIndicators.remove(index);
        addIndicatorPanel(view.getController(), true, index);
    }

    /**
     * Remove an indicator from the report passing its indicator panel as argument
     * @param pnlInd the panel containing the indicator
     */
    protected void removeIndicator(IndicatorWrapper pnlInd) {
        pnlIndicators.remove(pnlInd);
        report.getIndicators().remove(pnlInd.getController().getIndicator());
    }

    /**
     * Save the report
     */
    protected void saveReport() {
        AppResources.reportServices().saveReport(report, new StandardCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                showMessage("Report was successfully saved", MessagePanel.MessageType.INFO);
            }
        });
    }


    /**
     * Clone the existing report with a different name
     */
    protected void saveReportAs() {
        report.setId(null);
        saveReport();
    }
}
