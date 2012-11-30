package org.msh.tb.transactionlog;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TransactionLog;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.login.UserSession;
import org.msh.tb.reports.ReportSelection;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.EntityQuery;
import org.msh.utils.date.DateUtils;


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
		"log.user.id = #{reportSelection.userLog.id}",
		"log.entityDescription like #{transactionLogReport.searchKeyLike}",
		"log.transactionDate >= #{transactionLogReport.iniDate}",
		"log.transactionDate <= #{transactionLogReport.endDate1}",
		"log.entityId = #{transactionLogReport.entityId}",
		"log.entityClass = #{transactionLogReport.entityClass}",
		"log.unit.id = #{transactionLogReport.tbunitselection.tbunit.id}",
		"log.adminUnit.code like #{transactionLogReport.adminUnitCode}"
	};

	private RoleAction action;
	private ReportSelection reportSelection;

	private String userName;
	private String searchKey;
	
	private Date iniDate;
	private Date endDate;
	private Integer entityId;
	private String entityClass;
	
	private boolean allResults;

	
	/**
	 * Initialize report to display changes in a specific case
	 */
	public void initCaseReport() {
		entityClass = TbCase.class.getSimpleName();

		CaseHome caseHome = (CaseHome)Component.getInstance("caseHome");
		if ((caseHome == null) || (!caseHome.isManaged())) {
			entityId = -1;
			return;
		}
		
		entityId = caseHome.getInstance().getId();
		iniDate = null;
		endDate = null;
		searchKey = null;
		allResults = true;
	}


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
				"join fetch log.role " + getStaticConditions();
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
		return "log.transactionDate desc, log.id desc";
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getMaxResults()
	 */
	@Override
	public Integer getMaxResults() {
		return allResults? null: 40;
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

	public String getAdminUnitCode(){
		return (getTbunitselection().getAdminUnit() != null ? getTbunitselection().getAdminUnit().getCode()+"%" : null);
	}
	/**
	 * @return the iniDate
	 */
	public Date getIniDate() {
		return iniDate;
	}


	/**
	 * @param iniDate the iniDate to set
	 */
	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}


	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}
	
	
	/**
	 * Return the end date plus one day, to return also the dates with time information inside de date selected
	 * @return
	 */
	public Date getEndDate1() {
		return (endDate != null? DateUtils.incDays(endDate, 1): null);
	}


	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}


	/**
	 * @return the entityId
	 */
	public Integer getEntityId() {
		return entityId;
	}


	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}


	/**
	 * @return the entityClass
	 */
	public String getEntityClass() {
		return entityClass;
	}


	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return the allResults
	 */
	public boolean isAllResults() {
		return allResults;
	}

	/**
	 * @param allResults the allResults to set
	 */
	public void setAllResults(boolean allResults) {
		this.allResults = allResults;
	}
	

	public TBUnitSelection getTbunitselection(){
		UserSession us = (UserSession) Component.getInstance("userSession");
		return us.getTbunitselection();
	}
}
