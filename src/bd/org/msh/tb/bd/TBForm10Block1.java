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
import org.msh.tb.indicators.core.IndicatorTable;

@Name("tBForm10Block1")
public class TBForm10Block1 extends Indicator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7230654297736913859L;
	private String strMicroPos, strSmearNeg, strSmearEP, strOthers;
	private boolean flag = false;
	

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		
		IndicatorTable table = getTable();
		Map<String, String> messages = Messages.instance();

		List<TbCase> lst = new ArrayList<TbCase>();
		lst = createQuery().getResultList();
		
		float cntNewM = 0, cntNewF = 0, cntRelM = 0, cntRelF = 0, cntFailM = 0, cntFailF = 0, cntDefM = 0, cntDefF = 0;
		float cntSmearNegM = 0, cntSmearNegF = 0, cntEPM = 0, cntEPF = 0;
		float cntOtherM = 0, cntOtherF = 0;
		
		for(TbCase val:lst){
			System.out.println("TBCase ID ------->" +val.getId());
			
			if(val.getPulmonaryType() != null){
			//checking for Smear Positive
				if(isSmearPos(val.getPulmonaryType().getShortName().toString())){
					if(val.getPatientType() == PatientType.NEW){
						if(isMale(val.getPatient().getGender()))
							cntNewM++;
						
						if(isFemale(val.getPatient().getGender()))
							cntNewF++;
					}
					if(val.getPatientType() == PatientType.RELAPSE){
						if(isMale(val.getPatient().getGender()))
							cntRelM++;
						
						if(isFemale(val.getPatient().getGender()))
							cntRelF++;
					}
					if(val.getPatientType() == PatientType.FAILURE){
						if(isMale(val.getPatient().getGender()))
							cntFailM++;
						
						if(isFemale(val.getPatient().getGender()))
							cntFailF++;
					}
					if(val.getPatientType() == PatientType.AFTER_DEFAULT){
						if(isMale(val.getPatient().getGender()))
							cntDefM++;
						
						if(isFemale(val.getPatient().getGender()))
							cntDefF++;
					}
				}
			
				if(val.getPatientType() == PatientType.NEW && isSmearNeg(val.getPulmonaryType().getShortName().toString())){
					if(isMale(val.getPatient().getGender()))
						cntSmearNegM++;
					
					if(isFemale(val.getPatient().getGender()))
						cntSmearNegF++;
				}
			
			}
			
			if(val.getPatientType() == PatientType.NEW && (val.getExtrapulmonaryType() != null || val.getExtrapulmonaryType2() != null))   {
				if(isMale(val.getPatient().getGender()))
					cntEPM++;
				
				if(isFemale(val.getPatient().getGender()))
					cntEPF++;
				
			}
			
			if(val.getPulmonaryType() != null && isSmearOther(val.getPulmonaryType().getShortName().toString())){
				if(isMale(val.getPatient().getGender()))
					cntOtherM++;
				
				if(isFemale(val.getPatient().getGender()))
					cntOtherF++;
			}
		}
		
	
		addValue(messages.get("manag.gender.male1"), messages.get("#"), cntNewM);
		addValue(messages.get("manag.gender.female1"), messages.get("#"), cntNewF);
		
		
		addValue(messages.get("manag.gender.male2"), messages.get("#"), cntRelM);
		addValue(messages.get("manag.gender.female2"), messages.get("#"), cntRelF);
		
		addValue(messages.get("manag.gender.male3"), messages.get("#"), cntFailM);
		addValue(messages.get("manag.gender.female3"), messages.get("#"), cntFailF);
		
		addValue(messages.get("manag.gender.male4"), messages.get("#"), cntDefM);
		addValue(messages.get("manag.gender.female4"), messages.get("#"), cntDefF);
		
		addValue(messages.get("manag.gender.male5"), messages.get("#"), cntSmearNegM);
		addValue(messages.get("manag.gender.female5"), messages.get("#"), cntSmearNegF);
		
		addValue(messages.get("manag.gender.male6"), messages.get("#"), cntEPM);
		addValue(messages.get("manag.gender.female6"), messages.get("#"), cntEPF);
		
		addValue(messages.get("manag.gender.male7"), messages.get("#"), cntOtherM);
		addValue(messages.get("manag.gender.female7"), messages.get("#"), cntOtherF);
		
		float totM = cntNewM + cntRelM + cntFailM + cntDefM + cntSmearNegM + cntEPM + cntOtherM;
		float totF = cntNewF + cntRelF + cntFailF + cntDefF + cntSmearNegF + cntEPF + cntOtherF;
		
		addValue(messages.get("manag.gender.male8"), messages.get("#"), totM);
		addValue(messages.get("manag.gender.female8"), messages.get("#"), totF);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("#"), totM + totF);	

	}
	
	@Override
	public String getHQLSelect() {
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
	
	public boolean isSmearOther(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("O")){
			flag = true;
		}
		return flag;		
	}
	
	public boolean isSmearExtraPulmonary(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("Smear(-)")){
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
	
	
}
