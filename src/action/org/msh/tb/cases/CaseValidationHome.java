package org.msh.tb.cases;

import java.util.ArrayList;
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
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.CaseIssue;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.misc.SequenceGenerator;

/**
 * Responsible for the validation process in the case, including pending issues and answers
 * @author Ricardo Memoria
 *
 */
@Name("caseValidationHome")
@Scope(ScopeType.CONVERSATION)
public class CaseValidationHome {

	public static final String caseGenId = "CASE_NUMBER";

	@In(required=true)
	protected CaseHome caseHome;
	@In
	protected EntityManager entityManager;
	@In(create=true)
	protected SequenceGenerator sequenceGenerator;
	@In(create=true) FacesMessages facesMessages;
	
	private List<CaseIssue> issues;
	private CaseIssue issue;
	private User user;
	private CaseIssue lastIssue;
	private boolean displayingIssues;
	private String lastIssueEditingText;


	/**
	 * Return list of issues
	 * @return
	 */
	public List<CaseIssue> getIssues() {
		if (issues == null)
			createIssueList();
		return issues;
	}


	/**
	 * Create list of issues of the case
	 */
	protected void createIssueList(){	
		issues = entityManager.createQuery("from CaseIssue c " +
				"join fetch c.user " +
				"where c.tbcase.id = #{caseHome.id} " +
				"order by c.date desc")
				.getResultList(); 
	}

	
	public CaseIssue getIssue() {
		if (issue == null)
			issue = new CaseIssue();
		return issue;
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

		Patient p = tbcase.getPatient();
		if (p.getRecordNumber() == null) {
			int val = sequenceGenerator.generateNewNumber(caseGenId);
			p.setRecordNumber(val);
		}
				
		// generate new case number
		//Gets the major case number of this patient with diagnosisdate before the case in question.
		Integer caseNum = (Integer)entityManager.createQuery("select max(c.caseNumber) from TbCase c where c.patient.id = :id and c.diagnosisDate < :dt")
			.setParameter("id", p.getId())
			.setParameter("dt", tbcase.getDiagnosisDate())
			.getSingleResult();
		
		if (caseNum == null)
			//If there is no casenum before this one it sets the digit 1 for this case.
			caseNum = 1;
		else{
			//If there is a casenum before this case it sets this number plus one.
			caseNum++;
		}
		
		// Returns the ids of the cases cronologicaly after the case in memory
		ArrayList<Integer> lst = (ArrayList<Integer>) entityManager.createQuery("select id from TbCase c " + 
		        "where c.patient.id = :id and c.diagnosisDate > :dt order by c.diagnosisDate")
		        .setParameter("dt", tbcase.getDiagnosisDate())
		        .setParameter("id", p.getId())
		        .getResultList();
		
		// updates caseNums according to the cronological order
		int num = caseNum + 1;
		for (Integer id: lst) {
		   entityManager.createQuery("update TbCase set caseNumber = :num where id=:id")
		      .setParameter("num", num)
		      .setParameter("id", id)
		      .executeUpdate();
		   num++;
		}
		
		tbcase.setCaseNumber(caseNum);
		

		
		entityManager.persist(tbcase);
		entityManager.flush();

		Events.instance().raiseEvent("case.validate");
		caseHome.updateCaseTags();
		
		return "validated";
	}


	/**
	 * Register a new pending issue to the case
	 * @return "pending-registered" if successfully registered
	 */
	@Transactional
	public String registerPending() {
		issue = prepareNewIssue();
		issue.setAnswer(false);
		
		entityManager.persist(issue);
		entityManager.flush();
		
		TbCase tbcase = caseHome.getInstance();
		if (tbcase.getValidationState() != ValidationState.PENDING) {
			tbcase.setValidationState(ValidationState.PENDING);
		}

		tbcase.incIssueCounter();
		
		caseHome.persist();
		caseHome.updateCaseTags();
		
		return "pending-registered";
	}
	

	/**
	 * Register a new answer to the case
	 * @return "answered" if successfully answered
	 */
	@Transactional
	public String answer() {
		issue = prepareNewIssue();
		issue.setAnswer(true);
		
		entityManager.persist(issue);
		entityManager.flush();

		TbCase tbcase = caseHome.getInstance();
		tbcase.setValidationState(ValidationState.PENDING_ANSWERED);
		
		caseHome.persist();
		caseHome.updateCaseTags();
		
		return "answered";
	}
	
	/**
	 * Prepare the new issue to be saved
	 * @return
	 */
	protected CaseIssue prepareNewIssue() {
		TbCase tbcase = caseHome.getInstance();
		
		issue.setDate(new Date());
		issue.setTbcase(tbcase);
		issue.setUser(getUser());
		
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		int id = userWorkspace.getTbunit().getId();
		Tbunit unit = entityManager.find(Tbunit.class, id);
		issue.setTbunit(unit);
		
		return issue;
	}
	
	public void saveEditingLastIssue(){
		issue = getLast();
		UserLogin ul = (UserLogin) Component.getInstance("userLogin");
		
		if(issue != null){
			if(!ul.getUser().getId().equals(issue.getUser().getId())){
				facesMessages.addFromResourceBundle("exceptions.val");
				return;
			}
			
			issue.setDescription(lastIssueEditingText);
			
			entityManager.persist(issue);
			entityManager.flush();
			
			issue = null;
			issues = null;
			
			facesMessages.addFromResourceBundle("default.entity_updated");
		}
		
	}
	
	public void removeLastIssue(){
		issue = getLast();
		UserLogin ul = (UserLogin) Component.getInstance("userLogin");
		
		if(issue != null){	
			if(!ul.getUser().getId().equals(issue.getUser().getId())){
				facesMessages.addFromResourceBundle("exceptions.val");
				return;
			}
		
			entityManager.createQuery("delete from CaseIssue c " +
					"where c.id = :id")
					.setParameter("id", issue.getId())
					.executeUpdate();
			
			issue = getLast();
			if(issue == null)
				caseHome.getTbCase().setValidationState(ValidationState.WAITING_VALIDATION);		
			else if(issue.isAnswer())
				caseHome.getTbCase().setValidationState(ValidationState.PENDING_ANSWERED);
			else
				caseHome.getTbCase().setValidationState(ValidationState.PENDING);
			
			caseHome.persist();
			
			facesMessages.clear();
			facesMessages.addFromResourceBundle("default.entity_deleted");
		}
		
		issue = null;
		issues = null;
	}

	/**
	 * Return the current user logged in
	 * @return
	 */
	protected User getUser() {
		if (user == null) {
			user = ((UserLogin)Component.getInstance("userLogin")).getUser();
			// doesn't merge it because there is a long line of objects to be merged with the user logged in
			user = entityManager.find(User.class, user.getId());
		}
		return user;
	}

	
	/**
	 * Return the last issue posted for the case
	 * @return
	 */
	public CaseIssue getLastIssue() {
		try {
			if (lastIssue == null) {
				lastIssue = (CaseIssue)entityManager.createQuery("from CaseIssue c " +
						"join fetch c.user " +
						"where c.tbcase.id = :id and c.answer = false " +
						"and c.date = (select max(aux.date) from CaseIssue aux " +
						"where aux.tbcase.id=c.tbcase.id and aux.answer=false)")
						.setParameter("id", caseHome.getId())
						.getSingleResult();
			}
			return lastIssue;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Return the last posted for the case
	 * @return
	 */
	public CaseIssue getLast() {
		try {
			CaseIssue lastCi = (CaseIssue) entityManager.createQuery("from CaseIssue c " +
					"join fetch c.user " +
					"where c.tbcase.id = :id " +
					"and c.date = (select max(aux.date) from CaseIssue aux " +
					"where aux.tbcase.id=c.tbcase.id)")
					.setParameter("id", caseHome.getId())
					.getSingleResult();
			
			return lastCi;
		} catch (NoResultException e) {
			return null;
		}
	}


	/**
	 * Just to flag JSF that the issues panel must be rendered
	 */
	public void displayIssues() {
		displayingIssues = true;
	}


	/**
	 * Return if the issues are to be displayed 
	 * @return
	 */
	public boolean isDisplayingIssues() {
		return displayingIssues;
	}

	/**
	 * @return the lastIssueEditingText
	 */
	public String getLastIssueEditingText() {
		return lastIssueEditingText;
	}


	/**
	 * @param lastIssueEditingText the lastIssueEditingText to set
	 */
	public void setLastIssueEditingText(String lastIssueEditingText) {
		this.lastIssueEditingText = lastIssueEditingText;
	}
	
}
