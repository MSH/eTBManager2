package org.msh.tb.indicators;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

@Name("suspectConfirmedIndicator")
public class SuspectConfirmedIndicator extends Indicator2D {
	private static final long serialVersionUID = -7036968078954538467L;

	
	
	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);
		setCaseState(null);

		getTable().addRow(translateKey(DiagnosisType.SUSPECT), DiagnosisType.SUSPECT);
		getTable().addRow(translateKey(DiagnosisType.CONFIRMED), DiagnosisType.CONFIRMED);
		
		// get new cases
		List<Object[]> lst = generateValuesByField("c.diagnosisType", "c.patientType = " + PatientType.NEW.ordinal());
		addColValues(getMessage("manag.confmdrrep.new"), null, lst);

		// get prev treatment with 1st line drugs
		String condition = "not exists(select p.id from PrevTBTreatment p, in(p.substances) s " +
				"where s.line = #{suspectConfirmedIndicator.medicineSecLine} and p.tbcase.id = c.id) " +
				"and exists(select p.id from PrevTBTreatment p where p.tbcase.id = c.id) " +
				"and c.state >= " + CaseState.ONTREATMENT.ordinal();
		List<Object[]> vals = generateValuesByField("c.diagnosisType", condition);
		addColValues(getMessage("manag.ind.starttreat.prev1l"), null, vals);

		// get prev treatment with 2nd line drugs
		condition = "exists(select p.id from PrevTBTreatment p, in(p.substances) s " +
				"where s.line =#{suspectConfirmedIndicator.medicineSecLine} and p.tbcase.id = c.id) " +
				"and c.state >= " + CaseState.ONTREATMENT.ordinal();
		vals = generateValuesByField("c.diagnosisType", condition);
		addColValues(getMessage("manag.ind.starttreat.prev2l"), null, vals);
	}

	

	public MedicineLine getMedicineSecLine() {
		return MedicineLine.SECOND_LINE;
	}

	
	
	public PatientType getPatientType() {
		return PatientType.NEW;
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
*/	
	@Override
	public boolean isUseDiagnosisTypeFilter() {
		return false;
	}

}
