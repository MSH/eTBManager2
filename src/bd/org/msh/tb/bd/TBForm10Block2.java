package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator;

@Name("tBForm10Block2")
public class TBForm10Block2 extends Indicator{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5835057642122065366L;
	private boolean flag = false;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		List<TbCase> lst = new ArrayList<TbCase>();
		lst = createQuery().getResultList();
		
		float cntAgeRange1M = 0,cntAgeRange1F = 0, cntAgeRange2M = 0,cntAgeRange2F = 0, cntAgeRange3M = 0, cntAgeRange3F = 0;
		float cntAgeRange4M = 0, cntAgeRange4F = 0, cntAgeRange5M = 0, cntAgeRange5F = 0, cntAgeRange6M = 0, cntAgeRange6F = 0;
		float cntAgeRange7M = 0, cntAgeRange7F = 0,  cntAgeRange8M = 0, cntAgeRange8F = 0;
		
		for(TbCase val:lst){
			//Checking for NEW Smear Positive
			if(val.getPatientType() == PatientType.NEW && val.getPulmonaryType() != null && isSmearPos(val.getPulmonaryType().getShortName().toString())){
				switch(getAgeRange(val)){
				case 1 :{
					if(isMale(val.getPatient().getGender()))
						cntAgeRange1M++;
					if(isFemale(val.getPatient().getGender()))
						cntAgeRange1F++;
					break;
					}
				case 2 :{
					if(isMale(val.getPatient().getGender()))
						cntAgeRange2M++;
					if(isFemale(val.getPatient().getGender()))
						cntAgeRange2F++;
					break;
					}
				case 3 : {
					if(isMale(val.getPatient().getGender()))
						cntAgeRange3M++;
					if(isFemale(val.getPatient().getGender()))
						cntAgeRange3F++;
					break;
					}
				case 4 : {
					if(isMale(val.getPatient().getGender()))
						cntAgeRange4M++;
					if(isFemale(val.getPatient().getGender()))
						cntAgeRange4F++;
					break; 
					}
				case 5 :{
					if(isMale(val.getPatient().getGender()))
						cntAgeRange5M++;
					if(isFemale(val.getPatient().getGender()))
						cntAgeRange5F++;
					break;
					}
				case 6 : {
					if(isMale(val.getPatient().getGender()))
						cntAgeRange6M++;
					if(isFemale(val.getPatient().getGender()))
						cntAgeRange6F++;
					break;
					}
				case 7 : {
					if(isMale(val.getPatient().getGender()))
						cntAgeRange7M++;
					if(isFemale(val.getPatient().getGender()))
						cntAgeRange7F++;
					break; 
					}
				case 8 : {
					if(isMale(val.getPatient().getGender()))
						cntAgeRange8M++;
					if(isFemale(val.getPatient().getGender()))
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
	
	public boolean isSmearPos(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("Smear(+)")){
			flag = true;
		}
		return flag;		
	}

	public boolean isMale(Gender g){
		flag = false;
		if(g == Gender.MALE)
				flag = true;
		return flag;
	}
	
	public boolean isFemale(Gender g){
		flag = false;
		if(g == Gender.FEMALE)
				flag = true;
		return flag;
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
		return "";
	}

}
