package org.msh.tb.bd;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generate indicator for Pulmonary TB patients for Bangladesh
 * @author Vani Rao
 *
 */
@Name("pulmonaryRetreatIndicator")
public class PulmonaryRetreatIndicator extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2205664398466084869L;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		//Fetching all confirmed cases notified as 'retreat' 
		//String cond = " c.patientType <> 0 and c.state > 2 ";
		String cond = " c.patientType <> 0 ";
		setCondition(cond);
		setOrderByFields("c.id, c.registrationDate");
		List<TbCase> lst = new ArrayList<TbCase>();
		 lst = createQuery().getResultList();

		float cntRelM = 0, cntRelF = 0, cntFailM = 0, cntFailF = 0, cntDefM = 0, cntDefF = 0, cntOtherM = 0, cntOtherF = 0;
		float cntCureRelM = 0, cntCureFailM = 0, cntCureDefM = 0, cntCureOtherM = 0, cntCureRelF = 0,  cntCureFailF = 0, cntCureDefF = 0, cntCureOtherF = 0;
		float cntTreatCompRelM = 0, cntTreatCompFailM = 0, cntTreatCompDefM = 0, cntTreatCompOtherM = 0, cntTreatCompRelF = 0, cntTreatCompFailF = 0, cntTreatCompDefF = 0,cntTreatCompOtherF = 0; 
		float cntDiedRelM = 0, cntDiedRelF = 0, cntDiedFailM = 0,cntDiedFailF = 0, cntDiedDefM = 0, cntDiedDefF = 0, cntDiedOtherM = 0, cntDiedOtherF =0; 
		float cntFailRelM = 0, cntFailFailM = 0, cntFailDefM = 0, cntFailOtherM = 0, cntFailRelF = 0, cntFailFailF = 0, cntFailDefF = 0, cntFailOtherF = 0;
		float cntDefRelM = 0, cntDefFailM = 0, cntDefDefM = 0, cntDefOtherM = 0, cntDefRelF = 0, cntDefFailF = 0, cntDefDefF = 0,cntDefOtherF = 0;
		float cntTranRelM = 0, cntTranFailM = 0, cntTranDefM = 0, cntTranOtherM = 0, cntTranRelF = 0, cntTranFailF = 0, cntTranDefF = 0, cntTranOtherF = 0;
		float cntNotEvalRelM = 0, cntNotEvalFailM = 0, cntNotEvalDefM = 0, cntNotEvalOtherM = 0;
		float cntNotEvalRelF = 0, cntNotEvalFailF = 0, cntNotEvalDefF = 0, cntNotEvalOtherF = 0;
		
		for(TbCase val:lst){
			/*
			 * TB 11 Report for Retreatment category - 'Relapse'
			 */
			if(val.getPatientType() == PatientType.RELAPSE){
				if(val.getPatient().getGender() == Gender.MALE){
					cntRelM++;
					if(val.getState().ordinal()>2){
					if(val.getState() == CaseState.CURED)
						cntCureRelM++;
					if(val.getState() == CaseState.TREATMENT_COMPLETED)
						cntTreatCompRelM++;
					if(val.getState() == CaseState.DIED)
						cntDiedRelM++;
					if(val.getState() == CaseState.FAILED)
						cntFailRelM++;
					if(val.getState() == CaseState.DEFAULTED)
						cntDefRelM++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntTranRelM++;
					}
					else
						cntNotEvalRelM++;
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntRelF++;
					if(val.getState().ordinal()>2){
					if(val.getState() == CaseState.CURED)
						cntCureRelF++;
					if(val.getState() == CaseState.TREATMENT_COMPLETED)
						cntTreatCompRelF++;
					if(val.getState() == CaseState.DIED)
						cntDiedRelF++;
					if(val.getState() == CaseState.FAILED)
						cntFailRelF++;
					if(val.getState() == CaseState.DEFAULTED)
						cntDefRelF++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntTranRelF++;
					}
					else
						cntNotEvalRelF++;
				}
			}
			
			if(val.getPatientType() == PatientType.FAILURE_FT || val.getPatientType() == PatientType.FAILURE_RT){
				if(val.getPatient().getGender() == Gender.MALE){
					cntFailM++;
					if(val.getState().ordinal()>2){
						if(val.getState() == CaseState.CURED)
							cntCureFailM++;
						if(val.getState() == CaseState.TREATMENT_COMPLETED)
							cntTreatCompFailM++;
						if(val.getState() == CaseState.DIED)
							cntDiedFailM++;
						if(val.getState() == CaseState.FAILED)
							cntFailFailM++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefFailM++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranFailM++;
						}
						else
							cntNotEvalFailM++;
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntFailF++;
					if(val.getState().ordinal()>2){
						if(val.getState() == CaseState.CURED)
							cntCureFailF++;
						if(val.getState() == CaseState.TREATMENT_COMPLETED)
							cntTreatCompFailF++;
						if(val.getState() == CaseState.DIED)
							cntDiedFailF++;
						if(val.getState() == CaseState.FAILED)
							cntFailFailF++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefFailF++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranFailF++;
						}
					else
						cntNotEvalFailF++;
				}
			}
			
			if(val.getPatientType() == PatientType.AFTER_DEFAULT){
				if(val.getPatient().getGender() == Gender.MALE){
					cntDefM++;
					if(val.getState().ordinal()>2){
						if(val.getState() == CaseState.CURED)
							cntCureDefM++;
						if(val.getState() == CaseState.TREATMENT_COMPLETED)
							cntTreatCompDefM++;
						if(val.getState() == CaseState.DIED)
							cntDiedDefM++;
						if(val.getState() == CaseState.FAILED)
							cntFailDefM++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefDefM++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranDefM++;
						}
					else
						cntNotEvalDefM++;
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntDefF++;
					if(val.getState().ordinal()>2){
						if(val.getState() == CaseState.CURED)
							cntCureDefF++;
						if(val.getState() == CaseState.TREATMENT_COMPLETED)
							cntTreatCompDefF++;
						if(val.getState() == CaseState.DIED)
							cntDiedDefF++;
						if(val.getState() == CaseState.FAILED)
							cntFailDefF++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefDefF++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranDefF++;
						}
					else
						cntNotEvalDefF++;
				}
			}
				
				if(val.getPatientType() == PatientType.OTHER){
					if(val.getPatient().getGender() == Gender.MALE){
						cntOtherM++;
						if(val.getState().ordinal()>2){
							if(val.getState() == CaseState.CURED)
								cntCureOtherM++;
							if(val.getState() == CaseState.TREATMENT_COMPLETED)
								cntTreatCompOtherM++;
							if(val.getState() == CaseState.DIED)
								cntDiedOtherM++;
							if(val.getState() == CaseState.FAILED)
								cntFailOtherM++;
							if(val.getState() == CaseState.DEFAULTED)
								cntDefOtherM++;
							if(val.getState() == CaseState.TRANSFERRED_OUT)
								cntTranOtherM++;
							}
						else
							cntNotEvalOtherM++;
					}
					if(val.getPatient().getGender() == Gender.FEMALE){
						cntOtherF++;
						if(val.getState().ordinal()>2){
							if(val.getState() == CaseState.CURED)
								cntCureOtherF++;
							if(val.getState() == CaseState.TREATMENT_COMPLETED)
								cntTreatCompOtherF++;
							if(val.getState() == CaseState.DIED)
								cntDiedOtherF++;
							if(val.getState() == CaseState.FAILED)
								cntFailOtherF++;
							if(val.getState() == CaseState.DEFAULTED)
								cntDefOtherF++;
							if(val.getState() == CaseState.TRANSFERRED_OUT)
								cntTranOtherF++;
							}
						else
							cntNotEvalOtherF++;
					}
			}
		}
		Map<String, String> messages = Messages.instance();
		
		float tot = lst.size();
		
		//Total Retreat - Male
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.relapse"), cntRelM);
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.failures"), cntFailM);
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.default"), cntDefM);
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.others"), cntOtherM);
		
		//Total Retreat - Female
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.relapse"), cntRelF);
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.failures"), cntFailF);
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.default"), cntDefF);
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.others"), cntOtherF);
		
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.relapse"), cntRelM + cntRelF);
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.failures"), cntFailM + cntFailF);
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.default"), cntDefM + cntDefF);
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.others"), cntOtherM + cntOtherF);
//		
		//Case State Cured - Male
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.relapse"), cntCureRelM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.failures"), cntCureFailM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.default"), cntCureDefM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.others"), cntCureOtherM);
		
		//Case State Cured - Female
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.relapse"), cntCureRelF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.failures"), cntCureFailF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.default"), cntCureDefF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.others"), cntCureOtherF);
		
		//Case State Treatment Completed- Male
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.relapse"), cntTreatCompRelM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.failures"), cntTreatCompFailM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.default"), cntTreatCompDefM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.others"), cntTreatCompOtherM);
		
		//Case State Treatment Completed- Female
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.relapse"), cntTreatCompRelF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.failures"), cntTreatCompFailF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.default"), cntTreatCompDefF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.others"), cntTreatCompOtherF);
			
		//Case State Died - Male
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.relapse"), cntDiedRelM);
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.failures"), cntDiedFailM);
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.default"), cntDiedDefM);
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.others"), cntDiedOtherM);
		
		//Case State Died - Female
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.relapse"), cntDiedRelF);
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.failures"), cntDiedFailF);
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.default"), cntDiedDefF);
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.others"), cntDiedOtherF);
		
		//Case State Failure - Male
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.relapse"), cntFailRelM);
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.failures"), cntFailFailM);
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.default"), cntFailDefM);
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.others"), cntFailOtherM);
		
		//Case State Failure - Female
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.relapse"), cntFailRelF);
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.failures"), cntFailFailF);
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.default"), cntFailDefF);
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.others"), cntFailOtherF);
		
		//Case State Defaulted - Male
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.relapse"), cntDefRelM);
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.failures"), cntDefFailM);
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.default"), cntDefDefM);
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.others"), cntDefOtherM);
		
		//Case State Defaulted - Female
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.relapse"), cntDefRelF);
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.failures"), cntDefFailF);
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.default"), cntDefDefF);
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.others"), cntDefOtherF);
		
		//Case State Transferred Out - Male
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.relapse"), cntTranRelM);
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.failures"), cntTranFailM);
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.default"), cntTranDefM);
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.others"), cntTranOtherM);
		
		//Case State Transferred Out - FeMale
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.relapse"), cntTranRelF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.failures"), cntTranFailF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.default"), cntTranDefF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.others"), cntTranOtherF);
		
		//Not Eval - Male
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.relapse"), cntNotEvalRelM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.failures"), cntNotEvalFailM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.default"), cntNotEvalDefM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.others"), cntNotEvalOtherM);
		
		//Not Eval - FeMale
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.relapse"), cntNotEvalRelF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.failures"), cntNotEvalFailF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.default"), cntNotEvalDefF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.others"), cntNotEvalOtherF);
		
		float totRelM = 0, totRelF = 0, totFailM = 0, totFailF = 0, totDefM = 0, totDefF = 0, totOtherM = 0, totOtherF = 0;
		totRelM = cntCureRelM + cntTreatCompRelM + cntDiedRelM + cntFailRelM + cntDefRelM + cntTranRelM;
		totRelF = cntCureRelF + cntTreatCompRelF + cntDiedRelF + cntFailRelF + cntDefRelF + cntTranRelF;
		
		totFailM = cntCureFailM + cntTreatCompFailM + cntDiedFailM + cntFailFailM + cntDefFailM + cntTranFailM;
		totFailF = cntCureFailF + cntTreatCompFailF + cntDiedFailF + cntFailFailF + cntDefFailF + cntTranFailF;
		
		totDefM = cntCureDefM + cntTreatCompDefM + cntDiedDefM + cntFailDefM + cntDefDefM + cntTranDefM;
		totDefF = cntCureDefF + cntTreatCompDefF + cntDiedDefF + cntFailDefF + cntDefDefF + cntTranDefF;
		
		totOtherM = cntCureOtherM + cntTreatCompOtherM + cntDiedOtherM + cntFailOtherM + cntDefOtherM + cntTranOtherM;
		totOtherF = cntCureOtherF + cntTreatCompOtherF + cntDiedOtherF + cntFailOtherF + cntDefOtherF + cntTranOtherF;
		
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.relapse"), totRelM);
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.failures"), totFailM);
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.default"), totDefM);
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.others"), totOtherM);
		
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.relapse"), totRelF);
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.failures"), totFailF);
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.default"), totDefF);
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.others"), totOtherF);
		
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.relapse"), totRelM + totRelF);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.failures"), totFailM + totFailF);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.default"), totDefM + totDefF);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.others"), totOtherM + totOtherF);	
	}
	
	@Override
	public String getHQLSelect() {
		return "";
	}	
}
