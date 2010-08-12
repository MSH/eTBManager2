package org.msh.tb.indicators;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorFilters;

/**
 * Generates the TB/MDR-TB incidence indicator <br>
 * The incidence indicator considers just new cases diagnosed in the period set in the {@link IndicatorFilters} 
 * @author Ricardo Memória
 *
 */
@Name("incidenceIndicator")
public class IncidenceIndicator extends Indicator {
	private static final long serialVersionUID = -3029326408511512325L;

	@In(create=true) EntityManager entityManager;

	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);
		
		generateIndicatorByOutputSelection(null);
	}
}
