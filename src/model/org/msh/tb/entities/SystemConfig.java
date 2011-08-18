package org.msh.tb.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.Messages;

/**
 * Store e-TB Manager configuration information. Id is always = 1
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="systemconfig")
public class SystemConfig {

	@Id
	private Integer id;

	@Column(length=100)
	@NotNull
	private String systemURL;

	@Column(length=100)
	@NotNull
	private String systemMail;
	
	private boolean allowRegPage;
	
	@ManyToOne
	@JoinColumn(name="WORKSPACE_ID")
	private Workspace workspace;
	
	@ManyToOne
	@JoinColumn(name="USERPROFILE_ID")
	private UserProfile userProfile;
	
	@ManyToOne
	@JoinColumn(name="TBUNIT_ID")
	private Tbunit tbunit;
	
	@Column(length=100)
	private String adminMail;


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
	 * @return the systemURL
	 */
	public String getSystemURL() {
		return systemURL;
	}

	/**
	 * @param systemURL the systemURL to set
	 */
	public void setSystemURL(String systemURL) {
		this.systemURL = systemURL;
	}

	/**
	 * @return the systemMail
	 */
	public String getSystemMail() {
		return systemMail;
	}

	/**
	 * @param systemMail the systemMail to set
	 */
	public void setSystemMail(String systemMail) {
		this.systemMail = systemMail;
	}

	/**
	 * @return the allowRegPage
	 */
	public boolean isAllowRegPage() {
		return allowRegPage;
	}

	/**
	 * @param allowRegPage the allowRegPage to set
	 */
	public void setAllowRegPage(boolean allowRegPage) {
		this.allowRegPage = allowRegPage;
	}

	/**
	 * @return the workspace
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * @return the userProfile
	 */
	public UserProfile getUserProfile() {
		return userProfile;
	}

	/**
	 * @param userProfile the userProfile to set
	 */
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	/**
	 * @return the tbunit
	 */
	public Tbunit getTbunit() {
		return tbunit;
	}

	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

	/**
	 * @return the adminMail
	 */
	public String getAdminMail() {
		return adminMail;
	}

	/**
	 * @param adminMail the adminMail to set
	 */
	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}

	@Override
	public String toString() {
		return Messages.instance().get("admin.syssetup");
	}
}
