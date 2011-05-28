package org.msh.tb.log;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.TransactionLog;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.reports.ReportSelection;
import org.msh.utils.EntityQuery;


/**
 * Generate transaction log report
 * @author Ricardo Memoria
 *
 */
@Name("transactionLogReport")
@BypassInterceptors
public class TransactionLogReport extends EntityQuery<TransactionLog> {
	private static final long serialVersionUID = -6184135907973355697L;

	private static final RoleAction[] reportActions = {
		RoleAction.NEW,
		RoleAction.EDIT,
		RoleAction.DELETE,
		RoleAction.EXEC
	};
	
	private static final String[] restrictions = {
		"log.action = #{transactionLogReport.action}",
		"log.role.id = #{userRoleHome.instance.id}",
		"log.user.name like #{transactionLogReport.userNameLike}",
		"log.entityDescription like #{transactionLogReport.searchKeyLike}"
	};

	private RoleAction action;
	private ReportSelection reportSelection;

	private String userName;	
	private String searchKey;
	

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	protected String getCountEjbql() {
		return "select count(*) from TransactionLog log " + getStaticConditions();
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from TransactionLog log " +
				"join fetch log.user " +
				"join fetch log.workspace " +
				"join fetch log.role " +
				"left join fetch log.logValues " + getStaticConditions();
	}


	/**
	 * Return static conditions to the query
	 * @return
	 */
	public String getStaticConditions() {
		UserWorkspace userWorkspace = getReportSelection().getUserWorkspace();
		if (userWorkspace == null)
			 return "where log.workspace.id in (select aux.workspace.id from UserWorkspace aux where aux.user.id = #{userLogin.user.id})";
		else return "where log.workspace.id = " + userWorkspace.getWorkspace().getId().toString();
	}

	
	/**
	 * Called when the user workspace is changed in the report selection
	 */
	@Observer(value="report-workspace-changed", create=false)
	public void workspaceChangeListener() {
		refresh();
		setEjbql(null);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getOrder()
	 */
	@Override
	public String getOrder() {
		return "log.transactionDate desc";
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getMaxResults()
	 */
	@Override
	public Integer getMaxResults() {
		return 40;
	}

	/**
	 * @return the action
	 */
	public RoleAction getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(RoleAction action) {
		this.action = action;
	}

	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	public RoleAction[] getRoleActions() {
		return reportActions;
	}

	public ReportSelection getReportSelection() {
		if (reportSelection == null)
			reportSelection = (ReportSelection)Component.getInstance("reportSelection");
		return reportSelection;
	}


	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}


	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}


	/**
	 * @return the searchKey
	 */
	public String getSearchKey() {
		return searchKey;
	}


	/**
	 * @param searchKey the searchKey to set
	 */
	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}
	
	public String getUserNameLike() {
		return (userName != null? "%" + userName + "%" : null);
	}
	
	public String getSearchKeyLike() {
		return (searchKey != null? "%" + searchKey + "%" : null);
	}
}
