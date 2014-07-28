package org.msh.tb.client.reports;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.AppModule;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.indicators.*;
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
    @UiField IndicatorsPanel pnlIndicators;
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
                report.setTitle(txtTitle.getText());
                updateTitle();
            }
        });

        popupOptions = new OptionsPopup();
        popupOptions.setEventHandler(this);
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
     * Called when the user wants to include a new indicator
     * @param event instance of the click event
     */
    @UiHandler("lnkAddIndicator")
    public void lnkAddIndicator(ClickEvent event) {
        newIndicator();
    }

	/**
	 * Called when user clicks on the options button
	 * @param event instance of the click event
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
			SaveDlg.openDialog(report, false, this);
		}
		else {
            saveReport();
		}
	}

    /**
     * Open the dialog to make a clone of the current report
     */
    public void openSaveAsDialog() {
        SaveDlg.openDialog(report, true, this);
    }


	/**
	 * Called when the user clicks on the generate report button
	 * @param clickEvent contain information about the click event
	 */
	@UiHandler("btnGenerate")
	public void btnGenerateClick(ClickEvent clickEvent) {
        pnlMessage.setVisible(false);
        updateIndicatorPanel(0);
	}

    /**
     * Update the indicator panels recursively and asynchronously
     * @param index the index of the panel in the list of panels
     */
    private void updateIndicatorPanel(final int index) {
        // index is higher than the number of panels ?
        if (index >= pnlIndicators.getIndicatorsCount()) {
            showMessage("Report was successfully updated", MessagePanel.MessageType.INFO);
            return;
        }

        final IndicatorWrapperPanel pnl = pnlIndicators.getIndicatorPanel(index);
        // clear the response in order to force an update
        pnl.getController().clearData();

        pnl.update(new StandardCallback<ResultView>() {
            @Override
            public void onSuccess(ResultView result) {
                updateIndicatorPanel(index + 1);
            }
        });
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
        pnlMessage.setVisible(false);
		report = new CReport();
		report.setTitle("Report title (click here to change)");
        report.setIndicators(new ArrayList<CIndicator>());
        pnlGlobalFilters.setFilters(null);

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
        addIndicatorPanel(controller, true, -1, null);
    }


    /**
     * Add a new indicator panel to the list of indicators
     * @param controller the indicator controller to be displayed in the indicator panel list
     * @param editingMode if true, indicator will be displayed in edit mode, otherwise will be displayed in view mode
     */
    public void addIndicatorPanel(IndicatorController controller, boolean editingMode, int index, AsyncCallback callback) {
        IndicatorWrapperPanel pnlIndicator;
        if (editingMode) {
            pnlIndicator = new IndicatorEditor();
        }
        else {
            pnlIndicator = new IndicatorView();
        }
        pnlIndicator.setEventHandler(this);
        if (index == -1) {
            index = pnlIndicators.getIndicatorsCount();
        }
        pnlIndicator.setController(controller);
        pnlIndicators.insert(pnlIndicator, index);
        pnlIndicator.update(callback);
    }
	
	/**
	 * Open an existing report
	 * @param id is the report identification
	 */
	public void openReport(Integer id) {
		closeReportList();
        pnlIndicators.clear();
        pnlGlobalFilters.setFilters(null);
        txtTitle.setText("");
        pnlMessage.setVisible(false);

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
        pnlGlobalFilters.setFilters(report.getFilters());
        updateTitle();
        pnlIndicators.clear();

        addIndicators(0, new StandardCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
//                pnlIndicators.updateIndicatorsStyles();
//                showMessage("Report successfully loaded", MessagePanel.MessageType.INFO);
            }
        });
    }


    /**
     * Add an indicator to the panel and update it recursively
     * @param index index of the indicator
     * @param callback callkack function to be called when it's finished
     */
    private void addIndicators(final int index, final AsyncCallback<Void> callback) {
        if ((report.getIndicators() == null) || (index >= report.getIndicators().size())) {
            callback.onSuccess(null);
            return;
        }

        CIndicator indicator = report.getIndicators().get(index);
        addIndicatorPanel(new IndicatorController(report, indicator, null), false, -1, new StandardCallback() {
            @Override
            public void onSuccess(Object result) {
                addIndicators(index + 1, callback);
            }
        });
    }


	/**
	 * Delete the current report
	 */
	public void deleteReport() {
		if (!Window.confirm(AppResources.messages().deleteReport())) {
            return;
        }

        AppResources.reportServices().deleteReport(report.getId(), new StandardCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showMessage("Report was deleted", MessagePanel.MessageType.INFO);
            }
        });
	}


    @Override
    public void handleEvent(Object eventType, Object data) {
        if (eventType == ReportListPanel.ReportListEvent.CLOSE) {
            closeReportList();
            return;
        }

        // open a report
        if (eventType == ReportListPanel.ReportListEvent.OPEN) {
            openReport((Integer)data);
            return;
        }

        // new report
        if ((eventType == ReportListPanel.ReportListEvent.NEW_REPORT) || (eventType == OptionsPopup.Event.NEWREPORT)) {
            newReport();
            return;
        }

        // add a new indicator
        if (eventType == OptionsPopup.Event.ADDINDICATOR) {
            newIndicator();
            return;
        }

        // close the editor panel
        if (eventType == IndicatorWrapperPanel.IndicatorEvent.CLOSE) {
            closeEditor((IndicatorEditor)data);
            return;
        }

        // open the editor panel
        if (eventType == IndicatorWrapperPanel.IndicatorEvent.EDIT) {
            editIndicator((IndicatorView)data);
            return;
        }

        // remove an indicator
        if (eventType == IndicatorWrapperPanel.IndicatorEvent.REMOVE) {
            removeIndicator((IndicatorWrapperPanel)data);
            return;
        }

        // user clicked on the delete option in popup menu
        if (eventType == OptionsPopup.Event.DELETE) {
            deleteReport();
            return;
        }

        // user request report to be saved
        if (eventType == OptionsPopup.Event.SAVE) {
            lnkSaveClick(null);
            return;
        }

        // uer selected the save as command
        if (eventType == OptionsPopup.Event.SAVEAS) {
            openSaveAsDialog();
            return;
        }

        // open the option dialog
        if (eventType == OptionsPopup.Event.SETTINGS) {
            openOptionsDlg();
            return;
        }

        // called from the save dialog when user confirm the opeartion
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

        // options dialog
        if (eventType == OptionsDlg.Event.OK) {
            lnkSaveClick(null);
            return;
        }

        // options dialog
        if (eventType == OptionsDlg.Event.CANCEL) {
            return;
        }

        Window.alert("Not handled: " + eventType + " = " + data);
    }

    /**
     * Close the editor and displays the indicator view in its place
     * @param editor the instance of {@link org.msh.tb.client.indicators.IndicatorEditor} to close
     */
    protected void closeEditor(IndicatorEditor editor) {
        int index = pnlIndicators.getIndicatorIndex(editor);
        pnlIndicators.remove(index);
        addIndicatorPanel(editor.getController(), false, index, null);
    }

    /**
     * Open the indicator editor of the given indicator being displayed
     * @param view the indicator view
     */
    protected void editIndicator(IndicatorView view) {
        int index = pnlIndicators.getIndicatorIndex(view);
        pnlIndicators.remove(index);
        addIndicatorPanel(view.getController(), true, index, null);
    }

    /**
     * Remove an indicator from the report passing its indicator panel as argument
     * @param pnlInd the panel containing the indicator
     */
    protected void removeIndicator(IndicatorWrapperPanel pnlInd) {
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
                report.setId(result);
                updateTitle();
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


    /**
     * Open options dialog window
     */
    protected void openOptionsDlg() {
        OptionsDlg.open(report, this);
    }

}
