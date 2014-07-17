/**
 * 
 */
package org.msh.tb.client.reports.variables;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.reports.GroupPopup;
import org.msh.tb.client.reports.ReportUtils;
import org.msh.tb.client.shared.model.CVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel that displays variables vertically
 *  
 * @author Ricardo Memoria
 *
 */
public class VariablesPanel extends Composite implements StandardEventHandler {
    interface IndicatorEditorUiBinder extends UiBinder<HTMLPanel, VariablesPanel> {   }
    private static IndicatorEditorUiBinder binder = GWT.create(IndicatorEditorUiBinder.class);

	@UiField VerticalPanel pnlColVariables;
    @UiField VerticalPanel pnlRowVariables;

	private ArrayList<String> varsColumn = new ArrayList<String>();
    private ArrayList<String> varsRow = new ArrayList<String>();

	//  used internally to know which box called the popup
	private VariableBox selected;

    private StandardEventHandler eventHandler;


	/**
	 * Default constructor
	 */
	public VariablesPanel() {
        initWidget(binder.createAndBindUi(this));
	}


    /**
     * Set the column variables to be displayed in the panel
     * @param colVars list o variable IDs
     */
    public void setColumnVariables(ArrayList<String> colVars) {
        varsColumn.clear();
        updateVariables(colVars, varsColumn, pnlColVariables);
    }


    /**
     * Set the row variables to be displayed in the panel
     * @param rowVars list of variable IDs
     */
    public void setRowVariables(ArrayList<String> rowVars) {
        varsRow.clear();
        updateVariables(rowVars, varsRow, pnlRowVariables);
    }


    /**
     * Return the list of variables of the table row
     * @return list of variables ID
     */
    public ArrayList<String> getRowVariables() {
        if (varsRow.size() == 0) {
            return null;
        }
        return varsRow;
    }

    /**
     * Return the list of variables for the table column
     * @return list of variables ID
     */
    public ArrayList<String> getColumnVariables() {
        if (varsColumn.size() == 0) {
            return null;
        }
        return varsColumn;
    }

	
	/**
	 * Initialize the variables panel
	 * @param varsId list of variable IDs
     * @param target is the list that will receive the new variables
     * @param pnlVariables vertical panel that will display the variables
	 */
	protected void updateVariables(ArrayList<String> varsId, ArrayList<String> target, VerticalPanel pnlVariables) {
		pnlVariables.clear();

		if (varsId != null) {
			for (String varid: varsId) {
				CVariable var = ReportUtils.findVariableById(varid);
				if (var != null) {
					VariableBox box = new VariableBox(this, true);
					box.setVariable(var);
					pnlVariables.add(box);
                    target.add(varid);
				}
			}
		}
		pnlVariables.add( new VariableBox(this, false) );
	}
	

	/** {@inheritDoc}
	 */
	@Override
	public void handleEvent(Object eventType, Object data) {
		// called when a variable must be deleted
		if (VariableBox.VARIABLE_DELETE.equals(eventType)) {
			removeVariableBox((VariableBox)data);
			return;
		}
		
		// called when a variable changes
		if (GroupPopup.ITEM_SELECTED.equals(eventType)) {
			selectedVariableChanged((CVariable)data);
		}
	}


	/**
	 * @param variable the new variable that was selected by the user
	 */
	private void selectedVariableChanged(CVariable variable) {
		if (selected == null) {
			return;
		}

        VerticalPanel pnlVariables = (VerticalPanel)selected.getParent();
        ArrayList<String> variables = pnlVariables == pnlColVariables? varsColumn: varsRow;

        // new variable included ?
		if (selected.getVariable() == null) {
			pnlVariables.add(new VariableBox(this, false));
			variables.add(variable.getId());
		}
		
		selected.setVariable(variable);
		selected.setRemoveEnabled(true);
		int index = pnlVariables.getWidgetIndex(selected);
		variables.set(index, variable.getId());
		
		// clean up
		selected = null;
		GroupVariablesPopup.instance().hide();

        fireChangeEvent();
	}


	/**
	 * Remove a variable box and its corresponding variable from the list
	 * @param box instance of {@link VariableBox}
	 */
	protected void removeVariableBox(VariableBox box) {
        VerticalPanel pnl = (VerticalPanel)box.getParent();
        ArrayList<String> variables = pnl == pnlColVariables? varsColumn: varsRow;

		int index = pnl.getWidgetIndex(box);
		if (index < variables.size()) {
			variables.remove(index);
		}
		pnl.remove(index);
        fireChangeEvent();
	}

    /**
     * Fire event about changes in the variable panel
     */
    protected void fireChangeEvent() {
        if (eventHandler != null) {
            eventHandler.handleEvent(getClass(), null);
        }
    }


	/**
	 * @return the selected
	 */
	public VariableBox getSelected() {
		return selected;
	}


	/**
	 * @param selected the selected to set
	 */
	public void setSelected(VariableBox selected) {
		this.selected = selected;
	}

    public StandardEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(StandardEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}
