package org.msh.tb.test.dbgen;

import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store preferences about the cases to be generated in the database 
 * @author Ricardo Memoria
 *
 */
public class GeneratorPreferences {

	private boolean remExistingCases;
	private List<CaseInfo> cases = new ArrayList<CaseInfo>();
	private Map<Gender, Integer> genders = new HashMap<Gender, Integer>();
	private Map<PatientType, Integer> patientTypesTB = new HashMap<PatientType, Integer>();
	private Map<PatientType, Integer> patientTypesMDR = new HashMap<PatientType, Integer>();
	private Map<InfectionSite, Integer> infectionSites = new HashMap<InfectionSite, Integer>();
	private List<String> firstNamesMale = new ArrayList<String>();
	private List<String> firstNamesFemale = new ArrayList<String>();
	private List<String> lastNames = new ArrayList<String>();
	private FactorValue facRegimenStdInd = new FactorValue();
	private Map<Regimen, Integer> regimensTB = new HashMap<Regimen, Integer>();
	private Map<Regimen, Integer> regimensMDR = new HashMap<Regimen, Integer>();
	private Map<AgeRangeInfo, Integer> ageRanges = new HashMap<AgeRangeInfo, Integer>();
	private Map<LocalityType, Integer> localityTypes = new HashMap<LocalityType, Integer>();
	private Map<Nationality, Integer> nationalities = new HashMap<Nationality, Integer>();
	
	// number of treatments for each patient
	private Map<NumTreatments, Integer> numTreatments = new HashMap<NumTreatments, Integer>();
	
	private Map<AdministrativeUnit, Integer> regions = new HashMap<AdministrativeUnit, Integer>();
	private int percTransfCases;
	private int cohortVarTime;
	private Map<CaseState, Integer> outcomesTB = new HashMap<CaseState, Integer>();
	private Map<CaseState, Integer> outcomesMDR = new HashMap<CaseState, Integer>();

	private RangeValue startTreatmentTB = new RangeValue();
	private RangeValue startTreatmentMDR = new RangeValue();

	// list of substances to use in the DST exam
	private List<Substance> substances = new ArrayList<Substance>();
	// number of TB cases with resistance to some medicine (0 to 100) among 100 cases
	private int resTB; 
	
	// resistance patterns found in DST exams
	private Map<List<Substance>, Integer> resPatternsTB = new HashMap<List<Substance>, Integer>(); 
	private Map<List<Substance>, Integer> resPatternsMDR = new HashMap<List<Substance>, Integer>(); 

	private RangeValue microscopyFirst = new RangeValue();
	private RangeValue cultureFirst = new RangeValue();
	private RangeValue dstFirst = new RangeValue();
	private RangeValue hivFirst = new RangeValue();
	private int microscopyFreqTB;
	private int microscopyFreqMDR;
	private int cultureFreqTB;
	private int cultureFreqMDR;
	private int dstFreqTB;
	private int dstFreqMDR;
	private int varDateExam;
	private int hivFreqTB;
	private int hivFreqMDR;
	private Map<MicroscopyResult, Integer> microscopyResults = new HashMap<MicroscopyResult, Integer>();
	private Map<CultureResult, Integer> cultureResults = new HashMap<CultureResult, Integer>();
	private Map<DstResult, Integer> dstResultsTB = new HashMap<DstResult, Integer>();
	private Map<DstResult, Integer> dstResultsMDR = new HashMap<DstResult, Integer>();
	private Map<HIVResult, Integer> hivResults = new HashMap<HIVResult, Integer>();

	// number of days of variation between registration and diagnosis
	private RangeValue varDaysRegDiag = new RangeValue();
	
	private Map<DiagnosisType, Integer> mdrDiagnosis = new HashMap<DiagnosisType, Integer>();
	private Map<FieldValue, Integer> pulmonaryForms = new HashMap<FieldValue, Integer>();
	private Map<FieldValue, Integer> extrapulmonaryForms = new HashMap<FieldValue, Integer>();
	
	// X-Ray exams
	private Map<FieldValue, Integer> xrayPresentation = new HashMap<FieldValue, Integer>();
	private Map<FieldValue, Integer> xrayPresentationProgress = new HashMap<FieldValue, Integer>();
	private RangeValue xrayNextResult = new RangeValue();
	
	// comorbidities
	private int percComorbidities;
	private Map<FieldValue, Integer> comorbidities = new HashMap<FieldValue, Integer>();
	
	// adverse reactions
	private int percAdverseReactions;
	private Map<FieldValue, Integer> adverseReactions = new HashMap<FieldValue, Integer>();
	
	// contact evaluation
	private int percContacts;
	private RangeValue contactsRange = new RangeValue();
	private Map<FieldValue, Integer> contactType = new HashMap<FieldValue, Integer>();
	private Map<FieldValue, Integer> contactConduct = new HashMap<FieldValue, Integer>();


	/**
	 * @return the mdrDiagnosis
	 */
	public Map<DiagnosisType, Integer> getMdrDiagnosis() {
		return mdrDiagnosis;
	}
	/**
	 * @param mdrDiagnosis the mdrDiagnosis to set
	 */
	public void setMdrDiagnosis(Map<DiagnosisType, Integer> mdrDiagnosis) {
		this.mdrDiagnosis = mdrDiagnosis;
	}
	/**
	 * @return the varDaysRegDiag
	 */
	public RangeValue getVarDaysRegDiag() {
		return varDaysRegDiag;
	}
	/**
	 * @param varDaysRegDiag the varDaysRegDiag to set
	 */
	public void setVarDaysRegDiag(RangeValue varDaysRegDiag) {
		this.varDaysRegDiag = varDaysRegDiag;
	}
	/**
	 * @return the regimensTB
	 */
	public Map<Regimen, Integer> getRegimensTB() {
		return regimensTB;
	}
	/**
	 * @param regimensTB the regimensTB to set
	 */
	public void setRegimensTB(Map<Regimen, Integer> regimensTB) {
		this.regimensTB = regimensTB;
	}
	/**
	 * @return the regimensMDR
	 */
	public Map<Regimen, Integer> getRegimensMDR() {
		return regimensMDR;
	}
	/**
	 * @param regimensMDR the regimensMDR to set
	 */
	public void setRegimensMDR(Map<Regimen, Integer> regimensMDR) {
		this.regimensMDR = regimensMDR;
	}
	/**
	 * @return the outcomesTB
	 */
	public Map<CaseState, Integer> getOutcomesTB() {
		return outcomesTB;
	}
	/**
	 * @param outcomesTB the outcomesTB to set
	 */
	public void setOutcomesTB(Map<CaseState, Integer> outcomesTB) {
		this.outcomesTB = outcomesTB;
	}
	/**
	 * @return the outcomesMDR
	 */
	public Map<CaseState, Integer> getOutcomesMDR() {
		return outcomesMDR;
	}
	/**
	 * @param outcomesMDR the outcomesMDR to set
	 */
	public void setOutcomesMDR(Map<CaseState, Integer> outcomesMDR) {
		this.outcomesMDR = outcomesMDR;
	}
	/**
	 * @return the resTB
	 */
	public int getResTB() {
		return resTB;
	}
	/**
	 * @param resTB the resTB to set
	 */
	public void setResTB(int resTB) {
		this.resTB = resTB;
	}
	/**
	 * @return the resPatternsTB
	 */
	public Map<List<Substance>, Integer> getResPatternsTB() {
		return resPatternsTB;
	}
	/**
	 * @param resPatternsTB the resPatternsTB to set
	 */
	public void setResPatternsTB(Map<List<Substance>, Integer> resPatternsTB) {
		this.resPatternsTB = resPatternsTB;
	}
	/**
	 * @return the resPatternsMDR
	 */
	public Map<List<Substance>, Integer> getResPatternsMDR() {
		return resPatternsMDR;
	}
	/**
	 * @param resPatternsMDR the resPatternsMDR to set
	 */
	public void setResPatternsMDR(Map<List<Substance>, Integer> resPatternsMDR) {
		this.resPatternsMDR = resPatternsMDR;
	}
	/**
	 * @return the microscopyFirst
	 */
	public RangeValue getMicroscopyFirst() {
		return microscopyFirst;
	}
	/**
	 * @param microscopyFirst the microscopyFirst to set
	 */
	public void setMicroscopyFirst(RangeValue microscopyFirst) {
		this.microscopyFirst = microscopyFirst;
	}
	/**
	 * @return the cultureFirst
	 */
	public RangeValue getCultureFirst() {
		return cultureFirst;
	}
	/**
	 * @param cultureFirst the cultureFirst to set
	 */
	public void setCultureFirst(RangeValue cultureFirst) {
		this.cultureFirst = cultureFirst;
	}
	/**
	 * @return the dstFirst
	 */
	public RangeValue getDstFirst() {
		return dstFirst;
	}
	/**
	 * @param dstFirst the dstFirst to set
	 */
	public void setDstFirst(RangeValue dstFirst) {
		this.dstFirst = dstFirst;
	}
	/**
	 * @return the varDateExam
	 */
	public int getVarDateExam() {
		return varDateExam;
	}
	/**
	 * @param varDateExam the varDateExam to set
	 */
	public void setVarDateExam(int varDateExam) {
		this.varDateExam = varDateExam;
	}
	/**
	 * @return the microscopyResults
	 */
	public Map<MicroscopyResult, Integer> getMicroscopyResults() {
		return microscopyResults;
	}
	/**
	 * @param microscopyResults the microscopyResults to set
	 */
	public void setMicroscopyResults(Map<MicroscopyResult, Integer> microscopyResults) {
		this.microscopyResults = microscopyResults;
	}
	/**
	 * @return the cultureResults
	 */
	public Map<CultureResult, Integer> getCultureResults() {
		return cultureResults;
	}
	/**
	 * @param cultureResults the cultureResults to set
	 */
	public void setCultureResults(Map<CultureResult, Integer> cultureResults) {
		this.cultureResults = cultureResults;
	}
	/**
	 * @return the substances
	 */
	public List<Substance> getSubstances() {
		return substances;
	}
	/**
	 * @param substances the substances to set
	 */
	public void setSubstances(List<Substance> substances) {
		this.substances = substances;
	}
	/**
	 * @return the nationalities
	 */
	public Map<Nationality, Integer> getNationalities() {
		return nationalities;
	}
	/**
	 * @param nationalities the nationalities to set
	 */
	public void setNationalities(Map<Nationality, Integer> nationalities) {
		this.nationalities = nationalities;
	}
	/**
	 * @return the ageRanges
	 */
	public Map<AgeRangeInfo, Integer> getAgeRanges() {
		return ageRanges;
	}
	/**
	 * @param ageRanges the ageRanges to set
	 */
	public void setAgeRanges(Map<AgeRangeInfo, Integer> ageRanges) {
		this.ageRanges = ageRanges;
	}
	/**
	 * @return the cases
	 */
	public List<CaseInfo> getCases() {
		return cases;
	}
	/**
	 * @param cases the cases to set
	 */
	public void setCases(List<CaseInfo> cases) {
		this.cases = cases;
	}
	/**
	 * @return the genders
	 */
	public Map<Gender, Integer> getGenders() {
		return genders;
	}
	/**
	 * @param genders the genders to set
	 */
	public void setGenders(Map<Gender, Integer> genders) {
		this.genders = genders;
	}
	/**
	 * @return the infectionSites
	 */
	public Map<InfectionSite, Integer> getInfectionSites() {
		return infectionSites;
	}
	/**
	 * @param infectionSites the infectionSites to set
	 */
	public void setInfectionSites(Map<InfectionSite, Integer> infectionSites) {
		this.infectionSites = infectionSites;
	}
	/**
	 * @return the firstNamesMale
	 */
	public List<String> getFirstNamesMale() {
		return firstNamesMale;
	}
	/**
	 * @param firstNamesMale the firstNamesMale to set
	 */
	public void setFirstNamesMale(List<String> firstNamesMale) {
		this.firstNamesMale = firstNamesMale;
	}
	/**
	 * @return the firstNamesFemale
	 */
	public List<String> getFirstNamesFemale() {
		return firstNamesFemale;
	}
	/**
	 * @param firstNamesFemale the firstNamesFemale to set
	 */
	public void setFirstNamesFemale(List<String> firstNamesFemale) {
		this.firstNamesFemale = firstNamesFemale;
	}
	/**
	 * @return the lastNames
	 */
	public List<String> getLastNames() {
		return lastNames;
	}
	/**
	 * @param lastNames the lastNames to set
	 */
	public void setLastNames(List<String> lastNames) {
		this.lastNames = lastNames;
	}
	/**
	 * @return the facRegimenStdInd
	 */
	public FactorValue getFacRegimenStdInd() {
		return facRegimenStdInd;
	}
	/**
	 * @param facRegimenStdInd the facRegimenStdInd to set
	 */
	public void setFacRegimenStdInd(FactorValue facRegimenStdInd) {
		this.facRegimenStdInd = facRegimenStdInd;
	}
	/**
	 * @return the numTreatments
	 */
	public Map<NumTreatments, Integer> getNumTreatments() {
		return numTreatments;
	}
	/**
	 * @param numTreatments the numTreatments to set
	 */
	public void setNumTreatments(Map<NumTreatments, Integer> numTreatments) {
		this.numTreatments = numTreatments;
	}
	/**
	 * @return the regions
	 */
	public Map<AdministrativeUnit, Integer> getRegions() {
		return regions;
	}
	/**
	 * @param regions the regions to set
	 */
	public void setRegions(Map<AdministrativeUnit, Integer> regions) {
		this.regions = regions;
	}
	/**
	 * @return the percTransfCases
	 */
	public int getPercTransfCases() {
		return percTransfCases;
	}
	/**
	 * @param percTransfCases the percTransfCases to set
	 */
	public void setPercTransfCases(int percTransfCases) {
		this.percTransfCases = percTransfCases;
	}
	/**
	 * @return the cohortVarTime
	 */
	public int getCohortVarTime() {
		return cohortVarTime;
	}
	/**
	 * @param cohortVarTime the cohortVarTime to set
	 */
	public void setCohortVarTime(int cohortVarTime) {
		this.cohortVarTime = cohortVarTime;
	}
	/**
	 * @return the remExistingCases
	 */
	public boolean isRemExistingCases() {
		return remExistingCases;
	}
	/**
	 * @param remExistingCases the remExistingCases to set
	 */
	public void setRemExistingCases(boolean remExistingCases) {
		this.remExistingCases = remExistingCases;
	}
	/**
	 * @return the localityTypes
	 */
	public Map<LocalityType, Integer> getLocalityTypes() {
		return localityTypes;
	}
	/**
	 * @param localityTypes the localityTypes to set
	 */
	public void setLocalityTypes(Map<LocalityType, Integer> localityTypes) {
		this.localityTypes = localityTypes;
	}
	/**
	 * @return the microscopyFreqTB
	 */
	public int getMicroscopyFreqTB() {
		return microscopyFreqTB;
	}
	/**
	 * @param microscopyFreqTB the microscopyFreqTB to set
	 */
	public void setMicroscopyFreqTB(int microscopyFreqTB) {
		this.microscopyFreqTB = microscopyFreqTB;
	}
	/**
	 * @return the microscopyFreqMDR
	 */
	public int getMicroscopyFreqMDR() {
		return microscopyFreqMDR;
	}
	/**
	 * @param microscopyFreqMDR the microscopyFreqMDR to set
	 */
	public void setMicroscopyFreqMDR(int microscopyFreqMDR) {
		this.microscopyFreqMDR = microscopyFreqMDR;
	}
	/**
	 * @return the cultureFreqTB
	 */
	public int getCultureFreqTB() {
		return cultureFreqTB;
	}
	/**
	 * @param cultureFreqTB the cultureFreqTB to set
	 */
	public void setCultureFreqTB(int cultureFreqTB) {
		this.cultureFreqTB = cultureFreqTB;
	}
	/**
	 * @return the cultureFreqMDR
	 */
	public int getCultureFreqMDR() {
		return cultureFreqMDR;
	}
	/**
	 * @param cultureFreqMDR the cultureFreqMDR to set
	 */
	public void setCultureFreqMDR(int cultureFreqMDR) {
		this.cultureFreqMDR = cultureFreqMDR;
	}
	/**
	 * @return the dstFreqTB
	 */
	public int getDstFreqTB() {
		return dstFreqTB;
	}
	/**
	 * @param dstFreqTB the dstFreqTB to set
	 */
	public void setDstFreqTB(int dstFreqTB) {
		this.dstFreqTB = dstFreqTB;
	}
	/**
	 * @return the dstFreqMDR
	 */
	public int getDstFreqMDR() {
		return dstFreqMDR;
	}
	/**
	 * @param dstFreqMDR the dstFreqMDR to set
	 */
	public void setDstFreqMDR(int dstFreqMDR) {
		this.dstFreqMDR = dstFreqMDR;
	}
	/**
	 * @return the startTreatmentTB
	 */
	public RangeValue getStartTreatmentTB() {
		return startTreatmentTB;
	}
	/**
	 * @param startTreatmentTB the startTreatmentTB to set
	 */
	public void setStartTreatmentTB(RangeValue startTreatmentTB) {
		this.startTreatmentTB = startTreatmentTB;
	}
	/**
	 * @return the startTreatmentMDR
	 */
	public RangeValue getStartTreatmentMDR() {
		return startTreatmentMDR;
	}
	/**
	 * @param startTreatmentMDR the startTreatmentMDR to set
	 */
	public void setStartTreatmentMDR(RangeValue startTreatmentMDR) {
		this.startTreatmentMDR = startTreatmentMDR;
	}
	/**
	 * @return the pulmonaryForms
	 */
	public Map<FieldValue, Integer> getPulmonaryForms() {
		return pulmonaryForms;
	}
	/**
	 * @param pulmonaryForms the pulmonaryForms to set
	 */
	public void setPulmonaryForms(Map<FieldValue, Integer> pulmonaryForms) {
		this.pulmonaryForms = pulmonaryForms;
	}
	/**
	 * @return the extrapulmonaryForms
	 */
	public Map<FieldValue, Integer> getExtrapulmonaryForms() {
		return extrapulmonaryForms;
	}
	/**
	 * @param extrapulmonaryForms the extrapulmonaryForms to set
	 */
	public void setExtrapulmonaryForms(
			Map<FieldValue, Integer> extrapulmonaryForms) {
		this.extrapulmonaryForms = extrapulmonaryForms;
	}
	/**
	 * @return the patientTypesTB
	 */
	public Map<PatientType, Integer> getPatientTypesTB() {
		return patientTypesTB;
	}
	/**
	 * @param patientTypesTB the patientTypesTB to set
	 */
	public void setPatientTypesTB(Map<PatientType, Integer> patientTypesTB) {
		this.patientTypesTB = patientTypesTB;
	}
	/**
	 * @return the patientTypesMDR
	 */
	public Map<PatientType, Integer> getPatientTypesMDR() {
		return patientTypesMDR;
	}
	/**
	 * @param patientTypesMDR the patientTypesMDR to set
	 */
	public void setPatientTypesMDR(Map<PatientType, Integer> patientTypesMDR) {
		this.patientTypesMDR = patientTypesMDR;
	}
	/**
	 * @return the hivFreqTB
	 */
	public int getHivFreqTB() {
		return hivFreqTB;
	}
	/**
	 * @param hivFreqTB the hivFreqTB to set
	 */
	public void setHivFreqTB(int hivFreqTB) {
		this.hivFreqTB = hivFreqTB;
	}
	/**
	 * @return the hivFreqMDR
	 */
	public int getHivFreqMDR() {
		return hivFreqMDR;
	}
	/**
	 * @param hivFreqMDR the hivFreqMDR to set
	 */
	public void setHivFreqMDR(int hivFreqMDR) {
		this.hivFreqMDR = hivFreqMDR;
	}
	/**
	 * @return the hivResults
	 */
	public Map<HIVResult, Integer> getHivResults() {
		return hivResults;
	}
	/**
	 * @param hivResults the hivResults to set
	 */
	public void setHivResults(Map<HIVResult, Integer> hivResults) {
		this.hivResults = hivResults;
	}
	/**
	 * @return the hivFirst
	 */
	public RangeValue getHivFirst() {
		return hivFirst;
	}
	/**
	 * @param hivFirst the hivFirst to set
	 */
	public void setHivFirst(RangeValue hivFirst) {
		this.hivFirst = hivFirst;
	}
	/**
	 * @return the dstResultsTB
	 */
	public Map<DstResult, Integer> getDstResultsTB() {
		return dstResultsTB;
	}
	/**
	 * @param dstResultsTB the dstResultsTB to set
	 */
	public void setDstResultsTB(
			Map<DstResult, Integer> dstResultsTB) {
		this.dstResultsTB = dstResultsTB;
	}
	/**
	 * @return the dstResultsMDR
	 */
	public Map<DstResult, Integer> getDstResultsMDR() {
		return dstResultsMDR;
	}
	/**
	 * @param dstResultsMDR the dstResultsMDR to set
	 */
	public void setDstResultsMDR(
			Map<DstResult, Integer> dstResultsMDR) {
		this.dstResultsMDR = dstResultsMDR;
	}
	public Map<FieldValue, Integer> getXrayPresentation() {
		return xrayPresentation;
	}
	public void setXrayPresentation(Map<FieldValue, Integer> xrayPresentation) {
		this.xrayPresentation = xrayPresentation;
	}
	public RangeValue getXrayNextResult() {
		return xrayNextResult;
	}
	public void setXrayNextResult(RangeValue xrayNextResult) {
		this.xrayNextResult = xrayNextResult;
	}
	public int getPercComorbidities() {
		return percComorbidities;
	}
	public void setPercComorbidities(int percComorbidities) {
		this.percComorbidities = percComorbidities;
	}
	public Map<FieldValue, Integer> getComorbidities() {
		return comorbidities;
	}
	public void setComorbidities(Map<FieldValue, Integer> comorbidities) {
		this.comorbidities = comorbidities;
	}
	public int getPercAdverseReactions() {
		return percAdverseReactions;
	}
	public void setPercAdverseReactions(int percAdverseReactions) {
		this.percAdverseReactions = percAdverseReactions;
	}
	public Map<FieldValue, Integer> getAdverseReactions() {
		return adverseReactions;
	}
	public void setAdverseReactions(Map<FieldValue, Integer> adverseReactions) {
		this.adverseReactions = adverseReactions;
	}
	public int getPercContacts() {
		return percContacts;
	}
	public void setPercContacts(int percContacts) {
		this.percContacts = percContacts;
	}
	public RangeValue getContactsRange() {
		return contactsRange;
	}
	public void setContactsRange(RangeValue contactsRange) {
		this.contactsRange = contactsRange;
	}
	public Map<FieldValue, Integer> getContactType() {
		return contactType;
	}
	public void setContactType(Map<FieldValue, Integer> contactType) {
		this.contactType = contactType;
	}
	public Map<FieldValue, Integer> getXrayPresentationProgress() {
		return xrayPresentationProgress;
	}
	public void setXrayPresentationProgress(
			Map<FieldValue, Integer> xrayPresentationProgress) {
		this.xrayPresentationProgress = xrayPresentationProgress;
	}
	public Map<FieldValue, Integer> getContactConduct() {
		return contactConduct;
	}
	public void setContactConduct(Map<FieldValue, Integer> contactConduct) {
		this.contactConduct = contactConduct;
	}
}
