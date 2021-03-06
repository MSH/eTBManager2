package org.msh.tb.entities;

import org.msh.etbm.commons.transactionlog.Operation;
import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.enums.TbField;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Stores data about a field value from TB forms
 * @author Ricardo Lima
 *
 */
@Entity
@Table(name="fieldvalue")
public class FieldValue extends WSObject implements Serializable, SyncKey {
	private static final long serialVersionUID = -754148519681677704L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@Embedded
	@PropertyLog(messageKey="form.name", operations={Operation.NEW})
	private LocalizedNameComp name = new LocalizedNameComp();

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="name1", column=@Column(name="SHORT_NAME1")),
		@AttributeOverride(name="name2", column=@Column(name="SHORT_NAME2"))})
	@PropertyLog(messageKey="form.abbrevName", operations={Operation.NEW})
	private LocalizedNameComp shortName = new LocalizedNameComp();

	@Column(length=20)
	@PropertyLog(messageKey="form.customId")
	private String customId;
	
	@PropertyLog(messageKey="TbField")
	private TbField field;
	
	private boolean other;
	
	@Column(length=100)
	private String otherDescription;

	@PropertyLog(messageKey="form.displayorder")
	private Integer displayOrder;

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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof FieldValue))
			return false;
		
		return ((FieldValue)obj).getId().equals(getId());
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
	 * @return the name
	 */
	public LocalizedNameComp getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(LocalizedNameComp name) {
		this.name = name;
	}

	/**
	 * @return the shortName
	 */
	public LocalizedNameComp getShortName() {
		return shortName;
	}

	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(LocalizedNameComp shortName) {
		this.shortName = shortName;
	}

	/**
	 * @param customId the customId to set
	 */
	public void setCustomId(String customId) {
		this.customId = customId;
	}

	/**
	 * @return the customId
	 */
	public String getCustomId() {
		return customId;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(TbField field) {
		this.field = field;
	}

	/**
	 * @return the field
	 */
	public TbField getField() {
		return field;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (getName() != null? name.toString(): super.toString());
	}

	/**
	 * @return the other
	 */
	public boolean isOther() {
		return other;
	}

	/**
	 * @param other the other to set
	 */
	public void setOther(boolean other) {
		this.other = other;
	}

	/**
	 * @return the otherDescription
	 */
	public String getOtherDescription() {
		return otherDescription;
	}

	/**
	 * @param otherDescription the otherDescription to set
	 */
	public void setOtherDescription(String otherDescription) {
		this.otherDescription = otherDescription;
	}

	/**
	 * @return the displayOrder
	 */
	public Integer getDisplayOrder() {
		return displayOrder;
	}

	/**
	 * @param displayOrder the displayOrder to set
	 */
	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}
}
