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
	};

	private RoleAction action;
	private ReportSelection reportSelection;
	

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
}
