/**
 * 
 */
package org.msh.tb.client.reports.filters;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CItem;

import java.util.ArrayList;

/**
 * Filter widget to support TB unit selection
 * 
 * @author Ricardo Memoria
 *
 */
public class TbunitFilter extends FilterWidget {

	private VerticalPanel panel;
	private ListBox lbAdminUnits;
	private ListBox lbUnits;
	
	/**
	 * Called when an administrative unit changes
	 */
	private ChangeHandler adminUnitSelHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent event) {
			adminUnitChanged(null);
		}
	};
	
	
	/**
	 * Default constructor
	 */
	public TbunitFilter() {
		panel = new VerticalPanel();

		lbAdminUnits = new ListBox();
		lbAdminUnits.setVisibleItemCount(1);
		lbAdminUnits.setWidth("300px");

		panel.add(lbAdminUnits);
		
		initWidget(panel);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.filters.FilterWidget#initialize(org.msh.tb.client.shared.model.CFilter)
	 */
	@Override
	public void initialize(CFilter filter, String value) {
		super.initialize(filter, value);

		final String fval = value;

        loadServerOptions(null, new StandardCallback<ArrayList<CItem>>() {
            @Override
            public void onSuccess(ArrayList<CItem> result) {
                if (result != null) {
                    fillListOptions(lbAdminUnits, result, null);
                    lbAdminUnits.addChangeHandler(adminUnitSelHandler);

                    setValue(fval);
                }
            }
        });
	}	

	
	/**
	 * Return the ID of the selected administrative unit
	 * @return String value
	 */
	public String getSelectedAUID() {
		int index = lbAdminUnits.getSelectedIndex();
		if (index <= 0) {
			return null;
		}
		
		return lbAdminUnits.getValue(index);
	}
	
	/**
	 * Return the ID of the selected unit
	 * @return String value, or null if no unit is selected
	 */
	public String getSelectedUnitID() {
		int index = lbUnits.getSelectedIndex();
		return index > 0? lbUnits.getValue(index): null;
	}


	/**
	 * Called when the selection in the list box with administrative units is changed
	 */
	protected void adminUnitChanged(String unit) {
		if (lbUnits == null) {
			lbUnits = new ListBox();
			lbUnits.setVisibleItemCount(1);
			lbUnits.setWidth("300px");
            lbUnits.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    notifyFilterChange();
                }
            });
			
			panel.add(lbUnits);
		}

		// clear before anything, just to not display incompatible values with selection
		lbUnits.clear();
		// get selected admin unit ID
		String auid = getSelectedAUID();

        lbUnits.setVisible(auid != null);

		if (auid != null) {
			// update TB unit list
            final String fu = unit;
			loadServerOptions(auid, new StandardCallback<ArrayList<CItem>>() {
				@Override
				public void onSuccess(ArrayList<CItem> result) {
					if (result != null) {
						fillListOptions(lbUnits, result, fu);
					}
				}
			});
		}
        notifyFilterChange();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void setValue(String value) {
        if (value == null || value.isEmpty()) {
            lbAdminUnits.setSelectedIndex(0);
            adminUnitChanged(null);
            return;
        }

        String[] vals = value.split(",");
        String au, unit;
        if (vals.length > 1) {
            au = vals[0];
            unit = vals[1];
        }
        else {
            au = value;
            unit = null;
        }

        selectItemByValue(lbAdminUnits, au);
        adminUnitChanged(unit);
	}

	/** {@inheritDoc}
	 */
	@Override
	public String getValue() {
		String auid = getSelectedAUID();
		String unid = getSelectedUnitID();
		
		return (auid != null? auid: "") + "," + (unid != null? unid: "");
	}

}
