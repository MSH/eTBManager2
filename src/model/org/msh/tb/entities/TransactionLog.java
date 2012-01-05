package org.msh.tb.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.utils.TextUtils;

/**
 * Register a transaction that happened in the system
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="transactionlog")
public class TransactionLog {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ROLE_ID")
	@NotNull
	private UserRole role;
	
	@NotNull
	private RoleAction action;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USERLOG_ID")
	@NotNull
	private UserLog user;
	
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	private Date transactionDate;
	
	private Integer entityId;
	
	@Column(length=100)
	private String entityDescription;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="WORKSPACELOG_ID")
	@NotNull
	private WorkspaceLog workspace;
	
	
	@Lob
	private String comments;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ADMINUNIT_ID")
	private AdministrativeUnit adminUnit;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UNIT_ID")
	private Tbunit unit;

	// suffix information to be displayed after the title
	@Column(length=200)
	private String titleSuffix;
	
	/**
	 * Return the display text representation of the log service
	 * @return
	 */
	public String getDisplayText() {
		String s = (role != null? role.getDisplayName(): super.toString());

		return s;
	}

	
	/**
	 * Return the title to be displayed in a transaction log report
	 * @return
	 */
	public String getTitle() {
		String s;

		if ((action != null) && (action != RoleAction.EXEC))
			 s = Messages.instance().get(action.getKey()) + " - ";
		else s = "";

		s += getDisplayText();

		String suf = getDisplayableTitleSuffix();
		if (suf != null)
			s += " - " + suf;

		return s;
	}

	/**
	 * Return the display representation of the suffix to be appended to the title
	 * @return
	 */
	public String getDisplayableTitleSuffix() {
		if ((titleSuffix == null) || (titleSuffix.isEmpty()))
			return null;
		
		return TextUtils.eltokenFormat(titleSuffix, new TextUtils.TokenInterpreter() {
			@Override
			public String translate(String token) {
				return Messages.instance().get(token);
			}
		});
	}
	
	@Override
	public String toString() {
		return getDisplayText();
	}
	
	
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
	 * @return the role
	 */
	public UserRole getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(UserRole role) {
		this.role = role;
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
	 * @return the user
	 */
	public UserLog getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(UserLog user) {
		this.user = user;
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
	 * @return the entityDescription
	 */
	public String getEntityDescription() {
		return entityDescription;
	}

	/**
	 * @param entityDescription the entityDescription to set
	 */
	public void setEntityDescription(String entityDescription) {
		this.entityDescription = entityDescription;
	}

	/**
	 * @param transactionDate the transactionDate to set
	 */
	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	/**
	 * @return the transactionDate
	 */
	public Date getTransactionDate() {
		return transactionDate;
	}

	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(WorkspaceLog workspace) {
		this.workspace = workspace;
	}

	/**
	 * @return the workspace
	 */
	public WorkspaceLog getWorkspace() {
		return workspace;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the adminUnit
	 */
	public AdministrativeUnit getAdminUnit() {
		return adminUnit;
	}

	/**
	 * @param adminUnit the adminUnit to set
	 */
	public void setAdminUnit(AdministrativeUnit adminUnit) {
		this.adminUnit = adminUnit;
	}

	/**
	 * @return the unit
	 */
	public Tbunit getUnit() {
		return unit;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(Tbunit unit) {
		this.unit = unit;
	}

	/**
	 * @return the titleSuffix
	 */
	public String getTitleSuffix() {
		return titleSuffix;
	}

	/**
	 * @param titleSuffix the titleSuffix to set
	 */
	public void setTitleSuffix(String titleSuffix) {
		this.titleSuffix = titleSuffix;
	}
}
