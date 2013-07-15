package org.msh.tb.az.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.msh.tb.az.entities.enums.CaseFindingStrategy;
import org.msh.tb.az.entities.enums.LastAction;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;
import org.msh.utils.date.Period;

@Entity
@Table(name="tbcaseaz")
public class TbCaseAZ extends TbCase{
	private static final long serialVersionUID = 3151309615247467018L;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="MARITALSTATUS_ID"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="MARITALSTATUS_Complement"))})
	private FieldValueComponent maritalStatus;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="EDUCATIONALDEGREE_ID"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="EDUCATIONALDEGREE_Complement"))})
	private FieldValueComponent educationalDegree; 

	private CaseFindingStrategy caseFindingStrategy;
	
	private Integer numberOfImprisonments;
	
	private Date inprisonIniDate;
	
	private Date inprisonEndDate;

	@OneToMany(mappedBy="tbcase", cascade={CascadeType.ALL})
	private List<CaseSeverityMark> severityMarks = new ArrayList<CaseSeverityMark>();
	
	private String EIDSSID;
	
	private String EIDSSComment;
	
	private String doctor;
	
	private boolean referToOtherTBUnit;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="referToTBUnit_id")
	@PropertyLog(operations={Operation.ALL})
	private Tbunit referToTBUnit;

	private boolean toThirdCategory;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="iniDate", column=@Column(name="dateIniThirdCat")),
		@AttributeOverride(name="endDate", column=@Column(name="dateEndThirdCat"))
	})
	@PropertyLog(logEntityFields=true)
	private Period thirdCatPeriod = new Period();
	
	private String unicalID;
	
	private Date systemDate;
	
	private Date inEIDSSDate;
	
	private boolean colPrevTreatUnknown;
	
	private Date editingDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="createUser_id")
	private User createUser;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="editingUser_id")
	private User editingUser;
	
	private LastAction lastAction;
	
	/**
	 * Returns the case number in a formated way ready for displaying
	 * @return
	 */
	@Override
	public String getDisplayCaseNumber() {
		Workspace ws = (getPatient() != null? getPatient().getWorkspace() : null);
		if ((ws != null) && (ws.getConfirmedCaseNumber() == DisplayCaseNumber.USER_DEFINED))
			return getRegistrationCode();
		else {
			if (getPatient() == null)
				return "NA";
			if (getPatient().getRecordNumber() == null)
				return "NA";
			if (getCaseNumber() == null)
				return Integer.toString(getPatient().getRecordNumber());
			return formatCaseNumber(getPatient().getRecordNumber(), getCaseNumber());
		}
	}
	
	//=========================GETTERS FOR EIDSS COMPONENTS========================
	private boolean existInImport(int ind){
		if (EIDSSComment == null)
			return false;
		String[] ec = EIDSSComment.split(" / ");
		if (ec.length<ind+1)
			return false;
		if ("".equals(ec[ind]))
			return false;
		return true;
	}
	
	public String getEIDSSName(){
		if (existInImport(3)){
			String[] ec = EIDSSComment.split(" / ");
			return ec[3];
		}
		return "";
	}
	
	public Integer getEIDSSAge(){
		if (existInImport(4)){
			String[] ec = EIDSSComment.split(" / ");
			return Integer.parseInt(ec[4]);
		}
		return 0;
	}
	
	public Date getEIDSSBirthDate(){
		Date d = null;
		if (existInImport(5)){
			String[] ec = EIDSSComment.split(" / ");
			try {
				d = new SimpleDateFormat("dd-MM-yyyy").parse(ec[5]);
			} catch (ParseException e) {
				d = null;
			}
		}
		return d;
	}
	
	public Date getEIDSSNotifDate(){
		Date d = null;
		if (existInImport(6)){
			String[] ec = EIDSSComment.split(" / ");
			try {
				d = new SimpleDateFormat("dd-MM-yyyy").parse(ec[6]);
			} catch (ParseException e) {
				d = null;
			}
		}
		return d;
	}
	
	public Date getEIDSSInnerDate(){
		Date d = null;
		if (existInImport(2)){
			String[] ec = EIDSSComment.split(" / ");
			try {
				d = new SimpleDateFormat("dd-MM-yyyy").parse(ec[2]);
			} catch (ParseException e) {
				d = null;
			}
		}
		return d;
	}
	
	public String getEIDSSNotifUnit(){
		if (existInImport(0)){
			String[] ec = EIDSSComment.split(" / ");
			return ec[0];
		}
		return "";
	}
	
	public String getEIDSSNotifAddress(){
		if (existInImport(1)){
			String[] ec = EIDSSComment.split(" / ");
			return ec[1];
		}
		return ""; 
	}
	/**
	 * Return true if case import from EIDSS, but not binded yet
	 * @return
	 */
	public boolean isFirstEIDSSBind(){
		if (this.getLegacyId()!=null && this.getSystemDate()==null)
			return true;
		return false;
	}
	//===================GETTERS & SETTERS======================
	/**
	 * @return the maritalStatus
	 */
	public FieldValueComponent getMaritalStatus() {
		if (maritalStatus == null)
			maritalStatus = new FieldValueComponent();
		return maritalStatus;
	}

	/**
	 * @param maritalStatus the maritalStatus to set
	 */
	public void setMaritalStatus(FieldValueComponent maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	/**
	 * @return the educationalDegree
	 */
	public FieldValueComponent getEducationalDegree() {
		if (educationalDegree == null)
			educationalDegree = new FieldValueComponent();
		return educationalDegree;
	}

	/**
	 * @param educationalDegree the educationalDegree to set
	 */
	public void setEducationalDegree(FieldValueComponent educationalDegree) {
		this.educationalDegree = educationalDegree;
	}

	/**
	 * @return the caseFindingStrategy
	 */
	public CaseFindingStrategy getCaseFindingStrategy() {
		return caseFindingStrategy;
	}

	/**
	 * @param caseFindingStrategy the caseFindingStrategy to set
	 */
	public void setCaseFindingStrategy(CaseFindingStrategy caseFindingStrategy) {
		this.caseFindingStrategy = caseFindingStrategy;
	}

	/**
	 * @return the numberOfImprisonments
	 */
	public Integer getNumberOfImprisonments() {
		return numberOfImprisonments;
	}

	/**
	 * @param numberOfImprisonments the numberOfImprisonments to set
	 */
	public void setNumberOfImprisonments(Integer numberOfImprisonments) {
		this.numberOfImprisonments = numberOfImprisonments;
	}

	/**
	 * @return the inprisonIniDate
	 */
	public Date getInprisonIniDate() {
		return inprisonIniDate;
	}

	/**
	 * @param inprisonIniDate the inprisonIniDate to set
	 */
	public void setInprisonIniDate(Date inprisonIniDate) {
		this.inprisonIniDate = inprisonIniDate;
	}

	/**
	 * @return the inprisionEndDate
	 */
	public Date getInprisonEndDate() {
		return inprisonEndDate;
	}

	/**
	 * @param inprisionEndDate the inprisionEndDate to set
	 */
	public void setInprisonEndDate(Date inprisionEndDate) {
		this.inprisonEndDate = inprisionEndDate;
	}

	/**
	 * @return the severityMarks
	 */
	public List<CaseSeverityMark> getSeverityMarks() {
		return severityMarks;
	}

	/**
	 * @param severityMarks the severityMarks to set
	 */
	public void setSeverityMarks(List<CaseSeverityMark> severityMarks) {
		this.severityMarks = severityMarks;
	}

	public String getEIDSSID() {
		return EIDSSID;
	}

	public void setEIDSSID(String eIDSSID) {
		EIDSSID = eIDSSID;
	}

	public String getEIDSSComment() {
		return EIDSSComment;
	}

	public void setEIDSSComment(String eIDSSComment) {
		EIDSSComment = eIDSSComment;
	}

	public String getDoctor() {
		return doctor;
	}

	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}

	public boolean isReferToOtherTBUnit() {
		return referToOtherTBUnit;
	}

	public void setReferToOtherTBUnit(boolean referToOtherTBUnit) {
		this.referToOtherTBUnit = referToOtherTBUnit;
	}

	public Tbunit getReferToTBUnit() {
		return referToTBUnit;
	}

	public void setReferToTBUnit(Tbunit referToTBUnit) {
		this.referToTBUnit = referToTBUnit;
	}

	public boolean isToThirdCategory() {
		return toThirdCategory;
	}

	public void setToThirdCategory(boolean toThirdCategory) {
		this.toThirdCategory = toThirdCategory;
	}

	public Period getThirdCatPeriod() {
		if (thirdCatPeriod==null)
			thirdCatPeriod = new Period();
		return thirdCatPeriod;
	}

	public void setThirdCatPeriod(Period thirdCatPeriod) {
		this.thirdCatPeriod = thirdCatPeriod;
	}

	public String getUnicalID() {
		return unicalID;
	}

	public void setUnicalID(String unicalID) {
		this.unicalID = unicalID;
	}

	/**
	 * @param systemDate the systemDate to set
	 */
	public void setSystemDate(Date systemDate) {
		this.systemDate = systemDate;
	}

	/**
	 * @return the systemDate
	 */
	public Date getSystemDate() {
		return systemDate;
	}
	
	public void setInEIDSSDate(Date inEIDSSDate) {
		this.inEIDSSDate = inEIDSSDate;
	}

	public Date getInEIDSSDate() {
		return inEIDSSDate;
	}

	public void setColPrevTreatUnknown(boolean colPrevTreatUnknown) {
		this.colPrevTreatUnknown = colPrevTreatUnknown;
	}

	public boolean isColPrevTreatUnknown() {
		return colPrevTreatUnknown;
	}

	/**
	 * @return the editingDate
	 */
	public Date getEditingDate() {
		return editingDate;
	}

	/**
	 * @param editingDate the editingDate to set
	 */
	public void setEditingDate(Date editingDate) {
		this.editingDate = editingDate;
	}

	/**
	 * @return the createUser
	 */
	public User getCreateUser() {
		return createUser;
	}

	/**
	 * @param createUser the createUser to set
	 */
	public void setCreateUser(User createUser) {
		this.createUser = createUser;
	}

	/**
	 * @return the editingUser
	 */
	public User getEditingUser() {
		return editingUser;
	}

	/**
	 * @param editingUser the editingUser to set
	 */
	public void setEditingUser(User editingUser) {
		this.editingUser = editingUser;
	}

	public void setLastAction(LastAction lastAction) {
		this.lastAction = lastAction;
	}

	public LastAction getLastAction() {
		return lastAction;
	}

}
