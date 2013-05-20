package org.msh.tb.client.reports.filters;

import java.util.List;

import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.reports.MainPage;
import org.msh.tb.client.shared.ReportServiceAsync;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CItem;


public class RemoteOptionsFilter extends OptionsFilter{

	/* (non-Javadoc)
	 * @see org.msh.tb.client.reports.filters.FilterWidget#initialize(org.msh.tb.client.shared.model.CFilter)
	 */
	@Override
	public void initialize(CFilter filter) {
		super.initialize(filter);

		ReportServiceAsync srv = MainPage.instance().getService();
		srv.getFilterOptions(filter.getId(), null, new StandardCallback<List<CItem>>() {
			@Override
			public void onSuccess(List<CItem> result) {
				if (result != null)
					fillOptions(result);
			}
		});
	}	

}
