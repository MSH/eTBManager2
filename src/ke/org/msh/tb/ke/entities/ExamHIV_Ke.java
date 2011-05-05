package org.msh.tb.ke.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.CaseData;
import org.msh.tb.entities.enums.HIVResultKe;

/**
 * @author Ricardo Memória
 *
 * Records information about an HIV result during the treatment
 */
@Entity
public class ExamHIV_Ke extends CaseData implements Serializable {
	private static final long serialVersionUID = 2237957846637585494L;

	private HIVResultKe result;
	
	@Temporal(TemporalType.DATE)
	private Date resultDate;
	
	@Temporal(TemporalType.DATE)
	private Date startedARTdate;
	
	@Temporal(TemporalType.DATE)
	private Date startedCPTdate;
	
	@Column(length=100)
	private String laboratory;
	
	//usrivast
	//addition for kenya workspace
	private Integer cd4Count;	
	
	@Temporal(TemporalType.DATE)
	private Date cd4StDate;

	private HIVResultKe partnerResult;
	
	@Temporal(TemporalType.DATE)
	private Date partnerResultDate;
	
	public boolean isPartnerPresent() {
		return partnerResultDate != null;
	}
	
	public void setPartnerPresent(boolean value) {
		if (!value)
			partnerResultDate = null;
	}
	
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

	public HIVResultKe getResult() {
		return result;
	}

	public void setResult(HIVResultKe result) {
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

	public HIVResultKe getPartnerResult() {
		return partnerResult;
	}

	public void setPartnerResult(HIVResultKe partnerResult) {
		this.partnerResult = partnerResult;
	}

	public Date getPartnerResultDate() {
		return partnerResultDate;
	}

	public void setPartnerResultDate(Date partnerResultDate) {
		this.partnerResultDate = partnerResultDate;
	}	
	
}
