package org.msh.tb.indicators;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorDate;

/**
 * Generates case outcome indicator report
 * @author Ricardo Memoria
 *
 */
@Name("outcomeIndicator")
public class OutcomeIndicator extends Indicator {
	private static final long serialVersionUID = 1L;
	
	private static final CaseState[] outcomes = { 
		CaseState.CURED,
		CaseState.DEFAULTED,
		CaseState.DIED,
		CaseState.FAILED,
		CaseState.TRANSFERRED_OUT,
		CaseState.TREATMENT_COMPLETED};
	
	private Float successRate;


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);
		
		List<Object[]> lst = generateValuesByField("c.state", null);
		
		List<CaseState> outs = Arrays.asList(outcomes);

		Map<String, String> messages = getMessages();

		successRate = 0F;
		float total = 0;
		
		for (Object[] vals: lst) {
			CaseState cs = (CaseState)vals[0];
			Long qtd = (Long)vals[1];
			
			if (outs.contains(cs)) {
				addValue(messages.get(cs.getKey()), qtd.intValue());
				
				if ((CaseState.CURED.equals(cs)) || (CaseState.TREATMENT_COMPLETED.equals(cs)))
					successRate += qtd.intValue();
				total += qtd.intValue();
			}
		}
		
		if (successRate > 0) {
			successRate = successRate / total * 100;
		}
	}

	/**
	 * Return the success rate of the report
	 * @return
	 */
	public Float getSuccessRate() {
		if (successRate == null) {
			open();
		}
		
		return successRate;
	}

}
