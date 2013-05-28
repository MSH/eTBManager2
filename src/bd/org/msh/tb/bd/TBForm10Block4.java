package org.msh.tb.bd;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator;

@Name("tBForm10Block4")
public class TBForm10Block4 extends Indicator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1996031659774134287L;
	@In(create=true) EntityManager entityManager;
	//private boolean flag = false;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
//		String cond = " c.id = e.tbcase.id group by c.id ";
//		setCondition(cond);
//		setOrderByFields("e.tbcase.id, e.dateCollected");
		List<Object[]> lst = createQuery().getResultList();
		
		float cntAgeRange1M = 0,cntAgeRange1F = 0, cntAgeRange2M = 0,cntAgeRange2F = 0, cntAgeRange3M = 0, cntAgeRange3F = 0;
		float cntAgeRange4M = 0, cntAgeRange4F = 0, cntAgeRange5M = 0, cntAgeRange5F = 0, cntAgeRange6M = 0, cntAgeRange6F = 0;
		float cntAgeRange7M = 0, cntAgeRange7F = 0,  cntAgeRange8M = 0, cntAgeRange8F = 0;
		
		for(Object[] val:lst){
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, val[0]);
			//Checking for NEW and Extra Pulmonary
			if(tbcase.getPatientType() == PatientType.NEW && (tbcase.getInfectionSite() == InfectionSite.EXTRAPULMONARY )){
				switch(getAgeRange(tbcase)){
				case 1 :{
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange1M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange1F++;
					break;
					}
				case 2 :{
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange2M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange2F++;
					break;
					}
				case 3 : {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange3M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange3F++;
					break;
					}
				case 4 : {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange4M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange4F++;
					break; 
					}
				case 5 :{
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange5M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange5F++;
					break;
					}
				case 6 : {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange6M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange6F++;
					break;
					}
				case 7 : {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange7M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange7F++;
					break; 
					}
				case 8 : {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntAgeRange8M++;
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntAgeRange8F++;
					break; 
					}
				}
			}			
		}
		Map<String, String> messages = Messages.instance();
		addValue(messages.get("manag.gender.male1"), messages.get("#"), cntAgeRange1M);
		addValue(messages.get("manag.gender.female1"), messages.get("#"), cntAgeRange1F);
		
		addValue(messages.get("manag.gender.male2"), messages.get("#"), cntAgeRange2M);
		addValue(messages.get("manag.gender.female2"), messages.get("#"), cntAgeRange2F);
		
		addValue(messages.get("manag.gender.male3"), messages.get("#"), cntAgeRange3M);
		addValue(messages.get("manag.gender.female3"), messages.get("#"), cntAgeRange3F);
		
		addValue(messages.get("manag.gender.male4"), messages.get("#"), cntAgeRange4M);
		addValue(messages.get("manag.gender.female4"), messages.get("#"), cntAgeRange4F);
		
		addValue(messages.get("manag.gender.male5"), messages.get("#"), cntAgeRange5M);
		addValue(messages.get("manag.gender.female5"), messages.get("#"), cntAgeRange5F);
		
		addValue(messages.get("manag.gender.male6"), messages.get("#"), cntAgeRange6M);
		addValue(messages.get("manag.gender.female6"), messages.get("#"), cntAgeRange6F);
		
		addValue(messages.get("manag.gender.male7"), messages.get("#"), cntAgeRange7M);
		addValue(messages.get("manag.gender.female7"), messages.get("#"), cntAgeRange7F);
		
		addValue(messages.get("manag.gender.male8"), messages.get("#"), cntAgeRange8M);
		addValue(messages.get("manag.gender.female8"), messages.get("#"), cntAgeRange8F);
		
		float cntTotM = cntAgeRange1M + cntAgeRange2M + cntAgeRange3M + cntAgeRange4M + cntAgeRange5M + cntAgeRange6M + cntAgeRange7M + cntAgeRange8M;
		float cntTotF = cntAgeRange1F + cntAgeRange2F + cntAgeRange3F + cntAgeRange4F + cntAgeRange5F + cntAgeRange6F + cntAgeRange7F + cntAgeRange8F;
		
		addValue(messages.get("Gender.MALE"), messages.get("#"), cntTotM);
		addValue(messages.get("Gender.FEMALE"), messages.get("#"), cntTotF);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("#"), cntTotM + cntTotF);
	}
	
	public int getAgeRange(TbCase c ){
		int age = c.getPatientAge();
		int agerange = 0;
		if(age>=0 && age<4)
			agerange = 1;
		if(age>=5 && age<=14)
			agerange = 2;
		if(age>=15 && age<=24)
			agerange = 3;
		if(age>=25 && age<=34)
			agerange = 4;
		if(age>=35 && age<=44)
			agerange = 5;
		if(age>=45 && age<=54)
			agerange = 6;
		if(age>=55 && age<=64)
			agerange = 7;
		if(age >= 65)
			agerange = 8;
		
		return agerange;
	}
	@Override
	public String getHQLSelect() {
		String strSel = "";
		//strSel = "select c.id, c.patient.gender, c.state, c.outcomeDate, e.dateCollected, e.result ";
		strSel = "select c.id, c.patient.gender, c.state, c.outcomeDate ";
		return strSel;
	}
	
	@Override
	protected String getHQLFrom() {
		String strFrom = "";
		//strFrom =  " from ExamMicroscopy e join e.tbcase c ";
		strFrom =  " from TbCase c ";
		return strFrom;
	}
}
