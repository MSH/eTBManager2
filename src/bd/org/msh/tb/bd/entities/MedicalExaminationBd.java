package org.msh.tb.bd.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.mdrtb.entities.CaseData;
import org.msh.mdrtb.entities.enums.ClinicalEvolution;
import org.msh.mdrtb.entities.enums.MedAppointmentType;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.tb.bd.entities.enums.DotProvider;
import org.msh.tb.bd.entities.enums.ReferredTo;

/**
 *
 * Records information about a medical examination of a case in bangladesh workspace
 *
 * @author Utkarsh Srivastava
 *
 */
@Entity
public class MedicalExaminationBd extends CaseData implements Serializable {
	private static final long serialVersionUID = 2760727118134685773L;

	private Double weight;
	
	private Float height;
	
	private MedAppointmentType appointmentType;

	private YesNoType usingPrescMedicines;
	
	@Column(length=100)
	private String reasonNotUsingPrescMedicines;
	
	@Column(length=100)
	private String responsible;
	
	@Column(length=100)
	private String positionResponsible;

	
	private YesNoType supervisedTreatment;
	
	private ReferredTo patientRefTo;
	
	@Column(length=100)
	private String referredToUnitName;	
	
	@Temporal(TemporalType.DATE)
	@Column(name="REF_TO_DATE")
	private Date refToDate;	
	
	
	private DotProvider dotType;
	
	@Column(length=100)
	private String dotProvName;
	
		
	public YesNoType getSupervisedTreatment() {
		return supervisedTreatment;
	}

	public void setSupervisedTreatment(YesNoType supervisedTreatment) {
		this.supervisedTreatment = supervisedTreatment;
	}

	public ClinicalEvolution getClinicalEvolution() {
		return clinicalEvolution;
	}

	public void setClinicalEvolution(ClinicalEvolution clinicalEvolution) {
		this.clinicalEvolution = clinicalEvolution;
	}

	private ClinicalEvolution clinicalEvolution;

	
	/**
	 * Calculate the BMI (using weight and height)
	 * @return BMI value
	 */
	public double getBMI() {
		if ((height == null) || (height == 0))
			return 0;
		float h = height / 100;
		return (weight == null? 0: weight/(h*h)); 
	}
	
	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public YesNoType getUsingPrescMedicines() {
		return usingPrescMedicines;
	}

	public void setUsingPrescMedicines(YesNoType usingPrescMedicines) {
		this.usingPrescMedicines = usingPrescMedicines;
	}

	public String getReasonNotUsingPrescMedicines() {
		return reasonNotUsingPrescMedicines;
	}

	public void setReasonNotUsingPrescMedicines(String reasonNotUsingPrescMedicines) {
		this.reasonNotUsingPrescMedicines = reasonNotUsingPrescMedicines;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}

	public MedAppointmentType getAppointmentType() {
		return appointmentType;
	}

	public void setAppointmentType(MedAppointmentType appointmentType) {
		this.appointmentType = appointmentType;
	}
	
	/**
	 * @return the positionResponsible
	 */
	public String getPositionResponsible() {
		return positionResponsible;
	}

	/**
	 * @param positionResponsible the positionResponsible to set
	 */
	public void setPositionResponsible(String positionResponsible) {
		this.positionResponsible = positionResponsible;
	}
	
	public ReferredTo getPatientRefTo() {
		return patientRefTo;
	}

	public void setPatientRefTo(ReferredTo patientRefTo) {
		this.patientRefTo = patientRefTo;
	}

	public DotProvider getDotType() {
		return dotType;
	}

	public void setDotType(DotProvider dotType) {
		this.dotType = dotType;
	}

	public String getDotProvName() {
		return dotProvName;
	}

	public void setDotProvName(String dotProvName) {
		this.dotProvName = dotProvName;
	}

	public String getReferredToUnitName() {
		return referredToUnitName;
	}

	public void setReferredToUnitName(String referredToUnitName) {
		this.referredToUnitName = referredToUnitName;
	}

	public Date getRefToDate() {
		return refToDate;
	}

	public void setRefToDate(Date refToDate) {
		this.refToDate = refToDate;
	}

	
	
}
