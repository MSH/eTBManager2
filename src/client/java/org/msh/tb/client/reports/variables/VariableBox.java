package org.msh.tb.client.reports.variables;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.msh.tb.client.reports.HoverPanel;
import org.msh.tb.client.shared.model.CVariable;

/**
 * Display a variable in a rectangle border
 * @author Ricardo Memoria
 *
 */
public class VariableBox extends HoverPanel {

	public static final Integer VARIABLE_CHANGE = 1;
	public static final Integer VARIABLE_DELETE = 2;
	
	private FlowPanel option;
	private Label lblVariable;
	private CVariable variable;
	private VariablesPanel variablesPanel;
	
	/**
	 * Constructor
	 * @param variablesPanel
     * @param removeEnabled
	 */
	public VariableBox(VariablesPanel variablesPanel, boolean removeEnabled) {
		super();

		this.variablesPanel = variablesPanel;
		
		// check if remove button is visible
		setRemoveEnabled(removeEnabled);
		
		addStyleName("var-panel");

		// create box to display option
		option = new FlowPanel();
		option.setStyleName("var-name");

		// add button to display the options
		Anchor lnk = new Anchor(SafeHtmlUtils.fromTrustedString("<div class='caret-down'></div>"));
		lnk.setStyleName("option-button");
		lnk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				optionClickHandler(event);
			}
		});
		option.add(lnk);

		// display the variable name
		lblVariable = new Label("-");
		option.add(lblVariable);

		add(option);
		
		addDomHandler(this, MouseOverEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
	}

	/**
	 * Called when user clicks on the box to change the option
	 * @param event
	 */
	protected void optionClickHandler(ClickEvent event) {
		GroupVariablesPopup popup = GroupVariablesPopup.instance();
		popup.setEventHandler(variablesPanel);
		variablesPanel.setSelected(this);
		popup.showPopup(((Widget)event.getSource()).getParent());
	}

	/**
	 * Called when user wants to remove a variable
	 */
	@Override
	protected void removePanel() {
		variablesPanel.removeVariableBox(this);
	}

	/**
	 * Called when a variable is selected
	 * @param item
	 */
	protected void selectVariable(CVariable item) {
		this.variable = item;
		lblVariable.setText(item.getName());
	}

	/**
	 * Return the id of the variable selected
	 * @return
	 */
	public CVariable getVariable() {
		return variable;
	}

	/**
	 * @param variable the variable to set
	 */
	public void setVariable(CVariable variable) {
		this.variable = variable;
		lblVariable.setText(variable != null? variable.getName(): "");
	}


    /** {@inheritDoc}
	 */
/*	@Override
	public void eventHandler(Object eventType, Object data) {
		if (GroupPopup.ITEM_SELECTED.equals(eventType)) {
			selectVariable((CVariable)data);
			GroupVariablesPopup.instance().hide();
			MainPage.instance().variableChanged(this);
		}
	}
*/	
}
