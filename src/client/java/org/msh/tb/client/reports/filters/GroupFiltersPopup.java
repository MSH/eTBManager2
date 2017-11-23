package org.msh.tb.client.reports.filters;

import com.google.gwt.user.client.ui.VerticalPanel;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.reports.GroupPopup;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CGroup;

/**
 * Popup window that displays a list of filters to be selected by the user
 * @author Ricardo Memoria
 *
 */
public class GroupFiltersPopup extends GroupPopup {

    private static final String KEY_FILTERSPOPUP = "report.filters";

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


    public static GroupFiltersPopup instance() {
        GroupFiltersPopup popup = (GroupFiltersPopup)AppResources.instance().get(KEY_FILTERSPOPUP);

        if (popup == null) {
            popup = new GroupFiltersPopup();
            AppResources.instance().set(KEY_FILTERSPOPUP, popup);
        }
        return popup;
    }
}
