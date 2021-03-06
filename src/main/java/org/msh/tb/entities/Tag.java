package org.msh.tb.entities;

import org.hibernate.validator.NotNull;
import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Store information about a tag. A tag may be assigned to cases. This
 * assignment may be done automatically or manually according to the
 * content of the sqlCondition - If null, the tag is assigned manually
 * by a user to a case, if not null, so the tag is maintained by
 * the system. Manual tags are represented by the blue tag.
 * <p/>
 * The <code>consistencyCheck</code> field is used only in automatic
 * tags and indicate if the tag is used for checking validation
 * problems (red tag) or if this is just for grouping cases (green tag) 
 * 
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="tag")
public class Tag extends WSObject implements Serializable, SyncKey {
	private static final long serialVersionUID = 7625442925460611740L;

	public enum TagType { MANUAL, AUTOGEN, AUTOGEN_CONSISTENCY	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@Column(name="tag_name", length=100)
	@NotNull
	@PropertyLog(messageKey="form.name")
	private String name;
	
	@Lob
	private String sqlCondition; 
	
	private boolean consistencyCheck;
	
	@PropertyLog(messageKey="UserState.ACTIVE")
	private boolean active;
	
	private boolean dailyUpdate;

	@PropertyLog(messageKey = "form.displayorder")
	private Integer displayOrder;

	@PropertyLog(messageKey = "form.summary")
	private boolean summary;

	@Transient
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	private Integer clientId;

	@Override
	public Integer getClientId() {
		return clientId;
	}

	@Override
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	/**
	 * Return true if tag is auto generated and maintained by the system
	 * @return
	 */
	public boolean isAutogenerated() {
		return (sqlCondition != null) && (!sqlCondition.isEmpty());
	}
	

	/**
	 * Return the tag type
	 * @return
	 */
	public TagType getType() {
		if (!isAutogenerated()) 
			return TagType.MANUAL;
		
		return (consistencyCheck? TagType.AUTOGEN_CONSISTENCY: TagType.AUTOGEN);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the sqlCondition
	 */
	public String getSqlCondition() {
		return sqlCondition;
	}

	/**
	 * @param sqlCondition the sqlCondition to set
	 */
	public void setSqlCondition(String sqlCondition) {
		this.sqlCondition = sqlCondition;
	}

	/**
	 * @return the consistencyCheck
	 */
	public boolean isConsistencyCheck() {
		return consistencyCheck;
	}

	/**
	 * @param consistencyCheck the consistencyCheck to set
	 */
	public void setConsistencyCheck(boolean consistencyCheck) {
		this.consistencyCheck = consistencyCheck;
	}


	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * @return the dailyUpdate
	 */
	public boolean isDailyUpdate() {
		return dailyUpdate;
	}


	/**
	 * @param dailyUpdate the dailyUpdate to set
	 */
	public void setDailyUpdate(boolean dailyUpdate) {
		this.dailyUpdate = dailyUpdate;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public boolean isSummary() {
		return summary;
	}

	public void setSummary(boolean summary) {
		this.summary = summary;
	}
}
