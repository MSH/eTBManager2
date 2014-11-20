package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;

/**
 * Generate indicator for Pulmonary TB patients for Bangladesh
 * @author Vani Rao
 *
 */
@Name("pulmonaryTBRetreatIndicator")
public class PulmonaryTBRetreatIndicator extends Indicator2D{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2903668745457765502L;
	
	@In(create=true) EntityManager entityManager;
	public boolean flagMicroscopyNeg = false;
	public boolean flagMicroscopyPos = false;
	public int flagSelectQuery;
	
	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub

		/*
		 * Fetching all confirmed cases of PatientType 'retreatment' falling within the start treamtnet period selected in the filters page
		 */
		String cond = " c.patientType <> 0 " ;
		setCondition(cond);
		List<TbCase> lst = new ArrayList<TbCase>();
		flagSelectQuery = 1;
		 lst = createQuery().getResultList();

		
		float cntRelM = 0, cntRelF = 0, cntFailM = 0, cntFailF = 0, cntDefM = 0, cntDefF = 0, cntOtherM = 0, cntOtherF = 0;
		float cntDiedRelM = 0, cntDiedRelF = 0, cntDiedFailM = 0,cntDiedFailF = 0, cntDiedDefM = 0, cntDiedDefF = 0, cntDiedOtherM = 0, cntDiedOtherF =0; 
		float cntFailRelM = 0, cntFailFailM = 0, cntFailDefM = 0, cntFailOtherM = 0, cntFailRelF = 0, cntFailFailF = 0, cntFailDefF = 0, cntFailOtherF = 0;
		float cntDefRelM = 0, cntDefFailM = 0, cntDefDefM = 0, cntDefOtherM = 0, cntDefRelF = 0, cntDefFailF = 0, cntDefDefF = 0,cntDefOtherF = 0;
		float cntTranRelM = 0, cntTranFailM = 0, cntTranDefM = 0, cntTranOtherM = 0, cntTranRelF = 0, cntTranFailF = 0, cntTranDefF = 0, cntTranOtherF = 0;
		float cntNotEvalRelM = 0, cntNotEvalFailM = 0, cntNotEvalDefM = 0, cntNotEvalOtherM = 0, cntNotEvalRelF = 0, cntNotEvalFailF = 0, cntNotEvalDefF = 0, cntNotEvalOtherF = 0;
		
		float cntRelMNegM = 0, cntRelMNegF = 0, cntRelMPosM = 0, cntRelMPosF = 0;
		float cntFailMNegM = 0, cntFailMNegF = 0, cntFailMPosM = 0, cntFailMPosF = 0;
		float cntDefMNegM = 0, cntDefMNegF = 0, cntDefMPosM = 0, cntDefMPosF = 0;
		float cntOtherMNegM = 0, cntOtherMNegF = 0, cntOtherMPosM = 0, cntOtherMPosF = 0;
		
		for(TbCase val:lst){
			/*
			 * TB 12 Report for Retreatment category - 'Relapse'
			 */
			if(val.getPatientType() == PatientType.RELAPSE){
				if(val.getPatient().getGender() == Gender.MALE){
					cntRelM++;
					
					val.getTreatmentPeriod().getIniDate();
					/*
					 * Fetches Microscopy results for samples collected within 2-3 months of start treatment
					 */
					String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());
					
					
					/*
					 *  Checking for all cases with a microscopy result within 2-3 months of start treatment while case still NOT closed
					 */
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntRelMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntRelMPosM++;
					if(mres=="")
						cntNotEvalRelM++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					/*
					 * for cases with an outcome within 2-3 months of start treatment
					 */
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedRelM++;
						if(val.getState() == CaseState.FAILED)
							cntFailRelM++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefRelM++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranRelM++;
						}
					}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntRelF++;
					
					val.getTreatmentPeriod().getIniDate();
					/*
					 * Fetches Microscopy results for samples collected within 2-3 months of start treatment
					 */
					String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());
			
					/*
					 *  Checking for all cases with a microscopy result within 2-3 months of start treatment while case still NOT closed
					 */
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntRelMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntRelMPosF++;
					if(mres=="")
						cntNotEvalRelF++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					/*
					 * for cases with an outcome within 2-3 months of start treatment
					 */
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedRelF++;
						if(val.getState() == CaseState.FAILED)
							cntFailRelF++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefRelF++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranRelF++;
						}
					}
				}
			
			/*
			 * TB 12 Report for Retreatment category - 'Failure'
			 */
			if(val.getPatientType() == PatientType.FAILURE_FT || val.getPatientType() == PatientType.FAILURE_RT){
				if(val.getPatient().getGender() == Gender.MALE){
					cntFailM++;
					val.getTreatmentPeriod().getIniDate();
					String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());
					
					/*
					 *  Checking for all cases with a microscopy result within 2-3 months of start treatment while case still NOT closed
					 */
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntFailMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntFailMPosM++;
					if(mres=="")
						cntNotEvalFailM++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					/*
					 * for cases with an outcome within 2-3 months of start treatment
					 */
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedFailM++;
						if(val.getState() == CaseState.FAILED)
							cntFailFailM++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefFailM++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranFailM++;
					}
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntFailF++;
					val.getTreatmentPeriod().getIniDate();
					String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());
					
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntFailMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntFailMPosF++;
					if(mres=="")
						cntNotEvalFailF++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedFailF++;
						if(val.getState() == CaseState.FAILED)
							cntFailFailF++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefFailF++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranFailF++;
					}
				}
			}
			/*
			 * TB 12 Report for Retreatment category - 'Treatment after default'
			 */
			if(val.getPatientType() == PatientType.AFTER_DEFAULT){
				if(val.getPatient().getGender() == Gender.MALE){
					cntDefM++;
					val.getTreatmentPeriod().getIniDate();
					String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());	
				
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntDefMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntDefMPosM++;
					if(mres=="")
						cntNotEvalDefM++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedDefM++;
						if(val.getState() == CaseState.FAILED)
							cntFailDefM++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefDefM++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranDefM++;
					}
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntDefF++;
					val.getTreatmentPeriod().getIniDate();
					String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());
					
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntDefMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntDefMPosF++;
					if(mres=="")
						cntNotEvalDefF++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedDefF++;
						if(val.getState() == CaseState.FAILED)
							cntFailDefF++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefDefF++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranDefF++;
					}
				}
			}
				
			/*
			 * TB 12 Report for Retreatment category - 'Other'
			 */
				if(val.getPatientType() == PatientType.OTHER){
					if(val.getPatient().getGender() == Gender.MALE){
						cntOtherM++;
						val.getTreatmentPeriod().getIniDate();
						String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());
						
						if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
							cntOtherMNegM++;
						if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
								|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
								|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
							cntOtherMPosM++;
						if(mres=="")
							cntNotEvalOtherM++;
						
						Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
						Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
						
						if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
							if(val.getState() == CaseState.DIED)
								cntDiedOtherM++;
							if(val.getState() == CaseState.FAILED)
								cntFailOtherM++;
							if(val.getState() == CaseState.DEFAULTED)
								cntDefOtherM++;
							if(val.getState() == CaseState.TRANSFERRED_OUT)
								cntTranOtherM++;
						}
					}
					if(val.getPatient().getGender() == Gender.FEMALE){
						cntOtherF++;
						val.getTreatmentPeriod().getIniDate();
						String mres = getMicroscopyRes(val.getId(), val.getTreatmentPeriod().getIniDate());
						
						if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
							cntOtherMNegF++;
						if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
								|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
								|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
							cntOtherMPosF++;
						if(mres=="")
							cntNotEvalOtherF++;
						
						Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
						Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
						
						if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedOtherF++;
						if(val.getState() == CaseState.FAILED)
							cntFailOtherF++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefOtherF++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTranOtherF++;
						}
					}
			}
		}
		Map<String, String> messages = Messages.instance();
		
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
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.relapse"), cntRelMNegM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.failures"), cntFailMNegM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.default"), cntDefMNegM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.others"), cntOtherMNegM);
		
		//Case State Cured - Female
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.relapse"), cntRelMNegF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.failures"), cntFailMNegF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.default"), cntDefMNegF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.others"), cntOtherMNegF);
		
		//Case State Treatment Completed- Male
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.relapse"), cntRelMPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.failures"), cntFailMPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.default"), cntDefMPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.others"), cntOtherMPosM);
		
		//Case State Treatment Completed- Female
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.relapse"), cntRelMPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.failures"), cntFailMPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.default"), cntDefMPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.others"), cntOtherMPosF);
			
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
		
		//Case State Transferred Out - Male
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.relapse"), cntTranRelF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.failures"), cntTranFailF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.default"), cntTranDefF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.others"), cntTranOtherF);
		
		// Not Eval - Male
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.relapse"), cntNotEvalRelM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.failures"), cntNotEvalFailM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.default"), cntNotEvalDefM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.others"), cntNotEvalOtherM);
		
		//CNot Eval - FeMale
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.relapse"), cntNotEvalRelF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.failures"), cntNotEvalFailF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.default"), cntNotEvalDefF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.others"), cntNotEvalOtherF);
		
		float totRelM = 0, totRelF = 0, totFailM = 0, totFailF = 0, totDefM = 0, totDefF = 0, totOtherM = 0, totOtherF = 0;
		
		totRelM = cntRelMNegM + cntRelMPosM + cntDiedRelM + cntFailRelM + cntDefRelM + cntTranRelM + cntNotEvalRelM ;
		totRelF = cntRelMNegF + cntRelMPosF + cntDiedRelF + cntFailRelF + cntDefRelF + cntTranRelF + cntNotEvalRelF ;
		
		totFailM = cntFailMNegM + cntFailMPosM + cntDiedFailM + cntFailFailM + cntDefFailM + cntTranFailM + cntNotEvalFailM;
		totFailF = cntFailMNegF + cntFailMPosF + cntDiedFailF + cntFailFailF + cntDefFailF + cntTranFailF + cntNotEvalFailF;
		
		totDefM = cntDefMNegM + cntDefMPosM + cntDiedDefM + cntFailDefM + cntDefDefM + cntTranDefM + cntNotEvalDefM;
		totDefF = cntDefMNegF + cntDefMPosF + cntDiedDefF + cntFailDefF + cntDefDefF + cntTranDefF + cntNotEvalDefF;
		
		totOtherM = cntOtherMNegM + cntOtherMPosM + cntDiedOtherM + cntFailOtherM + cntDefOtherM + cntTranOtherM + cntNotEvalOtherM;
		totOtherF = cntOtherMNegF + cntOtherMPosF + cntDiedOtherF + cntFailOtherF + cntDefOtherF + cntTranOtherF + cntNotEvalOtherF;
		
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
		String strSel = "";
		if(flagSelectQuery == 2)
			strSel = "select e.result, e.dateCollected";
		return strSel;
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		String strFrom = "";
		if(flagSelectQuery == 1)
			strFrom = "from TbCase c";
		if(flagSelectQuery ==2)
			strFrom =  " from ExamMicroscopy e inner join e.tbcase c ";
		return strFrom;
		
	}

	/*
	 * Fetches Microscopy results for samples collected within 2-3 months of start treatment
	 */
	
	public String getMicroscopyRes(int tbcaseid, Date dtIniTreat){
		flagSelectQuery = 2;	
		String strMicroscopyResult = "" ;
		Date st;
		Date end;
			try {
				 st = addMonthsToDate(dtIniTreat, 2);
				 end = addMonthsToDate(dtIniTreat, 3);
			 							 
					String hql = " select e.result, e.dateCollected from ExamMicroscopy e inner join e.tbcase c where " +
					" c.validationState = #{indicatorFilters.validationState} and c.treatmentPeriod.iniDate between #{indicatorFilters.iniDate} and #{indicatorFilters.endDate} " +
					" and  e.tbcase.id = " +tbcaseid +
					" and e.dateCollected between :dt1 and :dt2 ";
		
			List<Object[]> lst = new ArrayList<Object[]>();
				
				lst = entityManager.createQuery(hql)
				.setParameter("dt1", st)
				.setParameter("dt2", end)
				.getResultList();
				
				if(lst.size()==1){
					for(Object[] val: lst){
					strMicroscopyResult = val[0].toString();
					}
				}
				//If more than one result exists between 2-3 months
				if(lst.size()>1){
					List<String> allRes = new ArrayList<String>();
					List<Date> dtCollList = new ArrayList<Date>();
					for(Object[] val: lst){
						allRes.add(val[0].toString());
						dtCollList.add((Date)val[1]);	
					}
					List<Date> dupdtCollList = new ArrayList<Date>();
					dupdtCollList = dtCollList;
					Collections.sort(dupdtCollList);
					Date dtmax = dupdtCollList.get(dupdtCollList.size()-1);
					int index = dtCollList.indexOf(dtmax);
					strMicroscopyResult = allRes.get(index);
				}
			}
			catch(NoResultException e){
				return null;
			} 
			finally{
				flagSelectQuery = 0;
			}
					return strMicroscopyResult;
		
	}
	
	public Date addMonthsToDate(Date date, int addMon){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, addMon);
		Date dFwd = c.getTime();
		return dFwd;		
	}

}
