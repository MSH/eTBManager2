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
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorTable;

@Name("tBForm10Block1")
public class TBForm10Block1 extends Indicator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7230654297736913859L;
	@In(create=true) EntityManager entityManager;
	private String strMicroPos, strSmearNeg, strSmearEP, strOthers;
	private boolean flag = false;
	

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		
		IndicatorTable table = getTable();
		Map<String, String> messages = Messages.instance();

		List<Object[]> lst = createQuery().getResultList();
		
		float cntNewM = 0, cntNewF = 0, cntRelM = 0, cntRelF = 0, cntFailM = 0, cntFailF = 0, cntDefM = 0, cntDefF = 0;
		float cntSmearNegM = 0, cntSmearNegF = 0, cntEPM = 0, cntEPF = 0;
		float cntOtherM = 0, cntOtherF = 0;
		
		for(Object[] val:lst){
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, val[0]);
			
			/*
			 * Checking for 1st reported Microscopy result to be Positive
			 */
				if(val[5] == MicroscopyResult.PLUS || val[5] == MicroscopyResult.PLUS2 || val[5] == MicroscopyResult.PLUS3 || val[5] == MicroscopyResult.PLUS4) {
						if(tbcase.getPatientType() == PatientType.NEW){
							if(tbcase.getPatient().getGender()==Gender.MALE)
								cntNewM++;
						
							if(tbcase.getPatient().getGender()==Gender.FEMALE)
								cntNewF++;
							}
						if(tbcase.getPatientType() == PatientType.RELAPSE){
							if(tbcase.getPatient().getGender()==Gender.MALE)
								cntRelM++;
						
							if(tbcase.getPatient().getGender()==Gender.FEMALE)
								cntRelF++;
							}
						if(tbcase.getPatientType() == PatientType.FAILURE){
							if(tbcase.getPatient().getGender()==Gender.MALE)
								cntFailM++;
						
							if(tbcase.getPatient().getGender()==Gender.FEMALE)
								cntFailF++;
							}
						if(tbcase.getPatientType() == PatientType.AFTER_DEFAULT){
							if(tbcase.getPatient().getGender()==Gender.MALE)
								cntDefM++;
						
							if(tbcase.getPatient().getGender()==Gender.FEMALE)
								cntDefF++;
							}
					}
				
				if(tbcase.getPatientType() == PatientType.NEW && val[5] == MicroscopyResult.NEGATIVE){
					if(isMale(tbcase.getPatient().getGender()))
						cntSmearNegM++;
					
					if(isFemale(tbcase.getPatient().getGender()))
						cntSmearNegF++;
					}
		
				if(tbcase.getPatientType() == PatientType.NEW && (tbcase.getExtrapulmonaryType() != null || tbcase.getExtrapulmonaryType2() != null))   {
					if(tbcase.getPatient().getGender()==Gender.MALE)
						cntEPM++;
				
					if(tbcase.getPatient().getGender()==Gender.FEMALE)
						cntEPF++;
					}
			
				if(tbcase.getPulmonaryType() != null && isSmearOther(tbcase.getPulmonaryType().getShortName().toString())){
					if(isMale(tbcase.getPatient().getGender()))
						cntOtherM++;
					if(isFemale(tbcase.getPatient().getGender()))
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
	@Override
	public String getHQLSelect() {
		String strSel = "";
		strSel = "select c.id, c.patient.gender, c.state, c.outcomeDate, e.dateCollected, e.result ";
		return strSel;
	}
	
	@Override
	protected String getHQLFrom() {
		String strFrom = "";
		strFrom =  " from ExamMicroscopy e join e.tbcase c ";
		return strFrom;
	}
	
	
}
