package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.msh.entities.enums.ClinicalEvolution;
import org.msh.mdrtb.entities.enums.MedAppointmentType;
import org.msh.mdrtb.entities.enums.YesNoType;

/**
 *
 * Records information about a medical examination of a case
 *
 * @author Ricardo Mem�ria
 *
 */
@Entity
public class MedicalExamination extends CaseData implements Serializable {
	private static final long serialVersionUID = 2760727118134685773L;

	private Double weight;
	
	private Float height;
	
	private Float heartRate;
	private Float bloodPressureMin;
	private Float bloodPressureMax;
	private Float respRateRest;
	private Float temperature;

	private MedAppointmentType appointmentType;

	private YesNoType usingPrescMedicines;
	
	@Column(length=100)
	private String reasonNotUsingPrescMedicines;
	
	@Column(length=100)
	private String responsible;
	
	@Column(length=100)
	private String positionResponsible;

	
	private YesNoType supervisedTreatment;
	
	@Column(length=100)
	private String supervisionUnitName;
	
	private YesNoType surgicalProcedure;
	
	@Column(length=100)
	private String surgicalProcedureDesc;


	public String getSurgicalProcedureDesc() {
		return surgicalProcedureDesc;
	}

	public void setSurgicalProcedureDesc(String surgicalProcedureDesc) {
		this.surgicalProcedureDesc = surgicalProcedureDesc;
	}

	public YesNoType getSurgicalProcedure() {
		return surgicalProcedure;
	}

	public void setSurgicalProcedure(YesNoType surgicalProcedure) {
		this.surgicalProcedure = surgicalProcedure;
	}
	
	public YesNoType getSupervisedTreatment() {
		return supervisedTreatment;
	}

	public void setSupervisedTreatment(YesNoType supervisedTreatment) {
		this.supervisedTreatment = supervisedTreatment;
	}

	public String getSupervisionUnitName() {
		return supervisionUnitName;
	}

	public void setSupervisionUnitName(String supervisionUnitName) {
		this.supervisionUnitName = supervisionUnitName;
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
	 * @return the heartRate
	 */
	public Float getHeartRate() {
		return heartRate;
	}

	/**
	 * @param heartRate the heartRate to set
	 */
	public void setHeartRate(Float heartRate) {
		this.heartRate = heartRate;
	}

	/**
	 * @return the temperature
	 */
	public Float getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(Float temperature) {
		this.temperature = temperature;
	}

	/**
	 * @return the bloodPressureMin
	 */
	public Float getBloodPressureMin() {
		return bloodPressureMin;
	}

	/**
	 * @param bloodPressureMin the bloodPressureMin to set
	 */
	public void setBloodPressureMin(Float bloodPressureMin) {
		this.bloodPressureMin = bloodPressureMin;
	}

	/**
	 * @return the bloodPressureMax
	 */
	public Float getBloodPressureMax() {
		return bloodPressureMax;
	}

	/**
	 * @param bloodPressureMax the bloodPressureMax to set
	 */
	public void setBloodPressureMax(Float bloodPressureMax) {
		this.bloodPressureMax = bloodPressureMax;
	}

	/**
	 * @return the respRateRest
	 */
	public Float getRespRateRest() {
		return respRateRest;
	}

	/**
	 * @param respRateRest the respRateRest to set
	 */
	public void setRespRateRest(Float respRateRest) {
		this.respRateRest = respRateRest;
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
}
