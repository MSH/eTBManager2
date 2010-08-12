package org.msh.tb.indicators.core;

import java.util.Calendar;
import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.AgeRange;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.DiagnosisType;
import org.msh.mdrtb.entities.enums.DrugResistanceType;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.tb.indicators.MicroscopyResult;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;

@Name("indicatorFilters")
@Scope(ScopeType.SESSION)
public class IndicatorFilters {
	private Integer iniMonth;
	private Integer iniYear;
	
	private Integer endMonth;
	private Integer endYear;

	private TBUnitSelection tbunitselection = new TBUnitSelection(true, TBUnitFilter.HEALTH_UNITS);
	private CaseClassification classification;
	private PatientType patientType;
	private InfectionSite infectionSite;
	private Gender gender;
	private Regimen regimen;
	private Source source;
	private Integer numPrevTBTreatments;
	private OutputSelection outputSelection = OutputSelection.ADMINUNIT;
	private int chartType = 1;
	private IndicatorSite indicatorSite = IndicatorSite.TREATMENTSITE;
	private AgeRange ageRange;
	private MicroscopyResult microscopyResult;
	private DrugResistanceType drugResistanceType;
	private DiagnosisType diagnosisType;

	@Observer("change-workspace")
	public void initializeFilters() {
		indicatorSite = IndicatorSite.TREATMENTSITE;
		outputSelection = OutputSelection.ADMINUNIT;
		iniMonth = null;
		iniYear = null;
		endMonth = null;
		endYear = null;
		tbunitselection.setTbunit(null);
		classification = null;
		patientType =  null;
		infectionSite = null;
		gender = null;
		numPrevTBTreatments = null;
		drugResistanceType = null;
	}
	
	public Date getIniDate() {
		if (iniYear == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, iniYear);
		if (iniMonth == null)
			 c.set(Calendar.MONTH, 0);
		else c.set(Calendar.MONTH, iniMonth-1);
		c.set(Calendar.DAY_OF_MONTH, 1);

		return c.getTime();
	}
	
	public Date getEndDate() {
		if (endYear == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, endYear);
		if (endMonth == null)
			 c.set(Calendar.MONTH, 11);
		else c.set(Calendar.MONTH, endMonth-1);
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
	public TBUnitSelection getTbunitselection() {
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
	public void setMicroscopyResult(MicroscopyResult microscopyResult) {
		this.microscopyResult = microscopyResult;
	}

	/**
	 * @return the microscopyResult
	 */
	public MicroscopyResult getMicroscopyResult() {
		return microscopyResult;
	}
	
	public MicroscopyResult[] getMicroscopyResults() {
		return MicroscopyResult.values();
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
}
