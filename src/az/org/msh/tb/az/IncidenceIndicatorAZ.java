package org.msh.tb.az;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.indicators.IncidenceIndicator;
import org.msh.tb.indicators.core.IndicatorFilters;

/**
 * Generates the TB/MDR-TB incidence indicator <br>
 * The incidence indicator considers just new cases diagnosed in the period set in the {@link IndicatorFilters} 
 * @author Ricardo Memória
 *
 */
@Name("incidenceIndicatorAZ")
public class IncidenceIndicatorAZ extends OutputSelectionIndicator {
	private static final long serialVersionUID = -3029326408511512325L;
	
	@Override
	protected void createIndicators() {
		if (validate()){
			setNewCasesOnly(true);
			generateIndicatorByOutputSelection(null);
		}	
	}
	
	@Override
	protected String getHQLWhere() {
		IncidenceIndicator ind = (IncidenceIndicator) App.getComponent("incidenceIndicator");
		return ind.getHQLWherePublic()+getHQLWhereCond();
	}
}
