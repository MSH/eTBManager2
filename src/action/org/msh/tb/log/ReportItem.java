package org.msh.tb.log;

import java.util.Date;

import org.msh.tb.entities.enums.RoleAction;

public class ReportItem {

	private Integer id;
	private String roleName;
	private RoleAction action;
	private Integer userId;
	private String userName;
	private Integer workspaceId;
	private String workspaceName;
	private Date date;


	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}
	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
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
	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
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
	 * @return the workspaceId
	 */
	public Integer getWorkspaceId() {
		return workspaceId;
	}
	/**
	 * @param workspaceId the workspaceId to set
	 */
	public void setWorkspaceId(Integer workspaceId) {
		this.workspaceId = workspaceId;
	}
	/**
	 * @return the workspaceName
	 */
	public String getWorkspaceName() {
		return workspaceName;
	}
	/**
	 * @param workspaceName the workspaceName to set
	 */
	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
}
