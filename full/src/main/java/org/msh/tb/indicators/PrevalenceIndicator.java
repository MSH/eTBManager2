package org.msh.tb.indicators;


import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorFilters;

import javax.persistence.EntityManager;

/**
 * Generate the prevalence indicator <br>
 * Consider all on-going cases during the period set in {@link IndicatorFilters}
 * @author Ricardo Memoria
 *
 */
@Name("prevalenceIndicator")
public class PrevalenceIndicator extends Indicator {
	private static final long serialVersionUID = -88508582725383853L;

	@In(create=true) EntityManager entityManager;
	
	@Override
	protected void createIndicators() {
		setNewCasesOnly(false);

		generateIndicatorByOutputSelection(null);
	}

}
