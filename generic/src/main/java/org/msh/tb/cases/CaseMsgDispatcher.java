package org.msh.tb.cases;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.MsgDispatcher;
import org.msh.tb.entities.Issue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;

/**
 * Component responsible to send asynchronous messages to the users based on order events
 * @author Ricardo Memoria
 *
 */
@Name("caseMsgDispatcher")
public class CaseMsgDispatcher extends MsgDispatcher{
	@In EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	
	/**
	 * Send a message to the users notifying about a new case transference
	 */
	//@Observer("case.transferout")
	public void notifyTransference(){		
		TbCase tbcase = caseHome.getInstance();
		Tbunit unitFrom = caseHome.getTransferOutHealthUnit().getTbunit();
		
		List<User> users = getUsersByRoleAndUnit("CASE_TRANSFER", tbcase.getOwnerUnit());
		
		addComponent("unitFrom", unitFrom);
		addComponent("tbcase", tbcase);
		
		sendMessage(users, "/mail/casetransfered.xhtml");
	}


	/**
	 * Send a message to the users notifying about a new pending
	 */
	//@Observer("pending-registered-answered")
	public void notifyNewPending(){
		CaseIssueHome caseIssueHome = (CaseIssueHome) Component.getInstance("caseIssueHome");
		TbCase tbcase = caseHome.getInstance();
		List<User> users;
		
		Issue issue = caseIssueHome.getInstance();
		
		if(!issue.isClosed())
			users = getUsersByRoleAndUnit("CASE_DATA", tbcase.getOwnerUnit());
		else
			users = getUsersByRoleAndUnit("CASE_VALIDATE", tbcase.getOwnerUnit());
		
		addComponent("tbcase", tbcase);
		addComponent("issue", issue);
		
		sendMessage(users, "/mail/caseissue.xhtml");
	}
		
	/**
	 * Return the instance of the CaseMsgDispatcher in the current context
	 * @return
	 */
	public static CaseMsgDispatcher instance() {
		return (CaseMsgDispatcher)Component.getInstance("caseMsgDispatcher");
	}
}
