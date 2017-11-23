package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.misc.GlobalLists;

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
		//setIndicatorDate(IndicatorDate.INITREATMENT_DATE);

		// add rows
		for (DrugResistanceType res: GlobalLists.instance().getDrugResistanceTypes()) {
			addValue(translateKey(res), 0);
		}

		createItems(generateValuesByField("c.drugResistanceType", null));
		
		
	}


	@Override
	public CaseClassification getClassification() {
		// TODO Auto-generated method stub
		return CaseClassification.DRTB;
	}
	
	
	
}
