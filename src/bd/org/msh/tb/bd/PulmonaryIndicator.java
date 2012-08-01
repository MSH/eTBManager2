package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.bd.entities.TbCaseBD;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

/**
 * Generate indicator for Pulmonary TB patients for Bangladesh
 * @author Vani Rao
 *
 */
@Name("pulmonaryIndicator")
public class PulmonaryIndicator extends Indicator2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3656727413285273116L;
	@In(create=true) EntityManager entityManager;
	private List<TbCaseBD>  tbcasebdList; 
	private boolean flag = false;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
	
		//Fetching all confirmed cases notified as 'new' and closed
		String cond = " c.patientType = 0 and c.state > 2";
		setCondition(cond);
		List<TbCase> lst = new ArrayList<TbCase>();
		lst = createQuery().getResultList();
		
		float cntSmearPosM = 0, cntSmearPosF = 0, cntSmearNegM = 0, cntSmearNegF = 0, cntEPM = 0, cntEPF = 0;
		float cntDiedPosM = 0,cntDiedPosF = 0, cntDiedNegM = 0, cntDiedNegF = 0;
		float cntFailPosM = 0, cntFailPosF = 0, cntFailNegM = 0, cntFailNegF = 0;
		float cntDefaultedPosM = 0, cntDefaultedPosF = 0, cntDefaultedNegM = 0, cntDefaultedNegF = 0; 
		float cntTransOutPosM = 0, cntTransOutPosF = 0, cntTransOutNegM = 0, cntTransOutNegF = 0;
		float cntCuredPosM = 0, cntCuredPosF = 0, cntCuredNegM = 0, cntCuredNegF = 0;
		float cntTreatedPosM = 0, cntTreatedNegM = 0, cntTreatedPosF = 0, cntTreatedNegF = 0;
		float cntEPCuredM = 0, cntEPTreatedM = 0, cntEPDiedM = 0, cntEPFailM = 0, cntEPDefaultedM = 0, cntEPTransOutM = 0;
		float cntEPCuredF = 0, cntEPTreatedF = 0, cntEPDiedF = 0, cntEPFailF = 0, cntEPDefaultedF = 0, cntEPTransOutF = 0;
			
		for(TbCase val:lst){
			
			if(val.getPulmonaryType() != null){
				if(isSmearPos(val.getPulmonaryType().getShortName().toString())){
					if(val.getPatient().getGender() == Gender.MALE){
					cntSmearPosM++;
						if(val.getState() == CaseState.CURED)
							cntCuredPosM++;
						if(val.getState() == CaseState.TREATMENT_COMPLETED)
							cntTreatedPosM++;
						if(val.getState() == CaseState.DIED)
							cntDiedPosM++;
						if(val.getState() == CaseState.FAILED)
							cntFailPosM++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefaultedPosM++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTransOutPosM++;
					}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntSmearPosF++;
					if(val.getState() == CaseState.CURED)
						cntCuredPosF++;
					if(val.getState() == CaseState.TREATMENT_COMPLETED)
						cntTreatedPosF++;
					if(val.getState() == CaseState.DIED)
						cntDiedPosF++;
					if(val.getState() == CaseState.FAILED)
						cntFailPosF++;
					if(val.getState() == CaseState.DEFAULTED)
						cntDefaultedPosF++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntTransOutPosF++;
				}
			}
			
			if(isSmearNeg(val.getPulmonaryType().getShortName().toString())){
				if(val.getPatient().getGender() == Gender.MALE){
					cntSmearNegM++;
					if(val.getState() == CaseState.CURED)
						cntCuredNegM++;
					if(val.getState() == CaseState.TREATMENT_COMPLETED)
						cntTreatedNegM++;
					if(val.getState() == CaseState.DIED)
						cntDiedNegM++;
					if(val.getState() == CaseState.FAILED)
						cntFailNegM++;
					if(val.getState() == CaseState.DEFAULTED)
						cntDefaultedNegM++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntTransOutNegM++;
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntSmearNegF++;
					if(val.getState() == CaseState.CURED)
						cntCuredNegF++;
					if(val.getState() == CaseState.TREATMENT_COMPLETED)
						cntTreatedNegF++;
					if(val.getState() == CaseState.DIED)
						cntDiedNegF++;
					if(val.getState() == CaseState.FAILED)
						cntFailNegF++;
					if(val.getState() == CaseState.DEFAULTED)
						cntDefaultedNegF++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntTransOutNegF++;
				}
			}
		}
			
			if((val.getExtrapulmonaryType() != null || val.getExtrapulmonaryType2() != null)){

				if(val.getPatient().getGender() == Gender.MALE){
					cntEPM++;
					if(val.getState() == CaseState.CURED)
						cntEPCuredM++;
					if(val.getState() == CaseState.TREATMENT_COMPLETED)
						cntEPTreatedM++;
					if(val.getState() == CaseState.DIED)
						cntEPDiedM++;
					if(val.getState() == CaseState.FAILED)
						cntEPFailM++;
					if(val.getState() == CaseState.DEFAULTED)
						cntEPDefaultedM++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntEPTransOutM++;
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntEPF++;
					if(val.getState() == CaseState.CURED)
						cntEPCuredF++;
					if(val.getState() == CaseState.TREATMENT_COMPLETED)
						cntEPTreatedF++;
					if(val.getState() == CaseState.DIED)
						cntEPDiedF++;
					if(val.getState() == CaseState.FAILED)
						cntEPFailF++;
					if(val.getState() == CaseState.DEFAULTED)
						cntEPDefaultedF++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntEPTransOutF++;
				}			
			}
		}
		
		Map<String, String> messages = Messages.instance();
		
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosM);
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegM);
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.extrapulmonary"),cntEPM);
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosF);
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegF);
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.extrapulmonary"),cntEPF);
		
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosM + cntSmearPosF);
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegM + cntSmearNegF);
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.extrapulmonary"), cntEPM + cntEPF);
		
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.smearpositive"), cntCuredPosM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.smearnegative"), cntCuredNegM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.extrapulmonary"), cntEPCuredM);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.smearpositive"), cntCuredPosF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.smearnegative"), cntCuredNegF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.extrapulmonary"), cntEPCuredF);
		
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.smearpositive"), cntTreatedPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.smearnegative"), cntTreatedNegM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.extrapulmonary"), cntEPTreatedM);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.smearpositive"), cntTreatedPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.smearnegative"), cntTreatedNegF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.extrapulmonary"), cntEPTreatedF);
	

		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.smearpositive"), cntDiedPosM);
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.smearnegative"), cntDiedNegM);
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.extrapulmonary"), cntEPDiedM);
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.smearpositive"), cntDiedPosF);
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.smearnegative"), cntDiedNegF);
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.extrapulmonary"), cntEPDiedF);
		
	
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.smearpositive"), cntFailPosM);
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.smearnegative"), cntFailNegM);
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.extrapulmonary"), cntEPFailM);
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.smearpositive"), cntFailPosF);
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.smearnegative"), cntFailNegF);
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.extrapulmonary"), cntEPFailF);
			

		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.smearpositive"), cntDefaultedPosM);
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.smearnegative"), cntDefaultedNegM);
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.extrapulmonary"), cntEPDefaultedM);
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.smearpositive"), cntDefaultedPosF);
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.smearnegative"), cntDefaultedNegF);
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.extrapulmonary"), cntEPDefaultedF);
		

		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.smearpositive"), cntTransOutPosM);
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.smearnegative"), cntTransOutNegM);
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.extrapulmonary"), cntEPTransOutM);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.smearpositive"), cntTransOutPosF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.smearnegative"), cntTransOutNegF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.extrapulmonary"), cntEPTransOutF);
		
		float totPosM = cntCuredPosM + cntTreatedPosM + cntDiedPosM + cntFailPosM + cntDefaultedPosM + cntTransOutPosM;
		float totNegM = cntCuredNegM + cntTreatedNegM + cntDiedNegM + cntFailNegM + cntDefaultedNegM + cntTransOutNegM;
		float totPosF = cntCuredPosF + cntTreatedPosF + cntDiedPosF + cntFailPosF + cntDefaultedPosF + cntTransOutPosF;
		float totNegF = cntCuredNegF + cntTreatedNegF + cntDiedNegF + cntFailNegF + cntDefaultedNegF + cntTransOutNegF;
		float totEPM = cntEPCuredM + cntEPTreatedM + cntEPDiedM + cntEPFailM + cntEPDefaultedM + cntEPTransOutM;
		float totEPF = cntEPCuredF + cntEPTreatedF + cntEPDiedF + cntEPFailF + cntEPDefaultedF + cntEPTransOutF;
		
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearpositive"), totPosM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearnegative"), totNegM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.extrapulmonary"), totEPM);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearpositive"), totPosF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearnegative"), totNegF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.extrapulmonary"), totEPF);
		
		float totPos = totPosM + totPosF;
		float totNeg = totNegM + totNegF;
		float totEP = totEPM + totEPF;
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearpositive"), totPos);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearnegative"), totNeg);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.extrapulmonary"), totEP);

		
	}

	
	public List<TbCaseBD> getTbcasebdList() {
		return tbcasebdList;
	}
	
	public void setTbcasebdList(List<TbCaseBD> tbcasebdList) {
		this.tbcasebdList = tbcasebdList;
	}
	
	@Override
	public String getHQLSelect() {
		//return "select c.id, c.state, c.patientType, c.pulmonaryType.id, c.pulmonaryType.shortName, c.patient.gender ";
		return "";
	}

	public boolean isSmearPos(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("Smear(+)")){
			flag = true;
		}
		return flag;		
	}
	
	public boolean isSmearNeg(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("Smear(-)")){
			flag = true;
		}
		return flag;		
	}	
}