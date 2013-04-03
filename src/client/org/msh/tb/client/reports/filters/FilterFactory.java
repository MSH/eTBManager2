package org.msh.tb.client.reports.filters;

import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CFilterType;


public class FilterFactory {

	public static FilterWidget createWidgetFilter(CFilter filter) {
		if (filter.getType() == CFilterType.DATE)
			return new PeriodFilter(filter);

		return new OptionsFilter(filter);
	}
}
