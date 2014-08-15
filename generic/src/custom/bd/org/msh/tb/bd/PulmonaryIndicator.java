package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.bd.entities.TbCaseBD;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

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
	
	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
	
		//Fetching all confirmed cases notified as 'new' and closed
		//String cond = " c.patientType = 0 and c.state > 2 and  c.id = e.tbcase.id group by c.id";
		String cond = " c.patientType = 0 and  c.id = e.tbcase.id group by c.id";
		setCondition(cond);
		setOrderByFields("e.tbcase.id, e.sample.dateCollected");
		List<Object[]> lst = createQuery().getResultList();
		
		float cntSmearPosM = 0, cntSmearPosF = 0, cntSmearNegM = 0, cntSmearNegF = 0, cntEPM = 0, cntEPF = 0;
		float cntDiedPosM = 0,cntDiedPosF = 0, cntDiedNegM = 0, cntDiedNegF = 0;
		float cntFailPosM = 0, cntFailPosF = 0, cntFailNegM = 0, cntFailNegF = 0;
		float cntDefaultedPosM = 0, cntDefaultedPosF = 0, cntDefaultedNegM = 0, cntDefaultedNegF = 0; 
		float cntTransOutPosM = 0, cntTransOutPosF = 0, cntTransOutNegM = 0, cntTransOutNegF = 0;
		float cntCuredPosM = 0, cntCuredPosF = 0, cntCuredNegM = 0, cntCuredNegF = 0;
		float cntTreatedPosM = 0, cntTreatedNegM = 0, cntTreatedPosF = 0, cntTreatedNegF = 0;
		float cntEPCuredM = 0, cntEPTreatedM = 0, cntEPDiedM = 0, cntEPFailM = 0, cntEPDefaultedM = 0, cntEPTransOutM = 0;
		float cntEPCuredF = 0, cntEPTreatedF = 0, cntEPDiedF = 0, cntEPFailF = 0, cntEPDefaultedF = 0, cntEPTransOutF = 0;
		float cntNotEvalPosM = 0, cntNotEvalPosF = 0, cntNotEvalNegM = 0, cntNotEvalNegF = 0, cntNotEvalEPM = 0, cntNotEvalEPF = 0;
			
for(Object[] val:lst){
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, val[0]);
			
			/*
			 * Checking for 1st reported Microscopy result to be Positive
			 */
			//	if(val[5] == MicroscopyResult.PLUS || val[5] == MicroscopyResult.PLUS2 || val[5] == MicroscopyResult.PLUS3 || val[5] == MicroscopyResult.PLUS4) {
			if(((MicroscopyResult)val[5]).isPositive()) {	
					if(val[1] == Gender.MALE){
					cntSmearPosM++;
					if(tbcase.getState().ordinal()>2){
						if(val[2] == CaseState.CURED)
							cntCuredPosM++;
						if(val[2] == CaseState.TREATMENT_COMPLETED)
							cntTreatedPosM++;
						if(val[2] == CaseState.DIED)
							cntDiedPosM++;
						if(val[2] == CaseState.FAILED)
							cntFailPosM++;
						if(val[2] == CaseState.DEFAULTED)
							cntDefaultedPosM++;
						if(val[2] == CaseState.TRANSFERRED_OUT)
							cntTransOutPosM++;
						}
					else 
							cntNotEvalPosM++;	
					}
				if(val[1] == Gender.FEMALE){
					cntSmearPosF++;
					if(tbcase.getState().ordinal()>2){
					if(val[2] == CaseState.CURED)
						cntCuredPosF++;
					if(val[2] == CaseState.TREATMENT_COMPLETED)
						cntTreatedPosF++;
					if(val[2] == CaseState.DIED)
						cntDiedPosF++;
					if(val[2] == CaseState.FAILED)
						cntFailPosF++;
					if(val[2] == CaseState.DEFAULTED)
						cntDefaultedPosF++;
					if(val[2] == CaseState.TRANSFERRED_OUT)
						cntTransOutPosF++;
						}
					else
						cntNotEvalPosF++;
					}
				}
		
			//if(val[5] == MicroscopyResult.NEGATIVE){
			if (((MicroscopyResult)val[5]).isNegative()){
				if(val[1] == Gender.MALE){
					cntSmearNegM++;
					if(tbcase.getState().ordinal()>2){
					if(val[2] == CaseState.CURED)
						cntCuredNegM++;
					if(val[2] == CaseState.TREATMENT_COMPLETED)
						cntTreatedNegM++;
					if(val[2] == CaseState.DIED)
						cntDiedNegM++;
					if(val[2] == CaseState.FAILED)
						cntFailNegM++;
					if(val[2] == CaseState.DEFAULTED)
						cntDefaultedNegM++;
					if(val[2] == CaseState.TRANSFERRED_OUT)
						cntTransOutNegM++;
						}
					else
						cntNotEvalNegM++;
					}
				if(val[1] == Gender.FEMALE){
					cntSmearNegF++;
					if(tbcase.getState().ordinal()>2){
					if(val[2] == CaseState.CURED)
						cntCuredNegF++;
					if(val[2] == CaseState.TREATMENT_COMPLETED)
						cntTreatedNegF++;
					if(val[2] == CaseState.DIED)
						cntDiedNegF++;
					if(val[2] == CaseState.FAILED)
						cntFailNegF++;
					if(val[2] == CaseState.DEFAULTED)
						cntDefaultedNegF++;
					if(val[2] == CaseState.TRANSFERRED_OUT)
						cntTransOutNegF++;
					}
					else
						cntNotEvalNegF++;
				}
			}
			
			//if(tbcase.getExtrapulmonaryType() != null || tbcase.getExtrapulmonaryType2() != null){
			if(tbcase.getInfectionSite()==InfectionSite.EXTRAPULMONARY){

				if(val[1] == Gender.MALE){
					cntEPM++;
					if(tbcase.getState().ordinal()>2){
					if(val[2] == CaseState.CURED)
						cntEPCuredM++;
					if(val[2] == CaseState.TREATMENT_COMPLETED)
						cntEPTreatedM++;
					if(val[2] == CaseState.DIED)
						cntEPDiedM++;
					if(val[2] == CaseState.FAILED)
						cntEPFailM++;
					if(val[2] == CaseState.DEFAULTED)
						cntEPDefaultedM++;
					if(val[2] == CaseState.TRANSFERRED_OUT)
						cntEPTransOutM++;
					}
					else
						cntNotEvalEPM++;
				}
				if(val[1] == Gender.FEMALE){
					cntEPF++;
					if(tbcase.getState().ordinal()>2){
					if(val[2] == CaseState.CURED)
						cntEPCuredF++;
					if(val[2] == CaseState.TREATMENT_COMPLETED)
						cntEPTreatedF++;
					if(val[2] == CaseState.DIED)
						cntEPDiedF++;
					if(val[2] == CaseState.FAILED)
						cntEPFailF++;
					if(val[2] == CaseState.DEFAULTED)
						cntEPDefaultedF++;
					if(val[2] == CaseState.TRANSFERRED_OUT)
						cntEPTransOutF++;
					}	
					else
						cntNotEvalEPF++;
				}
			}
				
		} //  end of for
		
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
		
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearpositive"), cntNotEvalPosM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearnegative"), cntNotEvalNegM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.extrapulmonary"), cntNotEvalEPM);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearpositive"), cntNotEvalPosF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearnegative"), cntNotEvalNegF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.extrapulmonary"), cntNotEvalEPF);
		
		float totPosM = cntCuredPosM + cntTreatedPosM + cntDiedPosM + cntFailPosM + cntDefaultedPosM + cntTransOutPosM + cntNotEvalPosM;
		float totNegM = cntCuredNegM + cntTreatedNegM + cntDiedNegM + cntFailNegM + cntDefaultedNegM + cntTransOutNegM + cntNotEvalNegM;
		float totPosF = cntCuredPosF + cntTreatedPosF + cntDiedPosF + cntFailPosF + cntDefaultedPosF + cntTransOutPosF + cntNotEvalPosF; 
		float totNegF = cntCuredNegF + cntTreatedNegF + cntDiedNegF + cntFailNegF + cntDefaultedNegF + cntTransOutNegF + cntNotEvalNegF;
		float totEPM = cntEPCuredM + cntEPTreatedM + cntEPDiedM + cntEPFailM + cntEPDefaultedM + cntEPTransOutM + cntNotEvalEPM;
		float totEPF = cntEPCuredF + cntEPTreatedF + cntEPDiedF + cntEPFailF + cntEPDefaultedF + cntEPTransOutF + cntNotEvalEPF;
		
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.smearpositive"), totPosM);
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.smearnegative"), totNegM);
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.extrapulmonary"), totEPM);
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.smearpositive"), totPosF);
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.smearnegative"), totNegF);
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.extrapulmonary"), totEPF);
		
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
		String strSel = "";
		strSel = "select c.id, c.patient.gender, c.state, c.outcomeDate, e.sample.dateCollected, e.result ";
		return strSel;
	}
	
	@Override
	protected String getHQLFrom() {
		String strFrom = "";
		strFrom =  " from ExamMicroscopy e join e.tbcase c ";
		return strFrom;
	}
}