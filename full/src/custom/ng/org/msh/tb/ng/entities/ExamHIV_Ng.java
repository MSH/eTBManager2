package org.msh.tb.ng.entities;

import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.*;
import java.util.Date;


/**
 * @author Ricardo Mem�ria
 *
 * Records information about an HIV result during the treatment
 */
@Entity
@DiscriminatorValue("ng")
@Table(name="examhiv_ng")
public class ExamHIV_Ng extends ExamHIV {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8053860831668724183L;

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
