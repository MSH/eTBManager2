package org.msh.tb.entities;

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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ClinicalEvolution;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;



/**
 * Store information about a case (TB or DR-TB)
 * @author Ricardo Memoria
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="tbcase")
public class TbCase implements Serializable{
	private static final long serialVersionUID = 7221451624723376561L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@Version
	@PropertyLog(ignore=true)
	private Integer version;
	
	private Integer caseNumber;

	@Column(length=50)
	@PropertyLog(operations={Operation.ALL})
	private String registrationCode;

	private Integer daysTreatPlanned;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PATIENT_ID")
	@NotNull
	@PropertyLog(logEntityFields=true)
	private Patient patient;
	
	private Integer age;

	@NotNull
	@Temporal(TemporalType.DATE) 
	@PropertyLog(operations={Operation.ALL})
	private Date registrationDate;
	
	@Temporal(TemporalType.DATE) 
	@PropertyLog(operations={Operation.ALL})
	private Date diagnosisDate;
	
	@Temporal(TemporalType.DATE) 
	private Date outcomeDate;

	// Treatment information
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="iniDate", column=@Column(name="iniTreatmentDate")),
		@AttributeOverride(name="endDate", column=@Column(name="endTreatmentDate"))
	})
	@PropertyLog(logEntityFields=true)
	private Period treatmentPeriod = new Period();
	
	@Temporal(TemporalType.DATE)
	private Date iniContinuousPhase; 

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGIMEN_ID")
	private Regimen regimen;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGIMEN_INI_ID")
	private Regimen regimenIni;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TREATMENT_UNIT_ID")
	private Tbunit treatmentUnit;
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	@PropertyLog(ignore=true)
	private List<TreatmentHealthUnit> healthUnits = new ArrayList<TreatmentHealthUnit>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase")
	@PropertyLog(ignore=true)
	private List<PrescribedMedicine> prescribedMedicines = new ArrayList<PrescribedMedicine>();

	@NotNull
	private CaseState state;
	
	@NotNull
	private ValidationState validationState;
	
	@PropertyLog(operations={Operation.ALL})
	private PatientType patientType;

	@PropertyLog(operations={Operation.ALL})
	private DiagnosisType diagnosisType;
	
	@PropertyLog(operations={Operation.ALL})
	private DrugResistanceType drugResistanceType;

	@NotNull
	@PropertyLog(operations={Operation.ALL})
	private CaseClassification classification;
	
	@PropertyLog(key="InfectionSite", operations={Operation.NEW, Operation.EDIT})
	private InfectionSite infectionSite;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PULMONARY_ID")
	@PropertyLog(key="TbField.PULMONARY_TYPES")
	private FieldValue pulmonaryType;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EXTRAPULMONARY_ID")
	@PropertyLog(key="TbField.EXTRAPULMONARY_TYPES")
	private FieldValue extrapulmonaryType;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EXTRAPULMONARY2_ID")
	@PropertyLog(key="TbField.EXTRAPULMONARY_TYPES")
	private FieldValue extrapulmonaryType2;
	
	@Column(length=100)
	private String patientTypeOther;

	private Nationality nationality;
	
	@Column(length=100)
	private String otherOutcome;
	
	@Column(length=50)
	@PropertyLog(key="global.legacyId")
	private String legacyId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="NOTIFICATION_UNIT_ID")
	@PropertyLog(operations={Operation.ALL})
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
	@PropertyLog(key="cases.details.addressnotif", operations={Operation.NEW, Operation.EDIT})
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
	@PropertyLog(key="cases.details.addresscurr")
	private Address currentAddress = new Address();
	
	@Column(length=50)
	private String phoneNumber;
	
	@Column(length=50)
	private String mobileNumber;
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<CaseSideEffect> sideEffects = new ArrayList<CaseSideEffect>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<CaseComorbidity> comorbidities = new ArrayList<CaseComorbidity>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<MedicalExamination> examinations = new ArrayList<MedicalExamination>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<ExamXRay> resXRay = new ArrayList<ExamXRay>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<TbContact> contacts = new ArrayList<TbContact>();
	
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<CaseDispensing> dispensing = new ArrayList<CaseDispensing>();
	

	/* EXAMS */
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<ExamHIV> resHIV = new ArrayList<ExamHIV>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<ExamCulture> examsCulture = new ArrayList<ExamCulture>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<ExamMicroscopy> examsMicroscopy = new ArrayList<ExamMicroscopy>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcase", fetch=FetchType.LAZY)
	private List<ExamDST> examsDST = new ArrayList<ExamDST>();
	
	private int issueCounter;

	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name="tags_case", 
			joinColumns={@JoinColumn(name="CASE_ID")},
			inverseJoinColumns={@JoinColumn(name="TAG_ID")})
	private List<Tag> tags = new ArrayList<Tag>();

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = getPatient().getFullName();
		String num = getDisplayCaseNumber();
		if (num != null)
			s = "(" + num + ") " + s;

		return s;
	}


	/**
	 * Check if the regimen has changed compared to the initial regimen
	 * @return
	 */
	public boolean isInitialRegimenChanged() {
		if (regimen == regimenIni) 
			return false;
		
		if ((regimen == null) || (regimenIni == null))
			return true;
		
		return (regimen.getId().equals(regimenIni.getId()));
	}


	/**
	 * Update number of days of treatment planned for the patient
	 */
	public void updateDaysTreatPlanned() {
		Date dt = getTreatmentPeriod().getIniDate();
		Date dtend = getTreatmentPeriod().getEndDate();
		
		int num = 0;
		while (!dt.after(dtend)) {
			if (isDayPrescription(dt))
				num++;
			dt = DateUtils.incDays(dt, 1);
		}

		daysTreatPlanned = num;
	}


	/**
	 * Returns if the date dt is a day of medicine prescription
	 * @param dt - Date
	 * @return true - if it's a day of medicine prescription
	 */
	public boolean isDayPrescription(Date dt) {
		if ((treatmentPeriod == null) || (treatmentPeriod.isEmpty()))
			return false;
		
		if (!treatmentPeriod.isDateInside(dt))
			return false;

		for (PrescribedMedicine pm: getPrescribedMedicines()) {
			if (pm.getPeriod().isDateInside(dt)) {
				WeeklyFrequency wf = pm.getWeeklyFrequency();
				if (wf.isDateSet(dt))
					return true;
			}
		}
		
		return false;
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
	 * Return list of prescribed medicines sorted by medicine name and initial date of the period
	 * @return
	 */
	public List<PrescribedMedicine> getSortedPrescribedMedicines() {
		// sort the periods
		Collections.sort(prescribedMedicines, new Comparator<PrescribedMedicine>() {
			public int compare(PrescribedMedicine pm1, PrescribedMedicine pm2) {
				int val = pm1.getMedicine().getAbbrevName().compareTo(pm2.getMedicine().getAbbrevName());
				
				return (val != 0? val: pm1.getPeriod().getIniDate().compareTo(pm2.getPeriod().getEndDate()));
			}
		});
		
		return prescribedMedicines;
	}
	

	/**
	 * Returns if the case is validated or not
	 * @return true - if the case is validated, false - otherwise
	 */
	public boolean isValidated() {
		return getValidationState() == ValidationState.VALIDATED;
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
			if (se.getSideEffect().getValue().equals(sideEffect))
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
		Workspace ws = (patient != null? patient.getWorkspace() : null);
		if ((ws != null) && (ws.getDisplayCaseNumber() == DisplayCaseNumber.REGISTRATION_CODE))
			return getRegistrationCode();
		else {
			Integer number = getCaseNumber();
			if ((number == null) || (getPatient() == null))
				return Messages.instance().get("cases.new");
			
			return formatCaseNumber(patient.getRecordNumber(), caseNumber);
		}
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
	 * Check if the case is open
	 * @return
	 */
	public boolean isOpen() {
		return (state != null ? state.ordinal() <=  CaseState.TRANSFERRING.ordinal() : false);
	}



	/**
	 * Return the treatment period of the intensive phase
	 * @return
	 */
	public Period getIntensivePhasePeriod() {
		if ((treatmentPeriod == null) || (treatmentPeriod.isEmpty()))
			return null;

		if (iniContinuousPhase != null)
			 return new Period(treatmentPeriod.getIniDate(), DateUtils.incDays( iniContinuousPhase, -1 ) );
		else return new Period(treatmentPeriod);
	}


	/**
	 * Return the treatment period of the continuous phase
	 * @return
	 */
	public Period getContinuousPhasePeriod() {
		if ((iniContinuousPhase == null) || (treatmentPeriod == null) || (treatmentPeriod.isEmpty()))
			return null;

		return new Period(iniContinuousPhase, treatmentPeriod.getEndDate());
	}


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
	public List<CaseComorbidity> getComorbidities() {
		return comorbidities;
	}


	/**
	 * @param comorbidities the comorbidities to set
	 */
	public void setComorbidities(List<CaseComorbidity> comorbidities) {
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


	/**
	 * @return the iniContinuousPhase
	 */
	public Date getIniContinuousPhase() {
		return iniContinuousPhase;
	}


	/**
	 * @param iniContinuousPhase the iniContinuousPhase to set
	 */
	public void setIniContinuousPhase(Date iniContinuousPhase) {
		this.iniContinuousPhase = iniContinuousPhase;
	}


	/**
	 * @return the regimen
	 */
	public Regimen getRegimen() {
		return regimen;
	}


	/**
	 * @param regimen the regimen to set
	 */
	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}


	/**
	 * @return the treatmentUnit
	 */
	public Tbunit getTreatmentUnit() {
		return treatmentUnit;
	}


	/**
	 * @param treatmentUnit the treatmentUnit to set
	 */
	public void setTreatmentUnit(Tbunit treatmentUnit) {
		this.treatmentUnit = treatmentUnit;
	}


	/**
	 * @return the tags
	 */
	public List<Tag> getTags() {
		return tags;
	}


	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}


	/**
	 * @return the regimenIni
	 */
	public Regimen getRegimenIni() {
		return regimenIni;
	}


	/**
	 * @param regimenIni the regimenIni to set
	 */
	public void setRegimenIni(Regimen regimenIni) {
		this.regimenIni = regimenIni;
	}
	
	/**
	 * @return the evolution of the case
	 */
	public String getCaseEvolution() {
		if(this.getExaminations().size() > 0 && (!this.getState().equals(CaseState.WAITING_TREATMENT))){
			ClinicalEvolution eval = this.getExaminations().get(getExaminations().size()-1).getClinicalEvolution(); // for some old cases may be null AK 26/05/2012
			if (eval != null)
				return this.getExaminations().get(getExaminations().size()-1).getClinicalEvolution().getKey();
		}
			
		return "";
	}
		
	/**
	 * @return the name of the sepervision unit
	 */
	public String getSupervisionUnitName() {
		
		MedicalExamination lastExam = null;
		
		if(this.getExaminations() != null && this.getExaminations().size() > 0){
			lastExam = this.getExaminations().get(0);
			for(MedicalExamination m : this.getExaminations()){
				if(m.getDate().after(lastExam.getDate()))
					lastExam = m;
			}
		}
		if(lastExam != null
				&& lastExam.getSupervisedTreatment() != null
				&& lastExam.getSupervisedTreatment().equals(YesNoType.YES)
				&& lastExam.getSupervisionUnitName() != null){
			
			for(MedicalExamination m : this.getExaminations()){
				if(m.getDate().after(lastExam.getDate()))
					lastExam = m;
			}
			
			return lastExam.getSupervisionUnitName();
		}
		
		return "";
	}
	
	/**
	 * @return the name of the sepervision unit
	 */
	public YesNoType getSupervised() {
		if(this.getExaminations() != null && this.getExaminations().size() > 0
				&& this.getExaminations().get(getExaminations().size()-1).getSupervisedTreatment() != null)
			return this.getExaminations().get(getExaminations().size()-1).getSupervisedTreatment();
		return null;
	}
}
