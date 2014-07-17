package org.msh.tb.entities;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.enums.*;
import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@TypeDefs({@TypeDef(name="weeklyFrequency", typeClass=WeeklyFrequencyType.class)})

@Entity
@Table(name="workspace")
public class Workspace implements Serializable, Transactional {
	private static final long serialVersionUID = -7496421288607921489L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Embedded
	@PropertyLog(messageKey="form.name")
	private LocalizedNameComp name = new LocalizedNameComp();

	@OneToMany(mappedBy="workspace", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@PropertyLog(ignore=true)
	private List<UserWorkspace> users = new ArrayList<UserWorkspace>();

	@Column(length=150)
	private String description;

	@Column(length=10)
	private String defaultLocale;
	
	@Column(length=10)
	private String alternateLocale;
	
	@Column(length=200)
	private String defaultTimeZone;

	// frequency of doses in a weekly basis
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq1 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq2 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq3 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq4 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq5 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq6 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq7 = new WeeklyFrequency();

	@Column(length=10)
	private String extension;

	@OneToOne(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private WorkspaceView view;

	/**
	 * Setup the patient name composition to be used in data entry forms and displaying
	 */
	private NameComposition patientNameComposition;
	
	/**
	 * Configuration of the case validation for TB cases
	 */
	private CaseValidationOption caseValidationTB;
	
	/**
	 * Configuration of the case validation for DR-TB cases
	 */
	private CaseValidationOption caseValidationDRTB;
	
	/**
	 * Configuration of the case validation for MNT cases
	 */
	private CaseValidationOption caseValidationNTM;

	/**
	 * If true, the ULA will be displayed once to the user to be accepted
	 */
	private boolean ulaActive;
	
	/**
	 * Setup the case number to be displayed for the suspect cases
	 */
	private DisplayCaseNumber suspectCaseNumber;
	
	/**
	 * Setup the case number to be displayed for the confirmed cases
	 */
	private DisplayCaseNumber confirmedCaseNumber;
	
	/**
	 * Required levels of administrative unit for patient address
	 */
	private Integer patientAddrRequiredLevels;
	
	@Column(length=200)
	private String url;

	/**
	 * Indicate if system will send e-mail messages to the users in certain system events (like new orders, orders authorized, etc)
	 */
	private boolean sendSystemMessages;
	
	/**
	 * Setup the quantity of months that the system will consider when it has to alert the user about medicines that will expire.
	 */
	private Integer monthsToAlertExpiredMedicines;
	
	/**
	 * Minimum stock on-hand in months allowed in the unit. If a medicine is equals or under this level,
	 * the system will alert about that 
	 */
	private Integer minStockOnHand;
	
	/**
	 * Maximum stock on-hand in months allowed in the unit. If a medicine is equals or over this level,
	 * the system will alert about that 
	 */
	private Integer maxStockOnHand;
	
	/**
	 * If true, in the medicine in-take monitoring of the case, user will specify if administered the treatment
	 * in DOTS or if it was self-administered by the patient. If false, the user will just select the day patient 
	 * received medicine
	 */
	private TreatMonitoringInput treatMonitoringInput;

	/**
	 * Setup the adjustment type that represents expired medicine movements
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="expiredMedicineAdjustmentType_ID")
	@PropertyLog(messageKey="Workspace.ExpiredMedAdjustType")
	private FieldValue expiredMedicineAdjustmentType;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="lastTransaction_ID")
	@PropertyLog(ignore=true)
	private TransactionLog lastTransaction;
	


	public WeeklyFrequency[] getWeeklyFrequencies() {
		WeeklyFrequency[] lst = new WeeklyFrequency[7];

		lst[0] = weekFreq1;
		lst[1] = weekFreq2;
		lst[2] = weekFreq3;
		lst[3] = weekFreq4;
		lst[4] = weekFreq5;
		lst[5] = weekFreq6;
		lst[6] = weekFreq7;
		
		return lst;
	}
	
	public void setWeeklyFrequency(WeeklyFrequency[] vals) {
		weekFreq1 = vals[0];
		weekFreq2 = vals[1];
		weekFreq3 = vals[2];
		weekFreq4 = vals[3];
		weekFreq5 = vals[4];
		weekFreq6 = vals[5];
		weekFreq7 = vals[6];
	}
	
	public WeeklyFrequency getWeeklyFrequency(int frequency) {
		switch (frequency) {
		case 1: return weekFreq1;
		case 2: return weekFreq2;
		case 3: return weekFreq3;
		case 4: return weekFreq4;
		case 5: return weekFreq5;
		case 6: return weekFreq6;
		case 7: return weekFreq7;
		}
		return null;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	/**
	 * Return the case validation setting of the given case classification
	 * @param classification is the classification of the case
	 * @return instance of {@link CaseValidationOption}
	 */
	public CaseValidationOption getCaseValidationOption(CaseClassification classification) {
		switch (classification) {
		case TB:
			return caseValidationTB;
		case DRTB:
			return caseValidationDRTB;
		case NTM:
			return caseValidationNTM;
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof Workspace))
			return false;

		return ((Workspace)obj).getId().equals(getId());
	}

	
	@Override
	public String toString() {
		return getName().toString();
	}
	
	public boolean isHasAlternateLocale() {
		return ((getAlternateLocale() != null) && (!alternateLocale.isEmpty()));
	}

	public String getViewUri() {
		WorkspaceView view = getView();
		if ((view == null) || (view.getPictureURI() == null))
			 return "/public/images/globe.png";
		else return view.getPictureURI();
	}
	
	/**
	 * Returns the name of the language according to the locale
	 * @param s
	 * @return
	 */
	protected String getLocaleDisplayName(String s) {
		if ((s == null) || (s.isEmpty()))
			return null;
		
		int p = s.indexOf("_");
		Locale loc = null;
		if (p != -1) {
			String lan = s.substring(0, p);
			String lc = s.substring(p+1);
			loc = new Locale(lan, lc);
		}
		else  loc = new Locale(s);
		
		return loc.getDisplayName(LocaleSelector.instance().getLocale());		
	}

	public String getDefaultDisplayLocale() {
		String s = getDefaultLocale();
		return getLocaleDisplayName(s);
	}
	
	public String getAlternateDisplayLocale() {
		return getLocaleDisplayName(getAlternateLocale());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	/**
	 * @return the name
	 */
	public LocalizedNameComp getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(LocalizedNameComp name) {
		this.name = name;
	}

	public List<UserWorkspace> getUsers() {
		return users;
	}

	public void setUsers(List<UserWorkspace> users) {
		this.users = users;
	}

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public String getDefaultTimeZone() {
		return defaultTimeZone;
	}

	public void setDefaultTimeZone(String defaultTimeZone) {
		this.defaultTimeZone = defaultTimeZone;
	}

	public WeeklyFrequency getWeekFreq1() {
		return weekFreq1;
	}

	public void setWeekFreq1(WeeklyFrequency weekFreq1) {
		this.weekFreq1 = weekFreq1;
	}

	public WeeklyFrequency getWeekFreq2() {
		return weekFreq2;
	}

	public void setWeekFreq2(WeeklyFrequency weekFreq2) {
		this.weekFreq2 = weekFreq2;
	}

	public WeeklyFrequency getWeekFreq3() {
		return weekFreq3;
	}

	public void setWeekFreq3(WeeklyFrequency weekFreq3) {
		this.weekFreq3 = weekFreq3;
	}

	public WeeklyFrequency getWeekFreq4() {
		return weekFreq4;
	}

	public void setWeekFreq4(WeeklyFrequency weekFreq4) {
		this.weekFreq4 = weekFreq4;
	}

	public WeeklyFrequency getWeekFreq5() {
		return weekFreq5;
	}

	public void setWeekFreq5(WeeklyFrequency weekFreq5) {
		this.weekFreq5 = weekFreq5;
	}

	public WeeklyFrequency getWeekFreq6() {
		return weekFreq6;
	}

	public void setWeekFreq6(WeeklyFrequency weekFreq6) {
		this.weekFreq6 = weekFreq6;
	}

	public WeeklyFrequency getWeekFreq7() {
		return weekFreq7;
	}

	public void setWeekFreq7(WeeklyFrequency weekFreq7) {
		this.weekFreq7 = weekFreq7;
	}

	public String getAlternateLocale() {
		return alternateLocale;
	}

	public void setAlternateLocale(String alternateLocale) {
		this.alternateLocale = alternateLocale;
	}

	/**
	 * Returns the root path of the custom pages for the country. Ex: /brazil or /ukraine 
	 * @return
	 */
/*	public String getRootPath() {
		return ((customPath == null)||(customPath.isEmpty()) ? "/custom/generic": customPath);
	}
*/

	/**
	 * @return the patientNameComposition
	 */
	public NameComposition getPatientNameComposition() {
		return patientNameComposition;
	}

	/**
	 * @param patientNameComposition the patientNameComposition to set
	 */
	public void setPatientNameComposition(NameComposition patientNameComposition) {
		this.patientNameComposition = patientNameComposition;
	}

	/**
	 * @return the patientAddrRequiredLevels
	 */
	public Integer getPatientAddrRequiredLevels() {
		return patientAddrRequiredLevels;
	}

	/**
	 * @param patientAddrRequiredLevels the patientAddrRequiredLevels to set
	 */
	public void setPatientAddrRequiredLevels(Integer patientAddrRequiredLevels) {
		this.patientAddrRequiredLevels = patientAddrRequiredLevels;
	}

	/**
	 * @return the extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * @return the view
	 */
	public WorkspaceView getView() {
		return view;
	}

	/**
	 * @param view the view to set
	 */
	public void setView(WorkspaceView view) {
		this.view = view;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the sendSystemMessages
	 */
	public boolean isSendSystemMessages() {
		return sendSystemMessages;
	}

	/**
	 * @param sendSystemMessages the sendSystemMessages to set
	 */
	public void setSendSystemMessages(boolean sendSystemMessages) {
		this.sendSystemMessages = sendSystemMessages;
	}

	/**
	 * @return the monthsToAlertExpiredMedicines
	 */
	public Integer getMonthsToAlertExpiredMedicines() {
		return monthsToAlertExpiredMedicines;
	}

	/**
	 * @param monthsToAlertExpiredMedicines the monthsToAlertExpiredMedicines to set
	 */
	public void setMonthsToAlertExpiredMedicines(
			Integer monthsToAlertExpiredMedicines) {
		this.monthsToAlertExpiredMedicines = monthsToAlertExpiredMedicines;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#getLastTransaction()
	 */
	@Override
	public TransactionLog getLastTransaction() {
		return lastTransaction;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.entities.Transactional#setLastTransaction(org.msh.tb.entities.TransactionLog)
	 */
	@Override
	public void setLastTransaction(TransactionLog transactionLog) {
		this.lastTransaction = transactionLog;
	}

	public boolean isUlaActive() {
		return ulaActive;
	}

	public void setUlaActive(boolean ulaActive) {
		this.ulaActive = ulaActive;
	}

	/**
	 * @return the expiredMedicineAdjustmentType
	 */
	public FieldValue getExpiredMedicineAdjustmentType() {
		return expiredMedicineAdjustmentType;
	}

	/**
	 * @param expiredMedicineAdjustmentType the expiredMedicineAdjustmentType to set
	 */
	public void setExpiredMedicineAdjustmentType(
			FieldValue expiredMedicineAdjustmentType) {
		this.expiredMedicineAdjustmentType = expiredMedicineAdjustmentType;
	}

	/**
	 * @return the caseValidationTB
	 */
	public CaseValidationOption getCaseValidationTB() {
		return caseValidationTB;
	}

	/**
	 * @param caseValidationTB the caseValidationTB to set
	 */
	public void setCaseValidationTB(CaseValidationOption caseValidationTB) {
		this.caseValidationTB = caseValidationTB;
	}

	/**
	 * @return the caseValidationDRTB
	 */
	public CaseValidationOption getCaseValidationDRTB() {
		return caseValidationDRTB;
	}

	/**
	 * @param caseValidationDRTB the caseValidationDRTB to set
	 */
	public void setCaseValidationDRTB(CaseValidationOption caseValidationDRTB) {
		this.caseValidationDRTB = caseValidationDRTB;
	}

	/**
	 * @return the caseValidationNTM
	 */
	public CaseValidationOption getCaseValidationNTM() {
		return caseValidationNTM;
	}

	/**
	 * @param caseValidationNTM the caseValidationNTM to set
	 */
	public void setCaseValidationNTM(CaseValidationOption caseValidationNTM) {
		this.caseValidationNTM = caseValidationNTM;
	}

	/**
	 * @return the minStockOnHand
	 */
	public Integer getMinStockOnHand() {
		return minStockOnHand;
	}

	/**
	 * @param minStockOnHand the minStockOnHand to set
	 */
	public void setMinStockOnHand(Integer minStockOnHand) {
		this.minStockOnHand = minStockOnHand;
	}

	/**
	 * @return the maxStockOnHand
	 */
	public Integer getMaxStockOnHand() {
		return maxStockOnHand;
	}

	/**
	 * @param maxStockOnHand the maxStockOnHand to set
	 */
	public void setMaxStockOnHand(Integer maxStockOnHand) {
		this.maxStockOnHand = maxStockOnHand;
	}

	/**
	 * @return the suspectCaseNumber
	 */
	public DisplayCaseNumber getSuspectCaseNumber() {
		return suspectCaseNumber;
	}

	/**
	 * @param suspectCaseNumber the suspectCaseNumber to set
	 */
	public void setSuspectCaseNumber(DisplayCaseNumber suspectCaseNumber) {
		this.suspectCaseNumber = suspectCaseNumber;
	}

	/**
	 * @return the confirmedCaseNumber
	 */
	public DisplayCaseNumber getConfirmedCaseNumber() {
		return confirmedCaseNumber;
	}

	/**
	 * @param confirmedCaseNumber the confirmedCaseNumber to set
	 */
	public void setConfirmedCaseNumber(DisplayCaseNumber confirmedCaseNumber) {
		this.confirmedCaseNumber = confirmedCaseNumber;
	}

	/**
	 * @return the treatMonitoringInput
	 */
	public TreatMonitoringInput getTreatMonitoringInput() {
		return treatMonitoringInput;
	}

	/**
	 * @param treatMonitoringInput the treatMonitoringInput to set
	 */
	public void setTreatMonitoringInput(TreatMonitoringInput treatMonitoringInput) {
		this.treatMonitoringInput = treatMonitoringInput;
	}

}
