package org.msh.tb.na.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.CaseData;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.HIVResultKe;
import org.msh.tb.log.FieldLog;

/**
 * @author Utkarsh Srivastava
 *
 * Records information about an HIV result during the treatment
 */
@Entity
public class ExamHIV_NA extends CaseData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8053860831668724183L;

	private HIVResult result;
	
	@Temporal(TemporalType.DATE)
	private Date resultDate;
	
	@Temporal(TemporalType.DATE)
	private Date startedARTdate;
	
	@Temporal(TemporalType.DATE)
	private Date startedCPTdate;
	
	@Column(length=100)
	private String laboratory;
	
	private Integer cd4Count;	
	
	@Temporal(TemporalType.DATE)
	private Date cd4StDate;

	private Integer artNumber;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ART_REGIMEN_ID")
	@FieldLog(key="TbField.ART_REGIMEN")
	private FieldValue artRegimen;	
	
	
	public boolean isARTstarted() {
		return startedARTdate != null;
	}
	
	public boolean isCPTstarted() {
		return startedCPTdate != null;
	}
	
	public void setCPTstarted(boolean value) {
		if (!value)
			startedCPTdate = null;
	}
	
	public void setARTstarted(boolean value) {
		if (!value)
			startedARTdate = null;
	}
	
	public String getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(String laboratory) {
		this.laboratory = laboratory;
	}

	public HIVResult getResult() {
		return result;
	}

	public void setResult(HIVResult result) {
		this.result = result;
	}

	public Date getResultDate() {
		return resultDate;
	}

	public void setResultDate(Date resultDate) {
		this.resultDate = resultDate;
	}

	public Date getStartedARTdate() {
		return startedARTdate;
	}

	public void setStartedARTdate(Date startedARTdate) {
		this.startedARTdate = startedARTdate;
	}

	public Date getStartedCPTdate() {
		return startedCPTdate;
	}

	public void setStartedCPTdate(Date startedCPTdate) {
		this.startedCPTdate = startedCPTdate;
	}
	
	public Integer getCd4Count() {
		return cd4Count;
	}

	public void setCd4Count(Integer cd4Count) {
		this.cd4Count = cd4Count;
	}

	public Date getCd4StDate() {
		return cd4StDate;
	}

	public void setCd4StDate(Date cd4StDate) {
		this.cd4StDate = cd4StDate;
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
