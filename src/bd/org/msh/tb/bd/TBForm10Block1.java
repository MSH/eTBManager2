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
import org.msh.tb.entities.enums.InfectionSite;
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
	private int flag = 0;
	

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		
		IndicatorTable table = getTable();
		Map<String, String> messages = Messages.instance();
			
		flag = 2;
		List<Object[]> obj = createQuery().getResultList();
		flag = 0;

		String cond = " c.id = e.tbcase.id group by c.id ";
		setCondition(cond);
		setOrderByFields("e.tbcase.id, e.dateCollected");
		flag = 1;
		List<Object[]> lst = createQuery().getResultList();
		flag = 0;
		
		
		
		float cntNewM = 0, cntNewF = 0, cntRelM = 0, cntRelF = 0, cntFailM = 0, cntFailF = 0, cntDefM = 0, cntDefF = 0;
		float cntSmearNegM = 0, cntSmearNegF = 0, cntEPM = 0, cntEPF = 0;
		float cntOtherM = 0, cntOtherF = 0;
		
		for(Object[] val:lst){
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, val[0]);
			
			/*
			 * Checking for 1st reported Microscopy result to be Positive
			 */
			if(((MicroscopyResult)val[5]).isPositive()) {	
				if(tbcase.getInfectionSite()!=InfectionSite.EXTRAPULMONARY) {
					if (tbcase.getPatientType()==PatientType.NEW) {
							if(tbcase.getPatient().getGender()==Gender.MALE)
								cntNewM++;
						
							if(tbcase.getPatient().getGender()==Gender.FEMALE)
								cntNewF++;
						}
					else {
							if(tbcase.getPatientType() == PatientType.RELAPSE){
								if(tbcase.getPatient().getGender()==Gender.MALE)
									cntRelM++;
								if(tbcase.getPatient().getGender()==Gender.FEMALE)
									cntRelF++;
								}
							
							if(tbcase.getPatientType() == PatientType.FAILURE || tbcase.getPatientType() == PatientType.FAILURE_FT || tbcase.getPatientType() == PatientType.FAILURE_RT){
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
						}	
					}
				
			else if (((MicroscopyResult)val[5]).isNegative()){
				if(tbcase.getInfectionSite()!=InfectionSite.EXTRAPULMONARY){
					if (tbcase.getPatientType()==PatientType.NEW) {
						if(tbcase.getPatient().getGender()==Gender.MALE)
							cntSmearNegM++;	
						if(tbcase.getPatient().getGender()==Gender.FEMALE)
							cntSmearNegF++;
						}
					}	
				}
		}
		
					
		for(Object[] val :obj) {
			if(val[1] == PatientType.NEW && (val[3] == InfectionSite.EXTRAPULMONARY ))   {
				if(val[2]==Gender.MALE)
					cntEPM++;
				if(val[2]==Gender.FEMALE)
					cntEPF++;
				}
						
			if(val[1]==PatientType.OTHER){
				if(val[2]==Gender.MALE)
					cntOtherM++;
				if(val[2]==Gender.FEMALE)
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
		
		addValue(messages.get("Gender.MALE"), messages.get("#"), totM);
		addValue(messages.get("Gender.FEMALE"), messages.get("#"), totF);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("#"), totM + totF);	

	}
	
	@Override
	public String getHQLSelect() {
		String strSel = "";
		if(flag==1)
			strSel = "select c.id, c.patient.gender, c.state, c.outcomeDate, e.dateCollected, e.result ";
		if(flag==2)
			strSel = "select c.id, c.patientType, c.patient.gender, c.infectionSite";
		return strSel;
	}
	
	@Override
	protected String getHQLFrom() {
		String strFrom = "";
		if(flag==1)
			strFrom =  " from ExamMicroscopy e join e.tbcase c ";
		if(flag==2)
			strFrom =  " from TbCase c ";
		return strFrom;
	}
	
	
}
