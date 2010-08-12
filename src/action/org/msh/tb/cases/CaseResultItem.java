package org.msh.tb.cases;

import java.text.DecimalFormat;
import java.util.Date;

import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.utils.date.DateUtils;


public class CaseResultItem {
	private Integer caseId;
	private String patientName;
	private Date patientBirthDate;
	private Gender gender;
	private Integer caseNumber;
	private String healthUnit;
	private String region;
	private String locality;
	private Date notificationDate;
	private Date beginningTreatmentDate;
	private CaseState caseState;
	private Integer treatmentLenght;
	private Date endingTreatmentDate;
	private Integer patientRecordNumber;
	private CaseClassification classification;
	private ValidationState validationState;
	private String registrationCode;

	public String getDisplayCaseNumber() {
		if (registrationCode != null)
			return registrationCode;
		
		if (caseNumber == null)
			return null;
		
		DecimalFormat df = new DecimalFormat("000");
		String s = df.format(patientRecordNumber);
		if (caseNumber > 1)
			return s + "-" + caseNumber.toString();
		else return s;
	}

	public String getRegistrationCode() {
		return registrationCode;
	}

	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}

	public Integer getPatientRecordNumber() {
		return patientRecordNumber;
	}
	public void setPatientRecordNumber(Integer patientRecordNumber) {
		this.patientRecordNumber = patientRecordNumber;
	}
	public Date getEndingTreatmentDate() {
		return endingTreatmentDate;
	}
	public void setEndingTreatmentDate(Date endingTreatmentDate) {
		this.endingTreatmentDate = endingTreatmentDate;
	}
	/**
	 * Calc patient age from the birth date to the notification date 
	 * @return
	 */
	public Integer getPatientAge() {
		return (patientBirthDate==null? null: DateUtils.yearsBetween(patientBirthDate, notificationDate));
	}
	public Integer getCaseId() {
		return caseId;
	}
	public void setCaseId(Integer caseId) {
		this.caseId = caseId;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public Integer getCaseNumber() {
		return caseNumber;
	}
	public void setCaseNumber(Integer caseNumber) {
		this.caseNumber = caseNumber;
	}
	public String getHealthUnit() {
		return healthUnit;
	}
	public void setHealthUnit(String healthUnit) {
		this.healthUnit = healthUnit;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	public Date getNotificationDate() {
		return notificationDate;
	}
	public void setNotificationDate(Date notificationDate) {
		this.notificationDate = notificationDate;
	}
	public Date getBeginningTreatmentDate() {
		return beginningTreatmentDate;
	}
	public void setBeginningTreatmentDate(Date beginningTreatmentDate) {
		this.beginningTreatmentDate = beginningTreatmentDate;
	}
	public CaseState getCaseState() {
		return caseState;
	}
	public void setCaseState(CaseState caseState) {
		this.caseState = caseState;
	}
	public Integer getTreatmentLenght() {
		return treatmentLenght;
	}
	public void setTreatmentLenght(Integer treatmentLenght) {
		this.treatmentLenght = treatmentLenght;
	}
	public Date getPatientBirthDate() {
		return patientBirthDate;
	}
	public void setPatientBirthDate(Date patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}

	public CaseClassification getClassification() {
		return classification;
	}

	public void setClassification(CaseClassification classification) {
		this.classification = classification;
	}

	/**
	 * @return the validationState
	 */
	public ValidationState getValidationState() {
		return validationState;
	}

	/**
	 * @param validationState the validationState to set
	 */
	public void setValidationState(ValidationState validationState) {
		this.validationState = validationState;
	}
}
