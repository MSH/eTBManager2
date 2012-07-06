package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.HealthUnitsQuery;

@Name("healthUnitsQueryUA")
public class HealthUnitsQueryUA extends HealthUnitsQuery {
	private static final long serialVersionUID = 4887501919593423214L;

	@Override
	public Integer getMaxResults() {
		return null;
	}
}
