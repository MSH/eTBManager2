package org.msh.tb.reports;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ErrorLog;
import org.msh.utils.EntityQuery;

@Name("errorLogReport")
public class ErrorLogReport extends EntityQuery<ErrorLog> {
	private static final long serialVersionUID = 6619760642121954943L;

	private Date iniDate;
	private Date endDate;
	private String userName;
	private String workspace;
	private String searchkey;
	
	private ErrorLog errorLog;
	
	static final private String restrictions[] = {
		"errorDate >= #{errorLogReport.iniDate}",
		"errorDate <= #{errorLogReport.endDate}",
	};
	
	
	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	protected String getCountEjbql() {
		return "select count(*) from ErrorLog".concat(getCondition());
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from ErrorLog".concat(getCondition());
	}

	
	
	/**
	 * Return static condition
	 * @return
	 */
	protected String getCondition() {
		if ((searchkey != null) && (!searchkey.isEmpty())) {
			return " where (exceptionClass like #{errorLogReport.searchkeyLike} or " +
					"exceptionMessage like #{errorLogReport.searchkeyLike} or " +
					"stackTrace like #{errorLogReport.searchkeyLike} or " +
					"user like #{errorLogReport.searchkeyLike})";
		}
		else return "";
	}

	/**
	 * @return
	 */
	public String getSearchkeyLike() {
		return (searchkey != null) && (!searchkey.isEmpty()) ? "%" + searchkey + "%": null;
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
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
	 * @return the workspace
	 */
	public String getWorkspace() {
		return workspace;
	}

	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	/**
	 * @return the searchkey
	 */
	public String getSearchkey() {
		return searchkey;
	}

	/**
	 * @param searchkey the searchkey to set
	 */
	public void setSearchkey(String searchkey) {
		this.searchkey = searchkey;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getMaxResults()
	 */
	@Override
	public Integer getMaxResults() {
		return 50;
	}

	

	public ErrorLog getErrorLog() {
		return errorLog;
	}
	
	public void setErrorLogId(Integer id) {
		if (id == null)
			 errorLog = null;
		else errorLog = getEntityManager().find(ErrorLog.class, id);
	}
	
	public Integer getErrorLogId() {
		return (errorLog != null? errorLog.getId(): null);
	}

	/* (non-Javadoc)
	 * @see org.msh.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getOrder()
	 */
	@Override
	public String getOrder() {
		return "errorDate desc";
	}
}
