package org.msh.tb.na;


import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.log.LogInfo;
import org.msh.tb.na.entities.ExamHIV_NA;

@Name("examHIVHomeNA")
@LogInfo(roleName="EXAM_HIV")
public class ExamHIVHome extends ExamHome<ExamHIV_NA> {
	private static final long serialVersionUID = 5431512237255765820L;
	@In(create=true)
	private EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	

	@Factory("examHIVNA")
	public ExamHIV_NA getExamHIV_NA() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		// TODO Auto-generated method stub
		ExamHIV_NA examHivNa = getInstance();
		
		examHivNa.setTbcase(caseHome.getInstance());
		if(examHivNa.getResult().toString()=="NEGATIVE"){
			examHivNa.setCd4Count(null);
			examHivNa.setCd4StDate(null);
			examHivNa.setARTstarted(false);
			examHivNa.setStartedARTdate(null);
			examHivNa.setArtRegimen(null);
			examHivNa.setCPTstarted(false);
			examHivNa.setStartedCPTdate(null);	
		}
		entityManager.persist(examHivNa);
		return "persisted";
	}
	
}
