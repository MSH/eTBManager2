package org.msh.tb.na.entities;

import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.FieldValue;

import javax.persistence.*;
import java.util.Date;

/**
 * Records information about an HIV result during the treatment
 * @author Utkarsh Srivastava
 *
 */
@Entity
@DiscriminatorValue("na")
public class ExamHIV_NA extends ExamHIV {
	private static final long serialVersionUID = 8053860831668724183L;

	@Temporal(TemporalType.DATE)
	private Date resultDate;
	
	private Integer artNumber;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ART_REGIMEN_ID")
	@PropertyLog(messageKey="TbField.ART_REGIMEN")
	private FieldValue artRegimen;	
	
	private Integer viralLoad;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date viralLoadDateRelease;

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

	/**
	 * @return the viralLoad
	 */
	public Integer getViralLoad() {
		return viralLoad;
	}

	/**
	 * @param viralLoad the viralLoad to set
	 */
	public void setViralLoad(Integer viralLoad) {
		this.viralLoad = viralLoad;
	}

	/**
	 * @return the viralLoadDateRelease
	 */
	public Date getViralLoadDateRelease() {
		return viralLoadDateRelease;
	}

	/**
	 * @param viralLoadDateRelease the viralLoadDateRelease to set
	 */
	public void setViralLoadDateRelease(Date viralLoadDateRelease) {
		this.viralLoadDateRelease = viralLoadDateRelease;
	}

	
	
}
