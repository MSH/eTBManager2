package org.msh.tb.ng.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.transactionlog.PropertyLog;

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

}
