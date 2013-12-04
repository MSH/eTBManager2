package org.msh.tb.ua.entities;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.PatientType;

@Entity
@Table(name="prevtbtreatmentua")
public class PrevTBTreatmentUA extends PrevTBTreatment{
	private static final long serialVersionUID = -7844708891730869765L;

	@Temporal(TemporalType.DATE) private Date registrationDate;
	private String registrationCode;
	private PatientType patientType;
	private boolean refuse2line;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="prevtreat_id")
	private TbCase prevTreatCase ;
	
	public Date getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}
	public String getRegistrationCode() {
		return registrationCode;
	}
	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}
	public PatientType getPatientType() {
		return patientType;
	}
	public void setPatientType(PatientType patientType) {
		this.patientType = patientType;
	}
	public boolean isRefuse2line() {
		return refuse2line;
	}
	public void setRefuse2line(boolean refuse2line) {
		this.refuse2line = refuse2line;
	}
	public void setPrevTreatCase(TbCase prevTreatCase) {
		this.prevTreatCase = prevTreatCase;
	}
	public TbCase getPrevTreatCase() {
		return prevTreatCase;
	}


}
