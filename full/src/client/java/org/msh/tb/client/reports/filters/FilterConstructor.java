package org.msh.tb.client.reports.filters;

import org.msh.tb.client.shared.model.CFilter;

public interface FilterConstructor {
	public FilterWidget create(CFilter filter);
}
