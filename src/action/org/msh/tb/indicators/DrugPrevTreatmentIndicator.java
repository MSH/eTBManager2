package org.msh.tb.indicators;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

@Name("drugPrevTreatmentIndicator")
public class DrugPrevTreatmentIndicator extends Indicator2D {
	private static final long serialVersionUID = -1249166676563999475L;
	
	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);

		String outcomes = "and c.state >= " + CaseState.WAITING_TREATMENT.ordinal();
		// get new cases
		List<Object[]> lst = generateValuesByField("c.state", "c.patientType = " + PatientType.NEW.ordinal() + outcomes);
		addRowValues(getMessage("manag.confmdrrep.new"), null, lst);

		// get cases previously treated with 1st line drugs
		String cond = "not exists(select p.id from PrevTBTreatment p, in(p.substances) s " +
				"where s.line = " + MedicineLine.SECOND_LINE.ordinal() + " and p.tbcase.id = c.id) " +
				outcomes;
		lst = generateValuesByField("c.state", cond);
		addRowValues(getMessage("manag.confmdrrep.prev1line"), null, lst);

		// get cases previously treated with 1st and 2nd line drugs
		cond = "exists(select p.id from PrevTBTreatment p, in(p.substances) s " +
				"where s.line = " + MedicineLine.SECOND_LINE.ordinal() + " and p.tbcase.id = c.id) " +
				outcomes;
		lst = generateValuesByField("c.state", cond);
		addRowValues(getMessage("manag.confmdrrep.prev12line"), null, lst);
	}



	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.CaseHQLBase#getClassification()
	 */
	@Override
	public CaseClassification getClassification() {
		return CaseClassification.DRTB;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.CaseHQLBase#getIndicatorDate()
	 */
/*	@Override
	public IndicatorDate getIndicatorDate() {
		return IndicatorDate.INITREATMENT_DATE;
	}

*/}
