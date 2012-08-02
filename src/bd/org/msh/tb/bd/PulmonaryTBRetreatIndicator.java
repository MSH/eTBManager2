package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

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

		//Fetching all confirmed cases of PatientType retreatment and thru filters page selecting cases based on start treatment dt
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
		
		float cntRelMNegM = 0, cntRelMNegF = 0, cntRelMPosM = 0, cntRelMPosF = 0;
		float cntFailMNegM = 0, cntFailMNegF = 0, cntFailMPosM = 0, cntFailMPosF = 0;
		float cntDefMNegM = 0, cntDefMNegF = 0, cntDefMPosM = 0, cntDefMPosF = 0;
		float cntOtherMNegM = 0, cntOtherMNegF = 0, cntOtherMPosM = 0, cntOtherMPosF = 0;
		
		for(TbCase val:lst){
			
			if(val.getPatientType() == PatientType.RELAPSE){
				if(val.getPatient().getGender() == Gender.MALE){
					cntRelM++;
					
					val.getTreatmentPeriod().getIniDate();
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntRelMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntRelMPosM++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
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
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					System.out.println("Microscopy Result ----->" +mres);
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntRelMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntRelMPosF++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
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
			
			if(val.getPatientType() == PatientType.FAILURE_FT || val.getPatientType() == PatientType.FAILURE_RT){
				if(val.getPatient().getGender() == Gender.MALE){
					cntFailM++;
					val.getTreatmentPeriod().getIniDate();
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					System.out.println("Microscopy Result ----->" +mres);
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntFailMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntFailMPosM++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
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
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					System.out.println("Microscopy Result ----->" +mres);
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntFailMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntFailMPosF++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
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
			
			if(val.getPatientType() == PatientType.AFTER_DEFAULT){
				if(val.getPatient().getGender() == Gender.MALE){
					cntDefM++;
					val.getTreatmentPeriod().getIniDate();
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					System.out.println("Microscopy Result ----->" +mres);
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntDefMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntDefMPosM++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
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
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					System.out.println("Microscopy Result ----->" +mres);
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntDefMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntDefMPosF++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
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
				
				if(val.getPatientType() == PatientType.OTHER){
					if(val.getPatient().getGender() == Gender.MALE){
						cntOtherM++;
						val.getTreatmentPeriod().getIniDate();
						String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
						System.out.println("Microscopy Result ----->" +mres);
						
						// Checking for all cases with microscopy results still NOT closed
						if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
							cntOtherMNegM++;
						if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
								|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
								|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
							cntOtherMPosM++;
						
						Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
						Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
						
						// Checking if the case was closed within 2-3 months of start treatment
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
						String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
						System.out.println("Microscopy Result ----->" +mres);
						
						// Checking for all cases with microscopy results still NOT closed
						if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
							cntOtherMNegF++;
						if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
								|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
								|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
							cntOtherMPosF++;
						
						Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
						Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
						
						// Checking if the case was closed within 2-3 months of start treatment
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
		
		float totRelM = 0, totRelF = 0, totFailM = 0, totFailF = 0, totDefM = 0, totDefF = 0, totOtherM = 0, totOtherF = 0;
		
		totRelM = cntRelMNegM + cntRelMPosM + cntDiedRelM + cntFailRelM + cntDefRelM + cntTranRelM;
		totRelF = cntRelMNegF + cntRelMPosF + cntDiedRelF + cntFailRelF + cntDefRelF + cntTranRelF;
		
		totFailM = cntFailMNegM + cntFailMPosM + cntDiedFailM + cntFailFailM + cntDefFailM + cntTranFailM;
		totFailF = cntFailMNegF + cntFailMPosF + cntDiedFailF + cntFailFailF + cntDefFailF + cntTranFailF;
		
		totDefM = cntDefMNegM + cntDefMPosM + cntDiedDefM + cntFailDefM + cntDefDefM + cntTranDefM;
		totDefF = cntDefMNegF + cntDefMPosF + cntDiedDefF + cntFailDefF + cntDefDefF + cntTranDefF;
		
		totOtherM = cntOtherMNegM + cntOtherMPosM + cntDiedOtherM + cntFailOtherM + cntDefOtherM + cntTranOtherM;
		totOtherF = cntOtherMNegF + cntOtherMPosF + cntDiedOtherF + cntFailOtherF + cntDefOtherF + cntTranOtherF;
		
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.relapse"), totRelM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.failures"), totFailM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.default"), totDefM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.others"), totOtherM);
		
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.relapse"), totRelF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.failures"), totFailF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.default"), totDefF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.others"), totOtherF);
		
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

	public String getMicroscopyResult(int tbcaseid, Date ini) {
		
		flagSelectQuery = 2;
		String strMicroscopyResult = "";		
		String condition = "e.dateCollected in (select max(m.dateCollected) from ExamMicroscopy m where m.tbcase.id = " +tbcaseid + ")" ;
		setCondition(condition);
		Object[] o = null;
		try{
		o = (Object[]) createQuery().getSingleResult();
		
		System.out.println("Result ------> " +o[0]);
		System.out.println("date Collected ------> " +o[1]);
		Date dtCollected = (Date)o[1];
		Date dt2Months = addMonthsToDate(ini, 2);
		Date dt3Months = addMonthsToDate(ini, 3);
			if(dtCollected.after(dt2Months) && dtCollected.before(dt3Months))
				strMicroscopyResult = o[0].toString();
			else 
				strMicroscopyResult = "";
		}
		catch (NoResultException e){		
			return null;
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
