package org.msh.tb.client.reports.variables;

import com.google.gwt.user.client.ui.VerticalPanel;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.reports.GroupPopup;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.shared.model.CVariable;

/**
 * Popup window that displays a list of variables by group to be selected
 * by the user
 * @author Ricardo Memoria
 *
 */
public class GroupVariablesPopup extends GroupPopup {

    private static final String KEY_VARIABLESPOPUP = "report.ui.variablePopup";

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
        GroupVariablesPopup popup = (GroupVariablesPopup)AppResources.instance().get(KEY_VARIABLESPOPUP);
        if (popup == null) {
            popup = new GroupVariablesPopup();
            AppResources.instance().set(KEY_VARIABLESPOPUP, popup);
        }
        return popup;
	}
}
