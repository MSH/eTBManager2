package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.validator.NotNull;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.DiagnosisType;
import org.msh.mdrtb.entities.enums.DrugResistanceType;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.Nationality;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.mdrtb.entities.enums.TbCategory;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


@Entity
public class TbCase implements Serializable{
	private static final long serialVersionUID = 7221451624723376561L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@Version
	private Integer version;
	
	private Integer caseNumber;

	@Column(length=50)
	private String registrationCode;

	private Integer daysTreatPlanned;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PATIENT_ID")
	@NotNull
	private Patient patient;
	
	private Integer age;

	@NotNull
	@Temporal(TemporalType.DATE) private Date registrationDate;
	@Temporal(TemporalType.DATE) private Date diagnosisDate;
	@Temporal(TemporalType.DATE) private Date outcomeDate;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="iniDate", column=@Column(name="iniTreatmentDate")),
		@AttributeOverride(name="endDate", column=@Column(name="endTreatmentDate"))
	})
	private Period treatmentPeriod = new Period();
	
	@NotNull
	private CaseState state;
	
	@NotNull
	private ValidationState validationState;
	
	private PatientType patientType;
	
	private TbCategory category;
	
	private DiagnosisType diagnosisType;
	
	private DrugResistanceType drugResistanceType;

	@NotNull
	private CaseClassification classification;
	
	private InfectionSite infectionSite;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PULMONARY_ID")
	private FieldValue pulmonaryType;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EXTRAPULMONARY_ID")
	private FieldValue extrapulmonaryType;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EXTRAPULMONARY2_ID")
	private FieldValue extrapulmonaryType2;
	
	@Column(length=100)
	private String patientTypeOther;

	private Nationality nationality;
	
	@Column(length=100)
	private String otherOutcome;
	
	@Column(length=50)
	private String legacyId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="NOTIFICATION_UNIT_ID")
	private Tbunit notificationUnit;
	
	private boolean notifAddressChanged;
	
	private boolean tbContact;
	
	@Column(length=100)
	private String patientContactName;
	
	@Lob
	private String comments;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="address", column=@Column(name="NOTIF_ADDRESS")),
		@AttributeOverride(name="complement", column=@Column(name="NOTIF_COMPLEMENT")),
		@AttributeOverride(name="localityType", column=@Column(name="NOTIF_LOCALITYTYPE")),
		@AttributeOverride(name="zipCode", column=@Column(name="NOTIF_ZIPCODE")),
		@AttributeOverride(name="phoneNumber", column=@Column(name="NOTIF_PHONENUMBER")),
		@AttributeOverride(name="mobileNumber", column=@Column(name="NOTIF_MOBILENUMBER"))
	})
	@AssociationOverrides({
		@AssociationOverride(name="adminUnit", joinColumns=@JoinColumn(name="NOTIF_ADMINUNIT_ID"))
	})
	private Address notifAddress = new Address();
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="address", column=@Column(name="CURR_ADDRESS")),
		@AttributeOverride(name="complement", column=@Column(name="CURR_COMPLEMENT")),
		@AttributeOverride(name="localityType", column=@Column(name="CURR_LOCALITYTYPE")),
		@AttributeOverride(name="zipCode", column=@Column(name="CURR_ZIPCODE")),
		@AttributeOverride(name="phoneNumber", column=@Column(name="CURR_PHONENUMBER")),
		@AttributeOverride(name="mobileNumber", column=@Column(name="CURR_MOBILENUMBER"))
	})
	@AssociationOverrides({
		@AssociationOverride(name="adminUnit", joinColumns=@JoinColumn(name="CURR_ADMINUNIT_ID"))
	})
	private Address currentAddress = new Address();
	
	@Column(length=50)
	private String phoneNumber;
	
	@Column(length=50)
	private String mobileNumber;
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<CaseSideEffect> sideEffects = new ArrayList<CaseSideEffect>();

	@ManyToMany(cascade={CascadeType.ALL})
	@JoinTable(name="CASE_COMORBIDITIES")
	private List<FieldValue> comorbidities = new ArrayList<FieldValue>();


	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbCase")
	private List<CaseRegimen> regimens = new ArrayList<CaseRegimen>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbCase")
	private List<TreatmentHealthUnit> healthUnits = new ArrayList<TreatmentHealthUnit>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<MedicalExamination> examinations = new ArrayList<MedicalExamination>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<ExamXRay> resXRay = new ArrayList<ExamXRay>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<TbContact> contacts = new ArrayList<TbContact>();
	
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase")
	private List<CaseDispensing> dispensing = new ArrayList<CaseDispensing>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<PrescribedMedicine> prescribedMedicines = new ArrayList<PrescribedMedicine>();

	/* EXAMS */
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<ExamHIV> resHIV = new ArrayList<ExamHIV>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<ExamCulture> examsCulture = new ArrayList<ExamCulture>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<ExamMicroscopy> examsMicroscopy = new ArrayList<ExamMicroscopy>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	private List<ExamDST> examsDST = new ArrayList<ExamDST>();
	
	private int issueCounter;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ((registrationCode != null) && (!registrationCode.isEmpty())? registrationCode: getPatient().getFullName());
	}

	
	/**
	 * Update number of days of treatment planned for the patient
	 */
	public void updateDaysTreatPlanned() {
		int num = 0;
		for (PrescribedMedicine pm: getPrescribedMedicines()) {
			num += pm.getNumPrescribedDays();
		}
		daysTreatPlanned = num;
	}


	/**
	 * Return number of month of treatment based on the date 
	 * @param date
	 * @return
	 */
	public int getMonthTreatment(Date date) {
		if (treatmentPeriod == null)
			return -1;
		
		Date dtTreat = getTreatmentPeriod().getIniDate();
		if ((dtTreat == null) || (date.before(dtTreat)))
			return -1;

		int num = DateUtils.monthsBetween(date, dtTreat) + 1;

		return num;
	}
	

	/**
	 * Return list of treatment health units sorted by period
	 * @return
	 */
	public List<TreatmentHealthUnit> getSortedTreatmentHealthUnits() {
		// sort the periods
		Collections.sort(healthUnits, new Comparator<TreatmentHealthUnit>() {
			public int compare(TreatmentHealthUnit o1, TreatmentHealthUnit o2) {
				return o1.getPeriod().getIniDate().compareTo(o2.getPeriod().getIniDate());
			}
		});
		
		return healthUnits;
	}
	
	
	/**
	 * Return list of prescribed medicines sorted by period
	 * @return
	 */
	public List<PrescribedMedicine> getSortedPrescribedMedicines() {
		// sort the periods
		Collections.sort(prescribedMedicines, new Comparator<PrescribedMedicine>() {
			public int compare(PrescribedMedicine o1, PrescribedMedicine o2) {
				return o1.getPeriod().getIniDate().compareTo(o2.getPeriod().getIniDate());
			}
		});
		
		return prescribedMedicines;
	}
	

	/**
	 * Returns if the case is validated or not
	 * @return true - if the case is validated, false - otherwise
	 */
	public boolean isValidated() {
		return (getState() != null? state.ordinal() >= CaseState.WAITING_TREATMENT.ordinal(): false);
	}
	
	/**
	 * Returns if the case is a pulmonary TB/MDRTB
	 * @return - true if it is pulmonary or pulmonary/extrapulmonary
	 */
	public boolean isPulmonary() {
		return (getInfectionSite() != null) && ((infectionSite == InfectionSite.PULMONARY) || (infectionSite == InfectionSite.BOTH));
	}

	
	/**
	 * Returns if the case is a extrapulmonary TB/MDRTB
	 * @return - true if it is extrapulmonary or pulmonary/extrapulmonary
	 */
	public boolean isExtrapulmonary() {
		return (getInfectionSite() != null) && ((infectionSite == InfectionSite.EXTRAPULMONARY) || (infectionSite == InfectionSite.BOTH));
	}
	
	/**
	 * Search for side effect data by the side effect
	 * @param sideEffect - FieldValue object representing the side effect
	 * @return - CaseSideEffect instance containing side effect data of the case, or null if there is no side effect data
	 */
	public CaseSideEffect findSideEffectData(FieldValue sideEffect) {
		for (CaseSideEffect se: getSideEffects()) {
			if (se.getSideEffect().equals(sideEffect))
				return se;
		}
		return null;
	}

	/**
	 * @return the sideEffects
	 */
	public List<CaseSideEffect> getSideEffects() {
		return sideEffects;
	}


	/**
	 * @param sideEffects the sideEffects to set
	 */
	public void setSideEffects(List<CaseSideEffect> sideEffects) {
		this.sideEffects = sideEffects;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof TbCase))
			return false;
		
		return ((TbCase)obj).getId().equals(getId());
	}


	/**
	 * Returns the case number in a formated way ready for displaying
	 * @return
	 */
	public String getDisplayCaseNumber() {
		Integer number = getCaseNumber();
		if ((number == null) || (getPatient() == null))
			return null;
		
		return formatCaseNumber(patient.getRecordNumber(), caseNumber);
	}
	

	/**
	 * Formats the case number to be displayed to the user
	 * @param patientNumber - patient record number
	 * @param caseNumber - case number associated to the patient
	 * @return - formated case number
	 */
	static public String formatCaseNumber(int patientNumber, int caseNumber) {
		DecimalFormat df = new DecimalFormat("000");
		String s = df.format(patientNumber);

		if (caseNumber > 1)
			 return s + "-" + Integer.toString(caseNumber);
		else return s;		
	}

	/**
	 * returns the beginning intensive phase date
	 * @return
	 */
/*	public Date getIniIntensivePhase() {
		Date dt = null;
		for (CaseRegimen r: getRegimens()) {
			if ((r.getPhase() == RegimenPhase.INTENSIVE) && ((dt == null) || (dt.after(r.getIniDate()))))
				dt = r.getIniDate();
		}
		
		return dt;
	}
*/

	/**
	 * returns the ending intensive phase date
	 * @return
	 */
/*	public Date getEndingIntensivePhase() {
		Date dt = null;
		for (CaseRegimen r: getRegimens()) {
			if ((r.getPhase() == RegimenPhase.INTENSIVE) && ((dt == null) || (dt.before(r.getEndDate()))))
				dt = r.getEndDate();
		}
		
		return dt;
	}
*/
	
	/**
	 * Check if the case is open
	 * @return
	 */
	public boolean isOpen() {
		return (state == null) || (state.ordinal() <=  CaseState.TRANSFERRING.ordinal());
	}

	/**
	 * returns the continuous phase beginning date
	 * @return
	 */
/*	public Date getBeginningContinuousPhase() {
		Date dt = null;
		for (CaseRegimen r: getRegimens()) {
			if ((r.getPhase() == RegimenPhase.CONTINUOUS) && ((dt == null) || (dt.after(r.getIniDate()))))
				dt = r.getIniDate();
		}
		
		return dt;
	}
*/

	/**
	 * returns the continuous phase ending date
	 * @return
	 */
/*	public Date getEndingContinuousPhase() {
		Date dt = null;
		for (CaseRegimen r: getRegimens()) {
			if ((r.getPhase() == RegimenPhase.CONTINUOUS) && ((dt == null) || (dt.before(r.getEndDate()))))
				dt = r.getEndDate();
		}
		
		return dt;
	}
*/
	
	/**
	 * Returns patient age at the date of the notification
	 * @return
	 */
	public int getPatientAge() {
		if (age != null)
			return age;
		
		Patient p = getPatient();
		if (p == null)
			return 0;
		
		Date dt = p.getBirthDate();
		Date dt2 = diagnosisDate;
		
		if (dt == null)
			return 0;
		if (dt2 == null)
			dt2 = new Date();
		return DateUtils.yearsBetween(dt, dt2);
	}

	public List<CaseRegimen> getRegimens() {
		return regimens;
	}

	public void setRegimens(List<CaseRegimen> regimens) {
		this.regimens = regimens;
	}

	public Integer getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(Integer caseNumber) {
		this.caseNumber = caseNumber;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public CaseState getState() {
		return state;
	}

	public void setState(CaseState state) {
		this.state = state;
	}

	public List<TreatmentHealthUnit> getHealthUnits() {
		return healthUnits;
	}

	public void setHealthUnits(List<TreatmentHealthUnit> healthUnits) {
		this.healthUnits = healthUnits;
	}



	public List<ExamHIV> getResHIV() {
		return resHIV;
	}


	public void setResHIV(List<ExamHIV> resHIV) {
		this.resHIV = resHIV;
	}


	public List<MedicalExamination> getExaminations() {
		return examinations;
	}


	public void setExaminations(List<MedicalExamination> examinations) {
		this.examinations = examinations;
	}


	public CaseClassification getClassification() {
		return classification;
	}


	public void setClassification(CaseClassification classification) {
		this.classification = classification;
	}


	public InfectionSite getInfectionSite() {
		return infectionSite;
	}


	public void setInfectionSite(InfectionSite infectionSite) {
		this.infectionSite = infectionSite;
	}


	public Address getNotifAddress() {
		return notifAddress;
	}


	public void setNotifAddress(Address notifAddress) {
		this.notifAddress = notifAddress;
	}


	public Address getCurrentAddress() {
		return currentAddress;
	}


	public void setCurrentAddress(Address currentAddress) {
		this.currentAddress = currentAddress;
	}


	public boolean isNotifAddressChanged() {
		return notifAddressChanged;
	}


	public void setNotifAddressChanged(boolean notifAddressChanged) {
		this.notifAddressChanged = notifAddressChanged;
	}


	public String getOtherOutcome() {
		return otherOutcome;
	}


	public void setOtherOutcome(String otherOutcome) {
		this.otherOutcome = otherOutcome;
	}


	public String getLegacyId() {
		return legacyId;
	}


	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}


	public PatientType getPatientType() {
		return patientType;
	}


	public void setPatientType(PatientType patientType) {
		this.patientType = patientType;
	}


	public Nationality getNationality() {
		return nationality;
	}


	public void setNationality(Nationality nationality) {
		this.nationality = nationality;
	}


	public Tbunit getNotificationUnit() {
		return notificationUnit;
	}


	public void setNotificationUnit(Tbunit notificationUnit) {
		this.notificationUnit = notificationUnit;
	}


	public Date getDiagnosisDate() {
		return diagnosisDate;
	}


	public void setDiagnosisDate(Date diagnosisDate) {
		this.diagnosisDate = diagnosisDate;
	}


	public Date getOutcomeDate() {
		return outcomeDate;
	}


	public void setOutcomeDate(Date outcomeDate) {
		this.outcomeDate = outcomeDate;
	}


	public String getPatientTypeOther() {
		return patientTypeOther;
	}


	public void setPatientTypeOther(String patientTypeOther) {
		this.patientTypeOther = patientTypeOther;
	}


	/**
	 * @return the resXRay
	 */
	public List<ExamXRay> getResXRay() {
		return resXRay;
	}


	/**
	 * @param resXRay the resXRay to set
	 */
	public void setResXRay(List<ExamXRay> resXRay) {
		this.resXRay = resXRay;
	}


	/**
	 * @return the comorbidities
	 */
	public List<FieldValue> getComorbidities() {
		return comorbidities;
	}


	/**
	 * @param comorbidities the comorbidities to set
	 */
	public void setComorbidities(List<FieldValue> comorbidities) {
		this.comorbidities = comorbidities;
	}


	/**
	 * @return the age
	 */
	public Integer getAge() {
		return age;
	}


	/**
	 * @param age the age to set
	 */
	public void setAge(Integer age) {
		this.age = age;
	}


	/**
	 * @return the registrationDate
	 */
	public Date getRegistrationDate() {
		return registrationDate;
	}

	/**
	 * @param registrationDate the registrationDate to set
	 */
	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(TbCategory category) {
		this.category = category;
	}

	/**
	 * @return the category
	 */
	public TbCategory getCategory() {
		return category;
	}


	/**
	 * @param diagnosisType the diagnosisType to set
	 */
	public void setDiagnosisType(DiagnosisType diagnosisType) {
		this.diagnosisType = diagnosisType;
	}


	/**
	 * @return the diagnosisType
	 */
	public DiagnosisType getDiagnosisType() {
		return diagnosisType;
	}

	
	/**
	 * @return the registrationCode
	 */
	public String getRegistrationCode() {
		return registrationCode;
	}


	/**
	 * @param registrationCode the registrationCode to set
	 */
	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}


	/**
	 * @return the drugResistanceType
	 */
	public DrugResistanceType getDrugResistanceType() {
		return drugResistanceType;
	}


	/**
	 * @param drugResistanceType the drugResistanceType to set
	 */
	public void setDrugResistanceType(DrugResistanceType drugResistanceType) {
		this.drugResistanceType = drugResistanceType;
	}


	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}


	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}



	/**
	 * @return the patientContactName
	 */
	public String getPatientContactName() {
		return patientContactName;
	}


	/**
	 * @param patientContactName the patientContactName to set
	 */
	public void setPatientContactName(String patientContactName) {
		this.patientContactName = patientContactName;
	}


	/**
	 * @param tbContact the tbContact to set
	 */
	public void setTbContact(boolean tbContact) {
		this.tbContact = tbContact;
	}


	/**
	 * @return the tbContact
	 */
	public boolean isTbContact() {
		return tbContact;
	}


	/**
	 * @return the contacts
	 */
	public List<TbContact> getContacts() {
		return contacts;
	}


	/**
	 * @param contacts the contacts to set
	 */
	public void setContacts(List<TbContact> contacts) {
		this.contacts = contacts;
	}


	/**
	 * @return the pulmonaryType
	 */
	public FieldValue getPulmonaryType() {
		return pulmonaryType;
	}


	/**
	 * @param pulmonaryType the pulmonaryType to set
	 */
	public void setPulmonaryType(FieldValue pulmonaryType) {
		this.pulmonaryType = pulmonaryType;
	}


	/**
	 * @return the extrapulmonaryType
	 */
	public FieldValue getExtrapulmonaryType() {
		return extrapulmonaryType;
	}


	/**
	 * @param extrapulmonaryType the extrapulmonaryType to set
	 */
	public void setExtrapulmonaryType(FieldValue extrapulmonaryType) {
		this.extrapulmonaryType = extrapulmonaryType;
	}


	/**
	 * @return the extrapulmonaryType2
	 */
	public FieldValue getExtrapulmonaryType2() {
		return extrapulmonaryType2;
	}


	/**
	 * @param extrapulmonaryType2 the extrapulmonaryType2 to set
	 */
	public void setExtrapulmonaryType2(FieldValue extrapulmonaryType2) {
		this.extrapulmonaryType2 = extrapulmonaryType2;
	}


	/**
	 * @return the daysTreatPlanned
	 */
	public Integer getDaysTreatPlanned() {
		return daysTreatPlanned;
	}


	/**
	 * @param daysTreatPlanned the daysTreatPlanned to set
	 */
	public void setDaysTreatPlanned(Integer daysTreatPlanned) {
		this.daysTreatPlanned = daysTreatPlanned;
	}


	/**
	 * @return the dispensing
	 */
	public List<CaseDispensing> getDispensing() {
		return dispensing;
	}


	/**
	 * @param dispensing the dispensing to set
	 */
	public void setDispensing(List<CaseDispensing> dispensing) {
		this.dispensing = dispensing;
	}


	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}


	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}


	/**
	 * @return the mobileNumber
	 */
	public String getMobileNumber() {
		return mobileNumber;
	}


	/**
	 * @param mobileNumber the mobileNumber to set
	 */
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
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


	/**
	 * @return the issueCounter
	 */
	public int getIssueCounter() {
		return issueCounter;
	}


	/**
	 * @param issueCounter the issueCounter to set
	 */
	public void setIssueCounter(int issueCount) {
		this.issueCounter = issueCount;
	}

	public void incIssueCounter() {
		issueCounter++;
	}


	public void setPrescribedMedicines(List<PrescribedMedicine> prescribedMedicines) {
		this.prescribedMedicines = prescribedMedicines;
	}


	public List<PrescribedMedicine> getPrescribedMedicines() {
		return prescribedMedicines;
	}


	public Period getTreatmentPeriod() {
		return treatmentPeriod;
	}


	public void setTreatmentPeriod(Period treatmentPeriod) {
		this.treatmentPeriod = treatmentPeriod;
	}


	public Integer getVersion() {
		return version;
	}


	public void setVersion(Integer version) {
		this.version = version;
	}


	public List<ExamCulture> getExamsCulture() {
		return examsCulture;
	}


	public void setExamsCulture(List<ExamCulture> examsCulture) {
		this.examsCulture = examsCulture;
	}


	public List<ExamMicroscopy> getExamsMicroscopy() {
		return examsMicroscopy;
	}


	public void setExamsMicroscopy(List<ExamMicroscopy> examsMicroscopy) {
		this.examsMicroscopy = examsMicroscopy;
	}


	public List<ExamDST> getExamsDST() {
		return examsDST;
	}


	public void setExamsDST(List<ExamDST> examsDST) {
		this.examsDST = examsDST;
	}
}
