package org.msh.tb.reports;

import java.util.Date;

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
	private String keyword;
	
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
		// TODO Auto-generated method stub
		return "from ErrorLog".concat(getCondition());
	}

	
	
	/**
	 * Return static condition
	 * @return
	 */
	protected String getCondition() {
		if (keyword != null) {
			return " where (exceptionClass like #{errorLogReport.keywordLike} or " +
					"exceptionMessage like #{errorLogReport.keywordLike} or " +
					"stackTrace like #{errorLogReport.keywordLike})";
		}
		else return "";
	}

	/**
	 * @return
	 */
	public String getKeywordLike() {
		return (keyword != null) && (!keyword.isEmpty()) ? "%" + keyword + "%": null;
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
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getMaxResults()
	 */
	@Override
	public Integer getMaxResults() {
		return 50;
	}
	
}
