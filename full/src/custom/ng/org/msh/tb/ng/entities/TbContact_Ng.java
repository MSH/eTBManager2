package org.msh.tb.ng.entities;

import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbContact;
import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.*;


/**
* @author Vani Rao
*
* Records information about Risk Group in TB Contacts
*/
@Entity
@Table(name="tbcontactng")
public class TbContact_Ng extends TbContact {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5958658778809036421L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="RISK_GROUP")
	@PropertyLog(messageKey="TbField.RISK_GROUP")
	private FieldValue riskGroup;
	
	private String contactLastName;
	
	private String contactOtherName;
	
	public FieldValue getRiskGroup() {
		return riskGroup;
	}
	
	public void setRiskGroup(FieldValue riskGroup) {
		this.riskGroup = riskGroup;
	}

	public void setContactLastName(String contactLastName) {
		this.contactLastName = contactLastName;
	}

	public String getContactLastName() {
		return contactLastName;
	}

	public void setContactOtherName(String contactOtherName) {
		this.contactOtherName = contactOtherName;
	}

	public String getContactOtherName() {
		return contactOtherName;
	}

	
	

}
