package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.na.entities.CaseDispensingNA;

@Name("tBForm10Block7")
public class TBForm10Block7 extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2014368874909253845L;
	@In(create=true) EntityManager entityManager;
	private boolean flag = false;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub

		List<Object[]> lst = new ArrayList<Object[]>();
		lst = createQuery().getResultList();
		
		
		float cntNewHIVTestedSmearPosM = 0, cntNewHIVTestedSmearPosF = 0, cntNewHIVTestedSmearNegM = 0, cntNewHIVTestedSmearNegF = 0;
		float cntNewHIVTestedEPM = 0, cntNewHIVTestedEPF = 0, cntNewHIVPosEPM = 0,cntNewHIVPosEPF = 0;
		float cntReHIVTestedM = 0, cntReHIVTestedF = 0, cntReHIVPosM = 0, cntReHIVPosF = 0;
		float cntNewHIVPosSmearPosM = 0, cntNewHIVPosSmearPosF = 0, cntNewHIVPosSmearNegM = 0, cntNewHIVPosSmearNegF = 0;
		float cntReHIVPosSmearPosM = 0, cntReHIVPosSmearPosF = 0, cntReHIVPosSmearNegM = 0, cntReHIVPosSmearNegF = 0;
		float cntOtherHIVTestedM = 0, cntOtherHIVTestedF = 0, cntOtherHIVPosM = 0, cntOtherHIVPosF = 0;
		
		for(Object[] val:lst){
		
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, val[0]);
			
				if(tbcase.getPatientType()==PatientType.NEW && isSmearPos(tbcase.getPulmonaryType().getShortName().toString())) {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntNewHIVTestedSmearPosM++;
			
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntNewHIVTestedSmearPosF++;
					}
			
				if(tbcase.getPatientType()== PatientType.NEW && isSmearNeg(tbcase.getPulmonaryType().getShortName().toString())) {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntNewHIVTestedSmearNegM++;
	
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntNewHIVTestedSmearNegF++;
					}
				
				if(tbcase.getPatientType()== PatientType.NEW && (tbcase.getExtrapulmonaryType()!=null) || tbcase.getExtrapulmonaryType2()!=null) {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntNewHIVTestedEPM++;
	
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntNewHIVTestedEPF++;
					}
			
				if(tbcase.getPatientType()!=PatientType.NEW ){
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntReHIVTestedM++;

					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntReHIVTestedF++;
					}
				
				if(isSmearOther(tbcase.getPulmonaryType().getShortName().toString())) {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntOtherHIVTestedM++;

					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntOtherHIVTestedF++;
					}		
				
			if(val[1]==HIVResult.POSITIVE) {
					if(tbcase.getPatientType()==PatientType.NEW && isSmearPos(tbcase.getPulmonaryType().getShortName().toString())) {
						if(tbcase.getPatient().getGender()==Gender.MALE)
							cntNewHIVPosSmearPosM++;
						
						if(tbcase.getPatient().getGender()==Gender.FEMALE)
							cntNewHIVPosSmearPosF++;	
						}
				
					if(tbcase.getPatientType()==PatientType.NEW && isSmearNeg(tbcase.getPulmonaryType().getShortName().toString())) {
						if(tbcase.getPatient().getGender()==Gender.MALE)
							cntNewHIVPosSmearNegM++;
				
						if(tbcase.getPatient().getGender()==Gender.FEMALE)
							cntNewHIVPosSmearNegF++;
						}
					
					if(tbcase.getPatientType()== PatientType.NEW && (tbcase.getExtrapulmonaryType()!=null) || tbcase.getExtrapulmonaryType2()!=null) {
						if(tbcase.getPatient().getGender()==Gender.MALE)
							cntNewHIVPosEPM++;
		
						if(tbcase.getPatient().getGender()==Gender.FEMALE)
							cntNewHIVPosEPF++;
						}
				
					if(tbcase.getPatientType()!=PatientType.NEW ){
						if(tbcase.getPatient().getGender()==Gender.MALE)
							cntReHIVPosM++;
		
						if(tbcase.getPatient().getGender()==Gender.FEMALE)
							cntReHIVPosF++;
						}
					
					if(isSmearOther(tbcase.getPulmonaryType().getShortName().toString())) {
						if(tbcase.getPatient().getGender()==Gender.MALE)
							cntOtherHIVPosM++;

						if(tbcase.getPatient().getGender()==Gender.FEMALE)
							cntOtherHIVPosF++;
						}	
				}
		}
		
		Map<String, String> messages = Messages.instance();
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVTestedSmearPosM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVTestedSmearNegM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVTestedM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.ep"), cntNewHIVTestedEPM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.others"), cntOtherHIVTestedM);
		
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVTestedSmearPosF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVTestedSmearNegF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVTestedF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.ep"), cntNewHIVTestedEPF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.others"), cntOtherHIVTestedF);
		
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVPosSmearPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVPosSmearNegM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.ep"), cntNewHIVPosEPM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.others"), cntOtherHIVPosM);
		
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVPosSmearPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVPosSmearNegF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.ep"), cntNewHIVPosEPF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.others"), cntOtherHIVPosF);
		
		
	}
	
	@Override
	public String getHQLSelect() {
		//return "select e.result, c.patient.gender, c.patientType, c.pulmonaryType.shortName, c.extrapulmonaryType.shortName, c.extrapulmonaryType2.shortName";
		return " select c.id, e.result ";
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		return "from ExamHIV e inner join e.tbcase c ";
		
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
	
	public boolean isSmearOther(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("O")){
			flag = true;
		}
		return flag;		
	}

}
