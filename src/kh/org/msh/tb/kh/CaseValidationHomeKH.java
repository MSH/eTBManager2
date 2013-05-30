package org.msh.tb.kh;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseValidationHome;
import org.msh.tb.entities.CaseIssue;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.misc.SequenceGenerator;

@Name("caseValidationHomeKH")
@Scope(ScopeType.CONVERSATION)
public class CaseValidationHomeKH extends CaseValidationHome {


	public void initialize() {
		caseHome.getTbCase().setDiagnosisType(DiagnosisType.CONFIRMED);
	}
	
	/**
	 * Validate a case, generating a new patient number if it was not generated yet
	 * @return
	 */
	@Transactional
	@Restrict("#{caseHome.canValidate}")
	public String validate() {
		TbCase tbcase = caseHome.getInstance();
		ValidationState vstate = tbcase.getValidationState();
		if ((vstate != ValidationState.WAITING_VALIDATION) && (vstate != ValidationState.PENDING_ANSWERED))
			return "error";

		tbcase.setValidationState(ValidationState.VALIDATED);
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);

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
		caseHome.updateCaseTags();
		
		return "validated";
	}

}
