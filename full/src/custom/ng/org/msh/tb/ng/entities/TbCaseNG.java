package org.msh.tb.ng.entities;

import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.TbCase;
import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tbcaseng")
public class TbCaseNG extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2227321155884824528L;
	
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase")
	@PropertyLog(ignore=true)
	private List<CaseDispensing_Ng> dispng = new ArrayList<CaseDispensing_Ng>();
	
	@Column(length=20)	
	private String tbRegistrationNumber;
	
	@Column(length=100)
	private String emailAddress;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUSPECT_TYPE")
	@PropertyLog(messageKey="TbField.SUSPECT_TYPE")
	private FieldValue suspectType;

	@Embedded
	@AssociationOverrides({ @AssociationOverride(name = "value", joinColumns = @JoinColumn(name = "SOURCEREFERRAL_ID")) })
	@AttributeOverrides({ @AttributeOverride(name = "complement", column = @Column(name = "otherSourceReferral")) })
	private FieldValueComponent sourceReferral;
	
	public List<CaseDispensing_Ng> getDispng() {
		return dispng;
	}

	public void setDispng(List<CaseDispensing_Ng> dispng) {
		this.dispng = dispng;
	}

	public String getTbRegistrationNumber() {
		return tbRegistrationNumber;
	}
	public void setTbRegistrationNumber(String tbRegistrationNumber) {
		this.tbRegistrationNumber = tbRegistrationNumber;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public FieldValue getSuspectType() {
		return suspectType;
	}
	
	public void setSuspectType(FieldValue suspectType) {
		this.suspectType = suspectType;
	}

	public FieldValueComponent getSourceReferral() {
		if (sourceReferral == null) {
			sourceReferral = new FieldValueComponent();
		}
		return sourceReferral;
	}

	public void setSourceReferral(FieldValueComponent sourceReferral) {
		this.sourceReferral = sourceReferral;
	}
}
