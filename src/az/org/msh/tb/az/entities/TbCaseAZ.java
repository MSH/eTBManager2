package org.msh.tb.az.entities;

import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;

import org.msh.tb.az.entities.enums.CaseFindingStrategy;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.TbCase;

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

	private CaseFindingStrategy caseFindingStrategy;
	
	private Integer numberOfImprisonments;
	
	private Date inprisonIniDate;
	
	private Date inprisonEndDate;


	/**
	 * @return the maritalStatus
	 */
	public FieldValueComponent getMaritalStatus() {
		if (maritalStatus == null)
			maritalStatus = new FieldValueComponent();
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
		if (educationalDegree == null)
			educationalDegree = new FieldValueComponent();
		return educationalDegree;
	}

	/**
	 * @param educationalDegree the educationalDegree to set
	 */
	public void setEducationalDegree(FieldValueComponent educationalDegree) {
		this.educationalDegree = educationalDegree;
	}

	/**
	 * @return the caseFindingStrategy
	 */
	public CaseFindingStrategy getCaseFindingStrategy() {
		return caseFindingStrategy;
	}

	/**
	 * @param caseFindingStrategy the caseFindingStrategy to set
	 */
	public void setCaseFindingStrategy(CaseFindingStrategy caseFindingStrategy) {
		this.caseFindingStrategy = caseFindingStrategy;
	}

	/**
	 * @return the numberOfImprisonments
	 */
	public Integer getNumberOfImprisonments() {
		return numberOfImprisonments;
	}

	/**
	 * @param numberOfImprisonments the numberOfImprisonments to set
	 */
	public void setNumberOfImprisonments(Integer numberOfImprisonments) {
		this.numberOfImprisonments = numberOfImprisonments;
	}

	/**
	 * @return the inprisonIniDate
	 */
	public Date getInprisonIniDate() {
		return inprisonIniDate;
	}

	/**
	 * @param inprisonIniDate the inprisonIniDate to set
	 */
	public void setInprisonIniDate(Date inprisonIniDate) {
		this.inprisonIniDate = inprisonIniDate;
	}

	/**
	 * @return the inprisionEndDate
	 */
	public Date getInprisonEndDate() {
		return inprisonEndDate;
	}

	/**
	 * @param inprisionEndDate the inprisionEndDate to set
	 */
	public void setInprisonEndDate(Date inprisionEndDate) {
		this.inprisonEndDate = inprisionEndDate;
	}
}
