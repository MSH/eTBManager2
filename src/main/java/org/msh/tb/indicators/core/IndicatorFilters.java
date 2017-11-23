package org.msh.tb.indicators.core;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.*;
import org.msh.tb.tbunits.TBUnitSelection2;
import org.msh.tb.tbunits.TBUnitType;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Name("indicatorFilters")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class IndicatorFilters {
	private static final HIVResult[] hivResults = {HIVResult.NOTDONE, HIVResult.ONGOING, HIVResult.NEGATIVE, HIVResult.POSITIVE };
	
	private int numcases;
	public int getNumcases(){
		return numcases;
	}
	public void setNumcases(int n){
		this.numcases = n;
	}
	
	private Integer iniMonth;
	private Integer iniYear;
	
	private Integer endMonth;
	private Integer endYear;

//	private IndicatorDate indicatorDate = IndicatorDate.DIAGNOSIS_DATE;

	// indicate which dates to filter
	private boolean useRegistrationDate = true;
	private boolean useDiagnosisDate;
	private boolean useIniTreatmentDate;

    private TBUnitSelection2 tbunitselection = new TBUnitSelection2("unitid", false, TBUnitType.HEALTH_UNITS);
	private CaseClassification classification;
    private PatientType patientType;
    private PatientType previouslyTreatedType;
	private InfectionSite infectionSite;
	private Gender gender;
	private Regimen regimen;
	private Source source;
	private Integer numPrevTBTreatments;
	private OutputSelection outputSelection = OutputSelection.ADMINUNIT;
	private int chartType = 1;
	private IndicatorSite indicatorSite = IndicatorSite.TREATMENTSITE;
	private AgeRange ageRange;
	private IndicatorMicroscopyResult microscopyResult;
	private IndicatorCultureResult cultureResult;
	private DrugResistanceType drugResistanceType;
	private DiagnosisType diagnosisType;
	private ValidationState validationState; //= ValidationState.VALIDATED;
	
	private int interimMonths;
	private HIVResult hivResult;
	private List<SelectItem> lstInterimMonths;
	private PatientType patTypFirstTreat;
	private YesNoType supervisedTreatment;
	private Substance substance;
	private HealthSystem healthSystem;
	
	private final static PatientType[] patTypFirstTreatArr = {
		PatientType.NEW,
		PatientType.TRANSFER_IN,
		PatientType.ALL_RETREATMENT,
		};
	private PatientType patTypReTreat;
	private final static PatientType[] patTypReTreatArr = {
		PatientType.RELAPSE,
		PatientType.AFTER_DEFAULT,
		PatientType.FAILURE_FT,
		PatientType.FAILURE_RT,
		PatientType.OTHER,
		};
	@In(required=false) Workspace defaultWorkspace;

	@Observer("change-workspace")
	public void initializeFilters() {
		indicatorSite = IndicatorSite.TREATMENTSITE;
		outputSelection = OutputSelection.ADMINUNIT;
		iniMonth = null;
		iniYear = null;
		endMonth = null;
		endYear = null;
        tbunitselection.setSelected(null);
		classification = null;
		patientType =  null;
		infectionSite = null;
		gender = null;
		numPrevTBTreatments = null;
		drugResistanceType = null;
		healthSystem = null;
	}

	
	/**
	 * Return the initial date based on the initial month and year
	 * @return
	 */
	public Date getIniDate() {
		if (iniYear == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, iniYear);
		if (iniMonth == null)
			 c.set(Calendar.MONTH, 0);
		else c.set(Calendar.MONTH, iniMonth);
		c.set(Calendar.DAY_OF_MONTH, 1);

		return c.getTime();
	}


	/**
	 * Return the ending date based on the ending month and year
	 * @return
	 */
	public Date getEndDate() {
		if (endYear == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, endYear);
		if (endMonth == null)
			 c.set(Calendar.MONTH, 11);
		else c.set(Calendar.MONTH, endMonth);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));

		return c.getTime();
	}
	
	public boolean isPeriodCompleted() {
		return ( (iniYear != null) && (endYear != null));
	}

	public Integer getIniMonth() {
		return iniMonth;
	}
	public void setIniMonth(Integer iniMonth) {
		this.iniMonth = iniMonth;
	}
	public Integer getIniYear() {
		return iniYear;
	}
	public void setIniYear(Integer iniYear) {
		this.iniYear = iniYear;
	}
	public Integer getEndMonth() {
		return endMonth;
	}
	public void setEndMonth(Integer endMonth) {
		this.endMonth = endMonth;
	}
	public Integer getEndYear() {
		return endYear;
	}
	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}
	
	/**
	 * @param chartType the chartType to set
	 */
	public void setChartType(int chartType) {
		this.chartType = chartType;
	}

	/**
	 * @return the chartType
	 */
	public int getChartType() {
		return chartType;
	}

	/**
	 * @param patientType the patientType to set
	 */
	public void setPatientType(PatientType patientType) {
		this.patientType = patientType;
	}

	/**
	 * @return the patientType
	 */
	public PatientType getPatientType() {
		return patientType;
	}

	/**
	 * @param infectionSite the infectionSite to set
	 */
	public void setInfectionSite(InfectionSite infectionSite) {
		this.infectionSite = infectionSite;
	}

	/**
	 * @return the infectionSite
	 */

	public InfectionSite getInfectionSite() {
		return infectionSite;
	}	
	/**
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
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
	 * @return the classification
	 */
	public CaseClassification getClassification() {
		return classification;
	}

	/**
	 * @param classification the classification to set
	 */
	public void setClassification(CaseClassification classification) {
		this.classification = classification;
	}
	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}
	/**
	 * @return the numPrevTBTreatments
	 */
	public Integer getNumPrevTBTreatments() {
		return numPrevTBTreatments;
	}

	/**
	 * @param numPrevTBTreatments the numPrevTBTreatments to set
	 */
	public void setNumPrevTBTreatments(Integer numPrevTBTreatments) {
		this.numPrevTBTreatments = numPrevTBTreatments;
	}

	/**
	 * @param outputSelection the outputSelection to set
	 */
	public void setOutputSelection(OutputSelection outputSelection) {
		this.outputSelection = outputSelection;
	}

	/**
	 * @return the outputSelection
	 */
	public OutputSelection getOutputSelection() {
		return outputSelection;
	}

    /**
     * @return the tbunitselection
     */
    public TBUnitSelection2 getTbunitselection() {
        return tbunitselection;
    }

	/**
	 * @param indicatorSite the indicatorSite to set
	 */
	public void setIndicatorSite(IndicatorSite indicatorSite) {
		this.indicatorSite = indicatorSite;
	}

	/**
	 * @return the indicatorSite
	 */
	public IndicatorSite getIndicatorSite() {
		return indicatorSite;
	}

	public void setOutputSelectionInt(Integer value) {
		if (value == null) {
			outputSelection = null;
			return;
		}
		
		for (OutputSelection sel: OutputSelection.values()) {
			if (sel.ordinal() == value) {
				outputSelection = sel;
				break;
			}
		}
	}
	
	public Integer getOutputSelectionInt() {
		if (outputSelection != null)
			 return outputSelection.ordinal();
		else return null;
	}

	/**
	 * @param ageRange the ageRange to set
	 */
	public void setAgeRange(AgeRange ageRange) {
		this.ageRange = ageRange;
	}

	/**
	 * @return the ageRange
	 */
	public AgeRange getAgeRange() {
		return ageRange;
	}

	/**
	 * @param microscopyResult the microscopyResult to set
	 */
	public void setMicroscopyResult(IndicatorMicroscopyResult microscopyResult) {
		this.microscopyResult = microscopyResult;
	}
	

	/**
	 * @return the microscopyResult
	 */
	public IndicatorMicroscopyResult getMicroscopyResult() {
		return microscopyResult;
	}
	
	public IndicatorMicroscopyResult[] getMicroscopyResults() {
		return IndicatorMicroscopyResult.values();
	}
	
	public IndicatorCultureResult[] getCultureResults() {
		return IndicatorCultureResult.values();
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
	 * @return the diagnosisType
	 */
	public DiagnosisType getDiagnosisType() {
		return diagnosisType;
	}

	/**
	 * @param diagnosisType the diagnosisType to set
	 */
	public void setDiagnosisType(DiagnosisType diagnosisType) {
		this.diagnosisType = diagnosisType;
	}

/*	public IndicatorDate getIndicatorDate() {
		return indicatorDate;
	}

	public void setIndicatorDate(IndicatorDate indicatorDate) {
		this.indicatorDate = indicatorDate;
	}
*/
	/**
	 * @return the cultureResult
	 */
	public IndicatorCultureResult getCultureResult() {
		return cultureResult;
	}

	/**
	 * @param cultureResult the cultureResult to set
	 */
	public void setCultureResult(IndicatorCultureResult cultureResult) {
		this.cultureResult = cultureResult;
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
	 * @return the useRegistrationDate
	 */
	public boolean isUseRegistrationDate() {
		return useRegistrationDate;
	}


	/**
	 * @param useRegistrationDate the useRegistrationDate to set
	 */
	public void setUseRegistrationDate(boolean useRegistrationDate) {
		this.useRegistrationDate = useRegistrationDate;
	}


	/**
	 * @return the useDiagnosisDate
	 */
	public boolean isUseDiagnosisDate() {
		return useDiagnosisDate;
	}


	/**
	 * @param useDiagnosisDate the useDiagnosisDate to set
	 */
	public void setUseDiagnosisDate(boolean useDiagnosisDate) {
		this.useDiagnosisDate = useDiagnosisDate;
	}


	/**
	 * @return the useIniTreatmentDate
	 */
	public boolean isUseIniTreatmentDate() {
		return useIniTreatmentDate;
	}


	/**
	 * @param useIniTreatmentDate the useIniTreatmentDate to set
	 */
	public void setUseIniTreatmentDate(boolean useIniTreatmentDate) {
		this.useIniTreatmentDate = useIniTreatmentDate;
	}
	
	public HIVResult[] getHivResults() {
		return hivResults;
	}


	/**
	 * @return the hivResult
	 */
	public HIVResult getHivResult() {
		return hivResult;
	}


	/**
	 * @param hivResult the hivResult to set
	 */
	public void setHivResult(HIVResult hivResult) {
		this.hivResult = hivResult;
	}
	
	public int getInterimMonths(){
		return interimMonths;
	}
	
	public void setInterimMonths(int interimMonths){
		this.interimMonths = interimMonths;
	}
	
	public YesNoType getSupervisedTreatment() {
		return supervisedTreatment;
	}
	public void setSupervisedTreatment(YesNoType supervisedTreatment) {
		this.supervisedTreatment = supervisedTreatment;
	}
	@Factory("interimMonths")
	public List<SelectItem> getLstInterimMonths() {
		lstInterimMonths = new ArrayList<SelectItem>();
		
		for(int i = 1; i<=24; i++){
		SelectItem item = new SelectItem();
		item.setValue(i);
		item.setLabel(Integer.toString(i));
		lstInterimMonths.add(item);
		}
		return lstInterimMonths;
	}
	
	@Factory("patTypFirstTreatArr")
	public PatientType[] getPatTypFirstTreatArr() {
		//return patientTypIncidenceRep;
		return getComponentValueWorkspace("patTypFirstTreatArr", PatientType[].class, patTypFirstTreatArr);
	}

	@Factory("patTypReTreatArr")
	public PatientType[] getPatTypReTreatArr() {
		return getComponentValueWorkspace("patTypReTreatArr", PatientType[].class, patTypReTreatArr);
	}
	/**
	 * Return a value of a property of a component called enumList + workspace extension
	 * @param <E>
	 * @param propertyName
	 * @param type
	 * @param result
	 * @return
	 */
	protected <E> E getComponentValueWorkspace(String propertyName, Class<E> type, Object result) {
		if ((defaultWorkspace == null) || (defaultWorkspace.getExtension() == null))
			return (E)result;
		
		String s = "globalLists_" + defaultWorkspace.getExtension();
		Object obj = Component.getInstance(s, true);
		
		try {
			E val = (E)PropertyUtils.getProperty(obj, propertyName);
			return (val == null? (E)result: val);
		} catch (Exception e) {
			return (E)result;
		}
	}

	public PatientType getPatTypFirstTreat() {
		return patTypFirstTreat;
	}

	public void setPatTypFirstTreat(PatientType patTypFirstTreat) {
		this.patTypFirstTreat = patTypFirstTreat;
	}

	public PatientType getPatTypReTreat() {
		return patTypReTreat;
	}

	public void setPatTypReTreat(PatientType patTypReTreat) {
		this.patTypReTreat = patTypReTreat;
	}
	public Substance getSubstance() {
		return substance;
	}
	public void setSubstance(Substance substance) {
		this.substance = substance;
	}
	public void setHealthSystem(HealthSystem healthSystem) {
		this.healthSystem = healthSystem;
	}
	public HealthSystem getHealthSystem() {
		return healthSystem;
	}

    public PatientType getPreviouslyTreatedType() {
        return previouslyTreatedType;
    }

    public void setPreviouslyTreatedType(PatientType previouslyTreatedType) {
        this.previouslyTreatedType = previouslyTreatedType;
    }
}
