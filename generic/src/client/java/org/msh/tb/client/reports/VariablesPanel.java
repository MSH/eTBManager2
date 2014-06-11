/**
 * 
 */
package org.msh.tb.client.reports;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CVariable;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Panel that displays variables vertically
 *  
 * @author Ricardo Memoria
 *
 */
public class VariablesPanel extends Composite implements StandardEventHandler {

	private VerticalPanel pnlVariables;
	private List<CVariable> variables = new ArrayList<CVariable>();
	//  used just to know which box called the popup
	private VariableBox selected;
	
	/**
	 * Default constructor
	 */
	public VariablesPanel() {
		pnlVariables = new VerticalPanel();
		initWidget(pnlVariables);
	}

	
	/**
	 * Initialize the variables panel
	 * @param varsId list of variable IDs
	 */
	public void update(ArrayList<String> varsId) {
		pnlVariables.clear();
		variables.clear();

		if (varsId != null) {
			for (String varid: varsId) {
				CVariable var = MainPage.instance().findVariableById(varid);
				if (var != null) {
					VariableBox box = new VariableBox(this, true);
					box.setVariable(var);
					pnlVariables.add(box);
					variables.add(var);
				}
			}
		}
		pnlVariables.add( new VariableBox(this, false) );
	}
	

	/**
	 * Return the number of declared variables
	 * @return
	 */
	public int getVariableCount() {
		return variables.size();
	}


	/**
	 * Return the current variable at the given position
	 * @param index 0-based index position
	 * @return instance of {@link CVariable}
	 */
	public CVariable getVariable(int index) {
		return ((VariableBox)pnlVariables.getWidget(index)).getVariable();
	}


	/**
	 * Return the list of variables in this panel
	 * @return
	 */
	public List<CVariable> getVariables() {
		return variables;
	}
	
	
	/**
	 * 
	 * Return the list of variable IDs of the variables declared in the panel
	 * @return List of string
	 */
	public ArrayList<String> getDeclaredVariables() {
		ArrayList<String> varNames = new ArrayList<String>();

		for (CVariable var: variables) {
			varNames.add(var.getId());
		}
		return varNames;
	}


	/** {@inheritDoc}
	 */
	@Override
	public void eventHandler(Object eventType, Object data) {
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
	 * @param data
	 */
	private void selectedVariableChanged(CVariable variable) {
		if (selected == null) {
			return;
		}
		
		if (selected.getVariable() == null) {
			pnlVariables.add(new VariableBox(this, false));
			variables.add(variable);
		}
		
		selected.setVariable(variable);
		selected.setRemoveEnabled(true);
		int index = pnlVariables.getWidgetIndex(selected);
		variables.set(index, variable);
		
		// clean up
		selected = null;
		GroupVariablesPopup.instance().hide();
	}


	/**
	 * Remove a variable box and its corresponding variable from the list
	 * @param box instance of {@link VariableBox}
	 */
	protected void removeVariableBox(VariableBox box) {
		int index = pnlVariables.getWidgetIndex(box);
		if (index < variables.size()) {
			variables.remove(index);
		}
		pnlVariables.remove(index);
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
}
