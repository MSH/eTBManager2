package org.msh.tb.client.reports.filters;

import java.util.ArrayList;

import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CItem;


public class RemoteOptionsFilter extends OptionsFilter{

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
				if (result != null)
					fillOptions(result, fval);
			}
		});
	}	

}
