package org.msh.tb.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.RoleAction;

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
	
	private boolean hasPrevValues;
	private int numValues;

	@OneToMany(mappedBy="transactionLog", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	private List<LogValue> logValues = new ArrayList<LogValue>();

	@Column(length=100)
	private String entityClass;
	
	private CaseClassification caseClassification;
	
	@Lob
	private String comments;

	
	/**
	 * Return the display text representation of the log service
	 * @return
	 */
	public String getDisplayText() {
		String s = (role != null? role.getDisplayName(): super.toString());

		if (caseClassification != null)
			s = Messages.instance().get(caseClassification.getKey()) + " - " + s;
		return s;
	}

	@Override
	public String toString() {
		return getDisplayText();
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
	 * @return the hasPrevValues
	 */
	public boolean isHasPrevValues() {
		return hasPrevValues;
	}

	/**
	 * @param hasPrevValues the hasPrevValues to set
	 */
	public void setHasPrevValues(boolean hasPrevValues) {
		this.hasPrevValues = hasPrevValues;
	}

	/**
	 * @return the logValues
	 */
	public List<LogValue> getLogValues() {
		return logValues;
	}

	/**
	 * @param logValues the logValues to set
	 */
	public void setLogValues(List<LogValue> logValues) {
		this.logValues = logValues;
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
	 * @param numValues the numValues to set
	 */
	public void setNumValues(int numValues) {
		this.numValues = numValues;
	}

	/**
	 * @return the numValues
	 */
	public int getNumValues() {
		return numValues;
	}

	/**
	 * @return the caseClassification
	 */
	public CaseClassification getCaseClassification() {
		return caseClassification;
	}

	/**
	 * @param caseClassification the caseClassification to set
	 */
	public void setCaseClassification(CaseClassification caseClassification) {
		this.caseClassification = caseClassification;
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
}
