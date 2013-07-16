/**
 * 
 */
package org.msh.tb.cases;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.application.mail.MailService;
import org.msh.tb.entities.Issue;
import org.msh.tb.entities.IssueFollowup;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.UserWorkspace;

/**
 * Services to handle case issues and its follow-up
 * 
 * @author Ricardo Memoria
 *
 */
@Name("caseIssueHome")
public class CaseIssueHome extends EntityHomeEx<Issue> {
	private static final long serialVersionUID = 5972585923087719473L;

	@In EntityManager entityManager;
	@In CaseHome caseHome;

	public enum Action {
		NEWISSUE, READING_FOLLOWUPS, NEWFOLLOWUP, DEL_ISSUE, DEL_FOLLOWUP, CLOSE_ISSUE, REOPEN_ISSUE; 
	};
	
	private List<Issue> list;
	private Integer count;
	private IssueFollowup followup;
	private Action action;
	private Integer followupId;

	
	/**
	 * Create a new issue for the current case and logged user
	 * @return String value "issue-created" if successfully created 
	 */
	@Restrict("#{s:hasRole('NEW_ISSUE')}")
	@Transactional
	public String createNewIssue() {
		Issue issue = getInstance();
		TbCase tbcase = caseHome.getInstance();
		
		issue.setCreationDate(new Date());
		issue.setTbcase(tbcase);
		issue.setUser(getUser());
		issue.setClosed(false);
		issue.setUser(getUser());
		issue.setUnit(getUserWorkspace().getTbunit());
		
		super.persist();
		caseHome.updateCaseTags();

		action = Action.NEWISSUE;
		return "issue-created";
	}

	
	/**
	 * Return the string to be displayed with the status of the issue 
	 * @param issue
	 * @return String value
	 */
	public String getDisplayStatus(Issue issue) {
		if (issue.isClosed()) {
			return Messages.instance().get("cases.closed");
		}
		
		if (issue.getAnswerDate() == null) {
			return Messages.instance().get("Issue.new");
		}
		
		return Messages.instance().get("Issue.answered");
	}

	
	/** {@inheritDoc}
	 */
	@Override
	@Transactional
	public String remove() {
		if (!isManaged())
			throw new IllegalAccessError("Issue is not informed");

		// check permission
		// is not the owner of the user?
		if (isAllowedToCloseOrDelete(getInstance())) {
			throw new SecurityException("User is not authorized to delete this issue");
		}

		return super.remove();
	}

	/**
	 * Post a new follow up for the given issue using the information
	 * in the {@link CaseIssueHome#followup} variable
	 * @return true if successfully saved
	 */
	public boolean postFollowup() {
		if (followup == null)
			return false;
		
		if (!isAllowedToAnswer(getInstance()))
			throw new SecurityException("You don't have permission to post an answer");

		// prepare and save the follow up
		followup.setFollowupDate(new Date());
		followup.setIssue(getInstance());
		followup.setUser(getUser());
		followup.setUnit(getUserWorkspace().getTbunit());
		entityManager.persist(followup);

		// save the follow up date
		getInstance().setAnswerDate(followup.getFollowupDate());
		persist();
		caseHome.updateCaseTags();

		action = Action.NEWFOLLOWUP;
		return true;
	}


	/**
	 * Notify a new issue by e-mail to the users in charge of responding it
	 */
	protected void notifyIssueByEmail(){
		TbCase tbcase = caseHome.getInstance();
		Issue issue = getInstance();
				
		MailService srv = MailService.instance();
		
		//Verificar a melhor forma de selecionar os usuarios
		List<UserWorkspace> users = (List<UserWorkspace>) entityManager.createQuery("from UserWorkspace uw where uw.tbunit.id = :id")
											.setParameter("id", tbcase.getOwnerUnit().getId())
											.getResultList();
		
		for (UserWorkspace userW: users) {
			srv.addComponent("user", userW.getUser());
			srv.addComponent("issue", issue);
			srv.addComponent("tbcase", tbcase);
			srv.addComponent("isAnswer", issue.isClosed());
			srv.addMessageToQueue("/mail/casetransfered.xhtml", userW.getUser().getTimeZone(), userW.getUser().getLanguage(), userW.getUser(), true);
		}
		
		srv.dispatchQueue();
	}


	/**
	 * Close the current issue
	 */
	@Transactional
	public void closeIssue() {
		if ((!isManaged()) || (getInstance().isClosed()))
			throw new IllegalAccessError("Issue is not informed or it's already closed: " + getId());

		// check permission
		// is not the owner of the user?
		if (!isAllowedToCloseOrDelete(getInstance())) {
			throw new SecurityException("User is not authorized to close this issue");
		}

		getInstance().setClosed(true);
		persist();
		caseHome.updateCaseTags();
		
		action = Action.CLOSE_ISSUE;
	}

	
	/**
	 * Check if user is allowed to close the given issue
	 * @param issue instance of the {@link Issue} class
	 * @return true if user can close the issue
	 */
	public boolean isAllowedToCloseOrDelete(Issue issue) {
		if (issue == null)
			return false;
		// is not the owner of the user?
		if (getUser().getId().equals(issue.getUser().getId()))
			return true;
		return Identity.instance().hasRole("CLOSEDEL_ISSUE");
	}
	
	/**
	 * Check if user is allowed to close the given issue
	 * @param issue instance of the {@link Issue} class
	 * @return true if user can close the issue
	 */
	public boolean isAllowedToAnswer(Issue issue) {
		if (issue == null)
			return false;
		// is not the owner of the user?
		if (getUser().getId().equals(issue.getUser().getId()))
			return true;
		return Identity.instance().hasRole("ANSWER_ISSUE");
	}

	
	/**
	 * Reopen a closed issue
	 */
	@Transactional
	public void reopenIssue() {
		if ((!isManaged()) || (!getInstance().isClosed()))
			throw new IllegalAccessError("Issue is not informed or it's already open: " + getId());

		// check permission
		// is not the owner of the user?
		if (!isAllowedToCloseOrDelete(getInstance())) {
			throw new SecurityException("User is not authorized to open this issue");
		}

		getInstance().setClosed(false);
		persist();
		caseHome.updateCaseTags();
		
		action = Action.REOPEN_ISSUE;
	}
	
	/**
	 * Return the number of open issues for a given case
	 * @return integer value
	 */
	public int getOpenIssuesCount() {
		if (count == null) {
			count = ((Long)entityManager
					.createQuery("select count(*) from Issue where tbcase.id = #{caseHome.id}")
					.getSingleResult()).intValue();
		}

		return count;
	}

	
	/**
	 * Called to indicate that the follow-ups of the current issue will be rendered in the client side
	 */
	public void loadFollowups() {
		action = Action.READING_FOLLOWUPS;
	}

	
	
	/**
	 * Return the instance of the issue follow up being edited
	 * @return instance of the {@link IssueFollowup} class
	 */
	public IssueFollowup getFollowup() {
		if (followup == null)
			followup = new IssueFollowup();
		return followup;
	}

	/**
	 * Return list of issues of a case
	 * @return list of objects of class {@link Issue}
	 */
	public List<Issue> getList() {
		if (list == null)
			createList();
		return list;
	}

	/**
	 * Create the list of issues of a given case 
	 */
	protected void createList() {
		list = entityManager.createQuery("from Issue where tbcase.id = #{caseHome.id} order by creationDate")
			.getResultList();
	}


	/**
	 * Delete the follow-up specified by the <code>followupId</code> property 
	 */
	public void deleteFollowup() {
		if (followupId == null)
			return;
		followup = entityManager.find(IssueFollowup.class, followupId);
		entityManager.remove(followup);
		caseHome.updateCaseTags();

		action = Action.DEL_FOLLOWUP;
	}

	
	/**
	 * Delete the current issue
	 */
	public void deleteIssue() {
		super.remove();
		
		action = Action.DEL_ISSUE;
	}
	
	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}


	/**
	 * @return the followupId
	 */
	public Integer getFollowupId() {
		return followupId;
	}


	/**
	 * @param followupId the followupId to set
	 */
	public void setFollowupId(Integer followupId) {
		this.followupId = followupId;
	}
}
