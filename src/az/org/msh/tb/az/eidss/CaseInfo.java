package org.msh.tb.az.eidss;


import java.util.Date;

public class CaseInfo {
	private  String caseID;
	private Date FinalDiagnosisDate;
    private String firstName;
    private String middleName;
    private String lastName;
    private int patientGender;
   private int caseStatus;
   private Date DateOfBirth;
   private String AdditionalComment;
public String getCaseID() {
	return caseID;
}
public void setCaseID(String caseID) {
	this.caseID = caseID;
}
public Date getFinalDiagnosisDate() {
	return FinalDiagnosisDate;
}
public void setFinalDiagnosisDate(Date finalDiagnosisDate) {
	FinalDiagnosisDate = finalDiagnosisDate;
}
public String getFirstName() {
	return firstName;
}
public void setFirstName(String firstName) {
	this.firstName = firstName;
}
public String getMiddleName() {
	return middleName;
}
public void setMiddleName(String middleName) {
	this.middleName = middleName;
}
public String getLastName() {
	return lastName;
}
public void setLastName(String lastName) {
	this.lastName = lastName;
}
public int getPatientGender() {
	return patientGender;
}
public void setPatientGender(int patientGender) {
	this.patientGender = patientGender;
}
public int getCaseStatus() {
	return caseStatus;
}
public void setCaseStatus(int caseStatus) {
	this.caseStatus = caseStatus;
}
public Date getDateOfBirth() {
	return DateOfBirth;
}
public void setDateOfBirth(Date dateOfBirth) {
	DateOfBirth = dateOfBirth;
}
public String getAdditionalComment() {
	return AdditionalComment;
}
public void setAdditionalComment(String additionalComment) {
	AdditionalComment = additionalComment;
}
  

}
