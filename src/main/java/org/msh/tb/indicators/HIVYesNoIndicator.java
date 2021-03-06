package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.indicators.core.Indicator;

import java.util.Map;

/**
 * Generate indicator about cases with and without HIV results
 * @author Utkarsh Srivastava
 *
 */
@Name("hivYesNoIndicator")
public class HIVYesNoIndicator extends Indicator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 204796497516711901L;

	@Override
	protected void createIndicators() {
		Map<String, String> messages = Messages.instance();

		// calculate number of cases with side effect
		int num = calcNumberOfCases("exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id = c.id)");
		addValue(messages.get("global.yes"), num);
		
		// calculate total number of cases and subtract the quantity of cases with side effect
		num = calcNumberOfCases(null) - num;
		addValue(messages.get("global.no"), num);
	}

}
