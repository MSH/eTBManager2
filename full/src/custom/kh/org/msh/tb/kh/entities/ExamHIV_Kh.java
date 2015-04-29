package org.msh.tb.kh.entities;
/**
 * @author Vani Rao
 *
 * Records information about an HIV result during the treatment for Cambodian Workspace
 */

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.FieldValue;

import javax.persistence.*;
import java.util.Date;

@Entity
@DiscriminatorValue("kh")
public class ExamHIV_Kh extends ExamHIV{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8884634003605359452L;
	@Temporal(TemporalType.DATE)
	private Date resultDate;
	
	private Integer artNumber;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ART_REGIMEN_ID")
	@PropertyLog(messageKey="TbField.ART_REGIMEN")
	private FieldValue artRegimen;	
	
	public Date getResultDate() {
		return resultDate;
	}

	public void setResultDate(Date resultDate) {
		this.resultDate = resultDate;
	}

	public Integer getArtNumber() {
		return artNumber;
	}

	public void setArtNumber(Integer artNumber) {
		this.artNumber = artNumber;
	}

	public FieldValue getArtRegimen() {
		return artRegimen;
	}

	public void setArtRegimen(FieldValue artRegimen) {
		this.artRegimen = artRegimen;
	}
}
