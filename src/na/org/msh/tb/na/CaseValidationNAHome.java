package org.msh.tb.na;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.msh.mdrtb.entities.CaseIssue;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseValidationHome;
import org.msh.tb.misc.SequenceGenerator;

/**
 * Responsible for the validation process in the case, including pending issues and answers
 * @author Utkarsh Srivastava
 *
 */
@Name("caseValidationNAHome")
@Scope(ScopeType.CONVERSATION)
public class CaseValidationNAHome extends CaseValidationHome {

	@In(create=true, required=true)
	private TBUnitCaseNumber tbunitcasenumber;
	
	
	@Override
	public String validate() {
		// TODO Auto-generated method stub
		
		TbCase tbcase = caseHome.getInstance();
		ValidationState vstate = tbcase.getValidationState();
		if ((vstate != ValidationState.WAITING_VALIDATION) && (vstate != ValidationState.PENDING_ANSWERED))
			return "error";

		tbcase.setValidationState(ValidationState.VALIDATED);

		Patient p = tbcase.getPatient();
		if (p.getRecordNumber() == null) {
			int val = sequenceGenerator.generateNewNumber(caseGenId);
			p.setRecordNumber(val);
		}
				
		// generate new case number
		Integer caseNum = (Integer)entityManager.createQuery("select max(c.caseNumber) from TbCase c where c.patient.id = :id and c.diagnosisDate < :dt")
			.setParameter("id", p.getId())
			.setParameter("dt", tbcase.getDiagnosisDate())
			.getSingleResult();
		
		if (caseNum == null)
			caseNum = 1;
		else caseNum++;
		
		tbcase.setCaseNumber(caseNum);
		
		// update case numbers with notification date after this case
		entityManager.createQuery("update TbCase c " +
				"set c.caseNumber = c.caseNumber + 1 " + 
				"where c.patient.id = :id and c.diagnosisDate > :dt")
				.setParameter("id", p.getId())
				.setParameter("dt", tbcase.getDiagnosisDate())
				.executeUpdate();
		
		entityManager.persist(tbcase);
		entityManager.flush();

		Events.instance().raiseEvent("case.validate");
		
		if(tbunitcasenumber.gernerateTBUnitCaseNumber().equals("error"))
				return "error";
		else	
			return "validated";
	}


	

	




	
}
