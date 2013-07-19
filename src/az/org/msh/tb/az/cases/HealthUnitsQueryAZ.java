package org.msh.tb.az.cases;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.HealthUnitsQuery;

@Name("healthUnitsQueryAZ")
public class HealthUnitsQueryAZ extends HealthUnitsQuery {
	private static final long serialVersionUID = 4887501919593423214L;

	@Override
	public Integer getMaxResults() {
		return null;
	}
}
