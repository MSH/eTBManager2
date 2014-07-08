package org.msh.tb.client.reports;

import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CGroup;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Popup window that displays a list of filters to be selected by the user
 * @author Ricardo Memoria
 *
 */
public class GroupFiltersPopup extends GroupPopup {

	public GroupFiltersPopup(StandardEventHandler eventHandler) {
		super(eventHandler);
	}

	/**
	 * Mount the list of options for the given group
	 * @param grp
	 * @param pnlOptions
	 */
	protected void initializeOptions(CGroup grp, VerticalPanel pnlOptions) {
		if (grp.getFilters() == null)
			return;

		for (CFilter filter: grp.getFilters()) {
			addItem(pnlOptions, filter.getName(), filter);
		}
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.GroupPopup#hasItems(org.msh.tb.client.shared.model.CGroup)
	 */
	@Override
	protected boolean hasItems(CGroup group) {
		return (group.getFilters() != null && group.getFilters().length > 0);
	}
	
}
