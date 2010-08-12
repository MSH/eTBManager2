package org.msh.tb.indicators;

import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.indicators.core.Indicator;

/**
 * Generate indicator about cases with and without side effects
 * @author Ricardo Memoria
 *
 */
@Name("sideEffectYesNoIndicator")
public class SideEffectYesNoIndicator extends Indicator {
	private static final long serialVersionUID = -372178339367723448L;

	@Override
	protected void createIndicators() {
		Map<String, String> messages = Messages.instance();

		// calculate number of cases with side effect
		int num = calcNumberOfCases("exists(select se.id from CaseSideEffect se where se.tbcase.id = c.id)");
		addValue(messages.get("global.yes"), num);
		
		// calculate total number of cases and subtract the quantity of cases with side effect
		num = calcNumberOfCases(null) - num;
		addValue(messages.get("global.no"), num);
	}

}
