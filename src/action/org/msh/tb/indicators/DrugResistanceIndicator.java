package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.enums.DrugResistanceType;
import org.msh.tb.indicators.core.Indicator;

/**
 * Generate indicator table with number of cases on treatment by drug resistance type
 * @author Ricardo Memoria
 *
 */
@Name("drugResistanceIndicator")
public class DrugResistanceIndicator extends Indicator {
	private static final long serialVersionUID = -3563110470214298003L;


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.Indicator#createIndicators()
	 */
	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);
		setCaseState(null);
//		setIndicatorDate(IndicatorDate.INITREATMENT_DATE);

		// add rows
		for (DrugResistanceType res: DrugResistanceType.values()) {
			addValue(translateKey(res), 0);
		}

		createItems(generateValuesByField("c.drugResistanceType", null));
	}
	
}
