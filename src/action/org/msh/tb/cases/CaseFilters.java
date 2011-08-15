package org.msh.tb.cases;


import java.util.Calendar;
import java.util.Date;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.security.Identity;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.misc.GlobalLists;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.ItemSelectList;
import org.msh.utils.date.DateUtils;


/**
 * Define the filters for case searching
 * @author Ricardo Memoria
 *
 */
@Name("caseFilters")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=10000L)
public class CaseFilters {

	@In(create=true) Workspace defaultWorkspace;
	
	private String name;
	private String middleName;
	private String lastName;
	private String recordNumber;
//	private String registrationCode;
	private String unitRegCode;
	private CaseState caseState;
	private ValidationState validationState;
	private CaseClassification classification;
	private int firstRecord;
	private int order;
	private boolean inverseOrder;
	private PatientType patientType;
	private InfectionSite infectionSite;
	private DiagnosisType diagnosisType;
	private Integer iniMonth;
	private Integer iniYear;
	private Integer endMonth;
	private Integer endYear;
	private FilterHealthUnit filterHealthUnit = FilterHealthUnit.BOTH;
	// date filters
	private boolean registrationDate = true;
	private boolean diagnosisDate;
	private boolean outcomeDate;
	private boolean iniTreatmentDate;
	
	private AdminUnitSelection auselection;
	private TBUnitSelection tbunitselection;
	
	private String patient;
	
	/**
	 * Used by the left-side status of the case. Used it because closed cases required a special index (100)
	 */
	private Integer stateIndex;
	
	private Integer tagid;
	
	private ItemSelectList<CaseClassification> classifications;
	
	/**
	 * Selected view in the detail page of the case
	 */
	private int caseTab;

	/**
	 * Used by {@link CasesQuery} to check which search mode to use 
	 */
	private SearchCriteria searchCriteria;

	
	/**
	 * Initialize the filters reseting values 
	 */
	@Observer("change-workspace")
	public void initialize() {
		searchCriteria = SearchCriteria.CUSTOM_FILTER;
		classifications = null;
		
		clearFilters();
	}

	/**
	 * Reinitialize the advanced filters
	 */
	protected void clearFilters() {
		name = null;
		middleName = null;
		lastName = null;
		recordNumber = null;
//		registrationCode = null;
		unitRegCode = null;
		caseState = null;
		validationState = null;
		classification = null;
		auselection = null;
		tbunitselection = null;
		firstRecord = 0;
		patientType = null;
		infectionSite = null;
		diagnosisType = null;
		iniMonth = null;
		iniYear = null;
		endMonth = null;
		endYear = null;
		filterHealthUnit = FilterHealthUnit.BOTH;
		registrationDate = true;
		diagnosisDate = false;
		outcomeDate = false;
		iniTreatmentDate = false;
		patient = null;
		stateIndex = null;

		tagid = null;
		
		order = 0;
		inverseOrder = false;
	}

	
/*	*//**
	 * Set the execution of case searching using advanced search filers
	 *//*
	public String startAdvancedSearch() {
		advancedSearch = true;
		return "search";
	}

*/

	
	/**
	 * Called when the search criteria changes
	 */
	protected void searchCriteriaChanged() {
		if (searchCriteria == null)
			searchCriteria = SearchCriteria.CUSTOM_FILTER;
		
		switch (searchCriteria) {
			case CASE_STATE:
				Integer si = stateIndex;
//				CaseState st = caseState;
				clearFilters();
				stateIndex = si;
//				caseState = st;
				break;
			case PATIENT:
				String s = patient;
				clearFilters();
				patient = s;
				break;
			case VALIDATION_STATE:
				ValidationState vs = validationState;
				clearFilters();
				validationState = vs;
				break;
			case CASE_TAG:
				Integer id = tagid;
				clearFilters();
				tagid = id;
				break;
			case CUSTOM_FILTER:
				patient = null;
				validationState = null;
				stateIndex = null;
				tagid = null;
				break;
		}
	}


	/**
	 * Set the execution of case searching using the patient name/number 
	 */
/*	public String startSingleSearch() {
		clearAdvancedSearhFilters();
		advancedSearch = false;
		return "search";
	}
*/
	
	public String getNameLike() {
		return ((name == null) || (name.isEmpty())? null: '%' + name.toUpperCase() + '%');
	}
	
	public String getMiddleNameLike() {
		return ((middleName == null) || (middleName.isEmpty())? null: '%' + middleName.toUpperCase() + '%');
	}
	
	public String getLastNameLike() {
		return ((lastName == null) || (lastName.isEmpty())? null: '%' + lastName.toUpperCase() + '%');
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public CaseState getCaseState() {
		return caseState;
	}

	public void setCaseState(CaseState caseState) {
		this.caseState = caseState;
	}

	public String getRecordNumber() {
		return recordNumber;
	}

	public void setRecordNumber(String recordNumber) {
		this.recordNumber = recordNumber;
	}

	public CaseClassification getClassification() {
		boolean mdrCases = Identity.instance().hasRole(CaseClassification.DRTB.toString() + "_CASE_VIEW");
		boolean tbcases = Identity.instance().hasRole(CaseClassification.TB.toString() + "_CASE_VIEW");
		
		if (mdrCases && tbcases)
			return classification;
		else {
			if (mdrCases)
				 return CaseClassification.DRTB;
			else return CaseClassification.TB;
		}
	}

	public void setClassification(CaseClassification classification) {
		this.classification = classification;
	}

	public int getFirstRecord() {
		return firstRecord;
	}

	public void setFirstRecord(int firstRecord) {
		this.firstRecord = firstRecord;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isInverseOrder() {
		return inverseOrder;
	}

	public void setInverseOrder(boolean inverseOrder) {
		this.inverseOrder = inverseOrder;
	}

	/**
	 * @return the patientType
	 */
	public PatientType getPatientType() {
		return patientType;
	}

	/**
	 * @param patientType the patientType to set
	 */
	public void setPatientType(PatientType patientType) {
		this.patientType = patientType;
	}

	/**
	 * @return the infectionSite
	 */
	public InfectionSite getInfectionSite() {
		return infectionSite;
	}

	/**
	 * @param infectionSite the infectionSite to set
	 */
	public void setInfectionSite(InfectionSite infectionSite) {
		this.infectionSite = infectionSite;
	}
	// VR: To include 'Type of Diagnostic' in Advance Search Page
	/**
	 * @return the diagnosisType
	 */
	public DiagnosisType getDiagnosisType(){
		return diagnosisType;
	}
	
	/**
	 * @param diagnosisType the diagnosisType to set
	 */
	public void setDiagnosisType(DiagnosisType diagnosisType){
		this.diagnosisType = diagnosisType;
	}

	/**
	 * @return the iniMonth
	 */
	public Integer getIniMonth() {
		return iniMonth;
	}

	/**
	 * @param iniMonth the iniMonth to set
	 */
	public void setIniMonth(Integer iniMonth) {
		this.iniMonth = iniMonth;
	}

	/**
	 * @return the iniYear
	 */
	public Integer getIniYear() {
		return iniYear;
	}

	/**
	 * @param iniYear the iniYear to set
	 */
	public void setIniYear(Integer iniYear) {
		this.iniYear = iniYear;
	}

	/**
	 * @return the endMonth
	 */
	public Integer getEndMonth() {
		return endMonth;
	}

	/**
	 * @param endMonth the endMonth to set
	 */
	public void setEndMonth(Integer endMonth) {
		this.endMonth = endMonth;
	}

	/**
	 * @return the endYear
	 */
	public Integer getEndYear() {
		return endYear;
	}

	/**
	 * @param endYear the endYear to set
	 */
	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}

	/**
	 * @return the filterHealthUnit
	 */
	public FilterHealthUnit getFilterHealthUnit() {
		return filterHealthUnit;
	}

	/**
	 * @param filterHealthUnit the filterHealthUnit to set
	 */
	public void setFilterHealthUnit(FilterHealthUnit filterHealthUnit) {
		this.filterHealthUnit = filterHealthUnit;
	}
	
	/**
	 * Returns the initial date of a filter by dates
	 * @return initial date if initial month and year are set in caseFilters, otherwise null
	 */
	public Date getIniDate() {
		if ((iniMonth != null) && (iniYear != null)) {
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(iniYear, iniMonth, 1);
			return c.getTime();
		}
		return null;
	}
	
	/**
	 * Returns the initial date of a filter by dates
	 * @return initial date if initial month and year are set in caseFilters, otherwise null
	 */
	public Date getEndDate() {
		if ((endMonth != null) && (endYear != null)) {
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(endYear, endMonth, DateUtils.daysInAMonth(endYear, endMonth));
			return c.getTime();
		}
		return null;		
	}
	/**
	 * @return the diagnosisDate
	 */
	public boolean isDiagnosisDate() {
		return diagnosisDate;
	}

	/**
	 * @param diagnosisDate the diagnosisDate to set
	 */
	public void setDiagnosisDate(boolean diagnosisDate) {
		this.diagnosisDate = diagnosisDate;
	}

	/**
	 * @return the outcomeDate
	 */
	public boolean isOutcomeDate() {
		return outcomeDate;
	}

	/**
	 * @param outcomeDate the outcomeDate to set
	 */
	public void setOutcomeDate(boolean outcomeDate) {
		this.outcomeDate = outcomeDate;
	}

	/**
	 * @return the iniTreatmentDate
	 */
	public boolean isIniTreatmentDate() {
		return iniTreatmentDate;
	}

	/**
	 * @param iniTreatmentDate the iniTreatmentDate to set
	 */
	public void setIniTreatmentDate(boolean iniTreatmentDate) {
		this.iniTreatmentDate = iniTreatmentDate;
	}


	/**
	 * Returns the new order property
	 * @return
	 */
	public int getNewOrder() {
		return order;
	}

	
	/**
	 * Change the order. If the order is the same as before, switch the inverseOrder property
	 * @param order
	 */
	public void setNewOrder(int order) {
		if (order == this.order)
			inverseOrder = !inverseOrder;
		else inverseOrder = false;
		this.order = order;
	}
	
	public AdminUnitSelection getAuselection() {
		if (auselection == null) {
			auselection = new AdminUnitSelection();
			auselection.setApplyUserRestrictions(false);
		}
		return auselection;
	}
	
	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null) {
			tbunitselection = new TBUnitSelection();
			tbunitselection.setApplyUserRestrictions(true);
			tbunitselection.setFilter(TBUnitFilter.HEALTH_UNITS);
		}
		return tbunitselection;
	}

	public String getAdminUnitLike() {
		AdministrativeUnit adm = getAuselection().getSelectedUnit();
		
		if (adm == null)
			return null;
		
		return adm.getCode() + "%";
	}


	public String getTbAdminUnitLike() {
		AdministrativeUnit adm = getTbunitselection().getAdminUnit();
		
		if (adm == null)
			return null;
		
		return adm.getCode() + "%";
	}

	/**
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	/**
	 * @return the registrationDate
	 */
	public boolean isRegistrationDate() {
		return registrationDate;
	}

	/**
	 * @param registrationDate the registrationDate to set
	 */
	public void setRegistrationDate(boolean registrationDate) {
		this.registrationDate = registrationDate;
	}

	/**
	 * @return the patient
	 */
	public String getPatient() {
		return patient;
	}

	/**
	 * @param patient the patient to set
	 */
	public void setPatient(String patient) {
		if ((patient != null) && (patient.trim().length() == 0))
			 this.patient = null;
		else this.patient = patient;
		
		stateIndex = null;
	}
	
	public String getPatientLike() {
		return patient != null? "%" + patient + "%": null;
	}
	
/*	public Integer getPatientNumber() {
		if (patient == null)
			return null;
		
		try {
			return Integer.valueOf(patient); 
		} catch (Exception e) {
			return null;
		}
	}
*/

	/**
	 * @return the stateIndex
	 */
	public Integer getStateIndex() {
		return stateIndex;
	}

	/**
	 * @param stateIndex the stateIndex to set
	 */
	public void setStateIndex(Integer stateIndex) {
		this.stateIndex = stateIndex;
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

	public SearchCriteria getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(SearchCriteria searchCriteria) {
		if (this.searchCriteria == searchCriteria)
			return;
		
		this.searchCriteria = searchCriteria;
		searchCriteriaChanged();
	}

	/**
	 * Check if record number is empty
	 * @return
	 */
	public boolean isRecordNumberEmpty() {
		return ((recordNumber == null) || (recordNumber.isEmpty()));
	}


	/**
	 * Return the patient record number entered in the field recordNumber in the format nnnnn-nn
	 * @return
	 */
	public Integer getPatientRecordNumber() {
		if (isRecordNumberEmpty())
			return null;
		
//		if (defaultWorkspace == null)
//			defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace");
		
		if ((defaultWorkspace == null) || (DisplayCaseNumber.REGISTRATION_CODE.equals(defaultWorkspace.getDisplayCaseNumber())))
			return null;

		return getNumberFromString(recordNumber, 0);
	}

	
	/**
	 * If patient search number is by its registration code, then return the record number given by the user
	 * @return
	 */
	public String getRegistrationCode() {
//		if (defaultWorkspace == null)
//			defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace");

		if (DisplayCaseNumber.REGISTRATION_CODE.equals(defaultWorkspace.getDisplayCaseNumber()))
			 return recordNumber;
		else return (getNumberFromString(recordNumber, 0) != null? null: recordNumber);
	}


	/**
	 * Return the case number being the number after the character '-' entered in the field record number 
	 * in the format NNNN-NN
	 * @return
	 */
	public Integer getCaseNumber() {
		if ((isRecordNumberEmpty()) || (DisplayCaseNumber.REGISTRATION_CODE.equals(defaultWorkspace.getDisplayCaseNumber())))
			return null;

		return getNumberFromString(recordNumber, 1);
	}


	protected Integer getNumberFromString(String val, int index) {
		if (val == null)
			return null;
		
		String[] vals = val.split("-");
		if (vals.length > index) {
			try {
				return Integer.parseInt(vals[index]);
			} catch (Exception e) {
				return null;
			}
		}
		else return null;		
	}


	/**
	 * Return the list of classifications available in the workspace
	 * @return
	 */
	public ItemSelectList<CaseClassification> getClassifications() {
		if (classifications == null) {
			GlobalLists globalLists = (GlobalLists)Component.getInstance("globalLists");
			classifications = new ItemSelectList<CaseClassification>(globalLists.getUserCaseClassifications());
			classifications.selectAll();
		}
		return classifications;
	}


	/**
	 * @return the caseView
	 */
	public CaseView getCaseView() {
		switch (caseTab) {
		case 2: return CaseView.EXAMS;
		case 3: return CaseView.TREATMENT;
		case 4: return CaseView.MEDEXAMS;
		case 5: return CaseView.ADDINFO;
		case 6: return CaseView.RESUME;
		}
		return CaseView.DATA;
	}

	public void setCaseView(CaseView view) {
		switch (view) {
		case DATA: caseTab = 1;
			break;
		case EXAMS: caseTab = 2;
			break;
		case TREATMENT: caseTab = 3;
			break;
		case MEDEXAMS: caseTab = 4;
			break;
		case ADDINFO: caseTab = 5;
			break;
		case RESUME: caseTab = 6;
		}
	}

	/**
	 * @return the tagid
	 */
	public Integer getTagid() {
		return tagid;
	}

	/**
	 * @param tagid the tagid to set
	 */
	public void setTagid(Integer tagid) {
		this.tagid = tagid;
	}

	/**
	 * @return the caseTab
	 */
	public int getCaseTab() {
		return caseTab;
	}

	/**
	 * @param caseTab the caseTab to set
	 */
	public void setCaseTab(int caseTab) {
		this.caseTab = caseTab;
	}

	public String getUnitRegCode() {
		return unitRegCode;
	}

	public void setUnitRegCode(String unitRegCode) {
		this.unitRegCode = unitRegCode;
	}
		
/*	public String getRegistrationCode() {
		return registrationCode;
	}

	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}
*/	
	
}
