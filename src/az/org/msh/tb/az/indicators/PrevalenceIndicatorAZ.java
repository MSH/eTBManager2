package org.msh.tb.az.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.IndicatorFilters;

/**
 * Generates the TB/MDR-TB incidence indicator <br>
 * The incidence indicator considers just new cases diagnosed in the period set in the {@link IndicatorFilters} 
 * @author Ricardo Memória
 *
 */
@Name("prevalenceIndicatorAZ")
public class PrevalenceIndicatorAZ extends OutputSelectionIndicator {
	private static final long serialVersionUID = -3029326408511512325L;
	
	@Override
	protected void createIndicators() {
		if (validate()){
			setNewCasesOnly(false);
			generateIndicatorByOutputSelection(null);
		}	
	}
	
	@Override
	protected String getHQLWhere() {
		String hql = super.getHQLWhere();
		return hql+getHQLWhereCond();
	}
}
