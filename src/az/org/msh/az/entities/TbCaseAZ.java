package org.msh.az.entities;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;

import org.msh.mdrtb.entities.FieldValueComponent;
import org.msh.mdrtb.entities.TbCase;

@Entity
public class TbCaseAZ extends TbCase{
	private static final long serialVersionUID = 3151309615247467018L;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="MARITALSTATUS_ID"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="MARITALSTATUS_Complement"))})
	private FieldValueComponent maritalStatus;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="EDUCATIONALDEGREE_ID"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="EDUCATIONALDEGREE_Complement"))})
	private FieldValueComponent educationalDegree; 


	/**
	 * @return the maritalStatus
	 */
	public FieldValueComponent getMaritalStatus() {
		return maritalStatus;
	}

	/**
	 * @param maritalStatus the maritalStatus to set
	 */
	public void setMaritalStatus(FieldValueComponent maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	/**
	 * @return the educationalDegree
	 */
	public FieldValueComponent getEducationalDegree() {
		return educationalDegree;
	}

	/**
	 * @param educationalDegree the educationalDegree to set
	 */
	public void setEducationalDegree(FieldValueComponent educationalDegree) {
		this.educationalDegree = educationalDegree;
	}
}
