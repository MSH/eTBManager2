package org.msh.tb.client.indicators;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.CaseListPopup;
import org.msh.tb.client.shared.model.CTableColumn;
import org.msh.tb.client.shared.model.CTableRow;
import org.msh.tb.client.shared.model.CVariable;
import org.msh.tb.client.tableview.Cell;
import org.msh.tb.client.tableview.TableData;
import org.msh.tb.client.tableview.TableView;

import java.util.HashMap;

/**
 * Panel wrapper that displays the indicator and, when hovering, displays a
 * remove and edit button over the indicator panel
 * Created by ricardo on 10/07/14.
 */
public class IndicatorView extends IndicatorWrapperPanel {
    interface IndicatorViewUiBinder extends UiBinder<FocusPanel, IndicatorView> {
    }
    private static IndicatorViewUiBinder ourUiBinder = GWT.create(IndicatorViewUiBinder.class);

    @UiField ResultView resIndicator;
    @UiField HorizontalPanel pnlButtons;


    /**
     * Default constructor
     */
    public IndicatorView() {
        final FocusPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);

        pnlButtons.setVisible(false);

        // event handler
        rootElement.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                mouseIn();
            }
        });

        rootElement.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                mouseOut();
            }
        });

        // handle cell click
        resIndicator.setEventHandler(new StandardEventHandler() {
            @Override
            public void handleEvent(Object eventType, Object data) {
                handleResultTableEvent(eventType, data);
            }
        });
    }

    /**
     * Handle table events
     * @param eventType the type of event
     * @param data the data related to the event
     */
    private void handleResultTableEvent(Object eventType, Object data) {
        if (eventType == TableView.Event.CELL_CLICK) {
            showPatients((Cell)data);
        }
    }

    /**
     * Show the list of patients
     * @param cell
     */
    private void showPatients(Cell cell) {
        // get variables from the row
        int index = cell.getRow();

        TableData tbl = getController().getData();
        HashMap<String, String> filters = getController().createRequest().getFilters();
        if (filters == null) {
            filters = new HashMap<String, String>();
        }
        HashMap<String, String> tmpmap = new HashMap<String, String>();

        if (tbl.getRowVariables() != null) {
            int level = tbl.getTable().getRows().get(index).getLevel();

            // get key values from rows
            while (index >= 0) {
                CTableRow row = tbl.getTable().getRows().get(index);
                if (row.getLevel() == level) {
                    CVariable var = tbl.getRowVariables().get(row.getVarIndex());
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
        }

        if (tbl.getColVariables() != null) {
            // get key values from columns
            CTableColumn col = tbl.getHeaderColumns().get(cell.getColumn());
            tmpmap.clear();
            while (col != null) {
                int level = col.getLevel();
                int n = tbl.getTable().getColVarIndex()[level];
                CVariable var = tbl.getColVariables().get(n);
                if (!tmpmap.containsKey(var.getId())) {
                    tmpmap.put(var.getId(), col.getKey());
                }
                col = col.getParent();
            }
            filters.putAll(tmpmap);
        }

        // show popup window
        CaseListPopup.instance().showPatients(filters);
    }


    /**
     * Update the content of the indicator view with the given indicator
     */
    @Override
    public void updateIndicator(AsyncCallback<ResultView> callback) {
        resIndicator.update(getController(), callback);
    }


    /**
     * Called when mouse is out of the panel
     */
    protected void mouseOut() {
        pnlButtons.setVisible(false);
    }

    /**
     * Called when mouse is over the panel
     */
    protected void mouseIn() {
        pnlButtons.setVisible(true);
    }

    /**
     * Called when the user tries to remove this indicator from the list of indicators
     * @param evt click event
     */
    @UiHandler("btnRemove")
    public void btnRemoveClick(ClickEvent evt) {
        fireIndicatorEvent(IndicatorEvent.REMOVE);
    }

    /**
     * Called when the user clicks on the edit like
     * @param evt click event
     */
    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent evt) {
        fireIndicatorEvent(IndicatorWrapperPanel.IndicatorEvent.EDIT);
    }
}