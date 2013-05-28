package org.msh.tb.kh.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.transactionlog.PropertyLog;
/**
 * @author Vani Rao
 *
 * Records information about Culture result during the treatment
 */
@Entity
@Table(name="examculture_kh")
public class ExamCulture_Kh extends ExamCulture{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2088191345814498430L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDENTIFICATION_ID")
	@PropertyLog(messageKey="TbField.IDENTIFICATION")
	private FieldValue identification;
	
	
	public FieldValue getIdentification() {
		return identification;
	}
	
	public void setIdentification(FieldValue identification) {
		this.identification = identification;
	}
}
