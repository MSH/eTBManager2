package org.msh.tb.client.reports;

import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CVariable;

import com.google.gwt.user.client.ui.VerticalPanel;

public class GroupVariablesPopup extends GroupPopup {

	public GroupVariablesPopup(StandardEventHandler eventHandler) {
		super(eventHandler);
	}

	/**
	 * Mount the list of options for the given group
	 * @param grp
	 * @param pnlOptions
	 */
	protected void initializeOptions(CGroup grp, VerticalPanel pnlOptions) {
		if (grp.getVariables() == null)
			return;

		for (CVariable var: grp.getVariables()) {
			addItem(pnlOptions, var.getName(), var);
		}
	}
	
}
