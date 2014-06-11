package org.msh.tb.client.reports;

import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CVariable;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Popup window that displays a list of variables by group to be selected
 * by the user
 * @author Ricardo Memoria
 *
 */
public class GroupVariablesPopup extends GroupPopup {

	private static GroupVariablesPopup myinstance;
	
	/**
	 * Default constructor
	 * @param eventHandler
	 */
	protected GroupVariablesPopup() {
		super(null);
		setGroups(MainPage.instance().getGroups());
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

	@Override
	protected boolean hasItems(CGroup group) {
		return (group.getVariables() != null && group.getVariables().length > 0);
	}

	
	/**
	 * Return the singleton instance of the class
	 * @return instance of {@link GroupVariablesPopup}
	 */
	public static GroupVariablesPopup instance() {
		if (myinstance == null) {
			myinstance = new GroupVariablesPopup();
		}
		return myinstance;
	}
}
