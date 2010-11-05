package org.msh.tb.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.BufferStockMeasure;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.mdrtb.entities.enums.DiagnosisType;
import org.msh.mdrtb.entities.enums.DispensingFrequency;
import org.msh.mdrtb.entities.enums.DisplayCaseNumber;
import org.msh.mdrtb.entities.enums.DrugResistanceType;
import org.msh.mdrtb.entities.enums.ExtraOutcomeInfo;
import org.msh.mdrtb.entities.enums.ForecastNewCaseFreq;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.HIVResult;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.LocalityType;
import org.msh.mdrtb.entities.enums.MedAppointmentType;
import org.msh.mdrtb.entities.enums.MedicineLine;
import org.msh.mdrtb.entities.enums.NameComposition;
import org.msh.mdrtb.entities.enums.Nationality;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.mdrtb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.mdrtb.entities.enums.SampleType;
import org.msh.mdrtb.entities.enums.MicroscopyResult;
import org.msh.mdrtb.entities.enums.DstResult;
import org.msh.mdrtb.entities.enums.TbCategory;
import org.msh.mdrtb.entities.enums.TbField;
import org.msh.mdrtb.entities.enums.UserState;
import org.msh.mdrtb.entities.enums.UserView;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.mdrtb.entities.enums.XRayBaseline;
import org.msh.mdrtb.entities.enums.XRayEvolution;
import org.msh.mdrtb.entities.enums.XRayResult;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.tb.cases.FilterHealthUnit;
import org.msh.tb.indicators.core.IndicatorDate;
import org.msh.tb.indicators.core.IndicatorSite;

/**
 * Contains common arrays used in several parts of the application
 * @author Ricardo Mem�ria
 *  
 */
@Name("globalLists")
public class GlobalLists {
	@In(create=true) Map<String, String> messages;
	@In(required=false) Workspace defaultWorkspace;
	
	private static final PatientType patientTypes[] = {
		PatientType.NEW,
		PatientType.TRANSFER_IN,
		PatientType.RELAPSE,
		PatientType.AFTER_DEFAULT,
		PatientType.FAILURE_FT,
		PatientType.FAILURE_RT,
		PatientType.OTHER
	};

	private static final CaseState caseStates[] = {
		CaseState.WAITING_TREATMENT,
		CaseState.ONTREATMENT,
		CaseState.TRANSFERRING,
		CaseState.CURED,
		CaseState.TREATMENT_COMPLETED,
		CaseState.FAILED,
		CaseState.DEFAULTED,
		CaseState.DIED,
		CaseState.TRANSFERRED_OUT,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.OTHER,
		CaseState.MDR_CASE,
		CaseState.TREATMENT_INTERRUPTION,
		CaseState.NOT_CONFIRMED, 
		CaseState.DIED_NOTTB
	};
	

	private static final PrevTBTreatmentOutcome prevTBTreatmentOutcomes[] = {
		PrevTBTreatmentOutcome.CURED,
		PrevTBTreatmentOutcome.COMPLETED,
		PrevTBTreatmentOutcome.FAILURE,
		PrevTBTreatmentOutcome.DEFAULTED,
		PrevTBTreatmentOutcome.SCHEME_CHANGED,
		PrevTBTreatmentOutcome.TRANSFERRED_OUT,
		PrevTBTreatmentOutcome.UNKNOWN
	};

	private static final TbField tbfields[] = {
		TbField.COMORBIDITY,
		TbField.SIDEEFFECT,
		TbField.DST_METHOD,
		TbField.CULTURE_METHOD,
		TbField.CONTACTCONDUCT,
		TbField.CONTACTTYPE,
		TbField.XRAYPRESENTATION,
		TbField.PULMONARY_TYPES,
		TbField.EXTRAPULMONARY_TYPES
	};
	
	private static final TbCategory tbcategories[] = {
		TbCategory.CATEGORY_I,
		TbCategory.CATEGORY_II,
		TbCategory.CATEGORY_III
	};
	
	private static final CultureResult cultureResults[] = {
		CultureResult.NEGATIVE,
		CultureResult.POSITIVE,
		CultureResult.PLUS,
		CultureResult.PLUS2,
		CultureResult.PLUS3,
		CultureResult.CONTAMINATED
	};
	
	private static final MicroscopyResult microscopyResults[] = {
		MicroscopyResult.NEGATIVE,
		MicroscopyResult.POSITIVE,
		MicroscopyResult.PLUS,
		MicroscopyResult.PLUS2,
		MicroscopyResult.PLUS3
	};
	
	private static final CultureResult cultureOptionsNotif[] = {
		CultureResult.NOTDONE,
		CultureResult.NEGATIVE,
		CultureResult.POSITIVE,
		CultureResult.PLUS,
		CultureResult.PLUS2,
		CultureResult.PLUS3,
		CultureResult.CONTAMINATED
	};

	private static final MicroscopyResult microscopyNotifOptions[] = {
		MicroscopyResult.NOTDONE,
		MicroscopyResult.NEGATIVE,
		MicroscopyResult.POSITIVE,
		MicroscopyResult.PLUS,
		MicroscopyResult.PLUS2,
		MicroscopyResult.PLUS3
	};

	private CaseClassification caseClassifications[];
	
	
	/**
	 * Get component according to the workspace in use
	 * @param <E>
	 * @param componentName
	 * @param type
	 * @param result
	 * @return
	 */
	protected <E> E getExtensionComponent(String componentName, Class<E> type, Object result) {
		if ((defaultWorkspace == null) || (defaultWorkspace.getExtension() == null))
			return (E)result;
		
		E val = (E)Component.getInstance(componentName.concat("." + defaultWorkspace.getExtension()));
		return (val == null? (E)result: val);
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
	

	/**
	 * List of sample types (sputum or others), used in microscopy and culture exams 
	 * @return
	 */
	public SampleType[] getSampleTypes() {
		return SampleType.values();
	}

	
	/**
	 * Return the validation states used in cases flow
	 * @return
	 */
	public ValidationState[] getValidationState() {
		return ValidationState.values();
	}


	/**
	 * Return dispensing frequencies
	 * @return Array of {@link DispensingFrequency}
	 */
	@Factory("dispensingFrequencies")
	public DispensingFrequency[] createDispensingFrequencies() {
		return DispensingFrequency.values();
	}


	/**
	 * Returns regimen phases (intensive or continuous)
	 * @return - array of RegimenPhase enumerations
	 */
	@Factory("regimenPhases")
	public RegimenPhase[] getRegimenPhase() {
		return RegimenPhase.values();
	}


	@Factory("caseStates")
	public CaseState[] getCaseStates() {
		return (CaseState[])getComponentValueWorkspace("caseStates", CaseState[].class, caseStates);
	}


	/**
	 * Returns options for health unit filter in case searching 
	 * @return - array of FilterHealthUnit enumerations
	 */
	@Factory("filterHealthUnits")
	public FilterHealthUnit[] getFilterHealthUnit() {
		return FilterHealthUnit.values();
	}


	/**
	 * Returns list of TB fields available for drop down menus
	 * @return array of TbField enum
	 */
	@Factory("tbFields")
	public TbField[] getTbFields() {
		return getComponentValueWorkspace("tbFields", TbField[].class, tbfields);
//		return getExtensionComponent("tbFields", TbField[].class, tbfields);
	}


	/**
	 * Returns an array of available gender enumerations
	 * @return array of the enum Gender
	 */
	@Factory("genders")
	public Gender[] getGenders() {
		return Gender.values();
	}


	/**
	 * Returns the localities type (urban or rural)
	 * @return array of enumeration LocalityType
	 */
	@Factory("localityTypes")
	public LocalityType[] getLocalityTypes() {
		return LocalityType.values();
	}


	@Factory("forecastNewCasesFreq")
	public ForecastNewCaseFreq[] getForecastNewCaseFreqs() {
		return ForecastNewCaseFreq.values();
	}
	
	/**
	 * Returns an array of available gender enumerations
	 * @return array of the enum Gender
	 */
	@Factory("medicineLines")
	public MedicineLine[] getMedicineLines() {
		return MedicineLine.values();
	}

	
	/**
	 * Returns an array of weekly frequencies in the format n/7 to be selected in a JSF component like selectOneMenu
	 * @return List of SelectItem objects containing the weekly frequency
	 */
	@Factory("frequencies")
	public List<SelectItem> getFrequencies() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		for (int i = 1; i <= 7; i++) {
			SelectItem it = new SelectItem();
			it.setLabel(Integer.toString(i) + "/7");
			it.setValue((Integer)i);
			lst.add(it);
		}
		return lst;
	}

	private static final Nationality nationalities[] = {
		Nationality.FOREIGN,
		Nationality.NATIVE
	};
	
	
	@Factory("nationalities")
	public Nationality[] getNationalities() {
		return nationalities;
	}
	
	@Factory("patientTypes")
	public PatientType[] getPatientTypes() {
		return getComponentValueWorkspace("patientTypes", PatientType[].class, patientTypes);
	}
	
	@Factory("infectionSites")
	public InfectionSite[] getInfectionSite() {
		return InfectionSite.values();
	}
	
//	@Factory("caseClassifications")
	public CaseClassification[] getCaseClassifications() {
		if (caseClassifications == null) {
			List lst = getUserCaseClassifications();
			caseClassifications = (CaseClassification[])lst.toArray(new CaseClassification[lst.size()]);
		}
		return caseClassifications;
	}
	
	public List<CaseClassification> getUserCaseClassifications() {
		ArrayList<CaseClassification> lst = new ArrayList<CaseClassification>();
		Identity identity = Identity.instance();
		if (identity.hasRole("TBCASES"))
			lst.add(CaseClassification.TB_DOCUMENTED);
		if (identity.hasRole("MDRCASES"))
			lst.add(CaseClassification.MDRTB_DOCUMENTED);
		if (identity.hasRole("NMTCASES"))
			lst.add(CaseClassification.NMT);
		return lst;
	}

	
//	@Factory("prevTBTreatmentOutcomes")
	public PrevTBTreatmentOutcome[] getPrevTBTreatmentOutcomes() {
		return getExtensionComponent("prevTBTreatmentOutcomes", PrevTBTreatmentOutcome[].class, prevTBTreatmentOutcomes);
	}
	
	@Factory("extraOutcomesInfo")
	public ExtraOutcomeInfo[] getExtraOutcomesInfo() {
		return ExtraOutcomeInfo.values();
	}
	
	@Factory("microscopyResults")
	public MicroscopyResult[] getMicroscopyResults() {
		return microscopyResults;
	}
	
	@Factory("caseState")
	public CaseState[] getCaseState() {
		return CaseState.values();
	}
	
	@Factory("hivResults")
	public HIVResult[] getHIVResults() {
		return HIVResult.values();
	}
	
	@Factory("cultureResults")
	public CultureResult[] getCultureResults() {
		return cultureResults;
	}
	
	@Factory("xrayResults")
	public XRayResult[] getXRayResults() {
		return XRayResult.values();
	}
	
	@Factory("bufferStockMeasures")
	public BufferStockMeasure[] getBufferStockMeasures() {
		return BufferStockMeasure.values();
	}
	
	@Factory("dstResults")
	public DstResult[] getDstResults() {
		return DstResult.values();
	}
	
	@Factory("medAppointmentTypes")
	public MedAppointmentType[] getMedAppointmentTypes() {
		return MedAppointmentType.values();
	}
	
	@Factory("weekFrequencies")
	public List<SelectItem> getWeekFrequencies() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		
		SelectItem si = new SelectItem();
		si.setValue(null);
		si.setLabel("-");
		lst.add(si);
		
		for (int i = 1; i <= 7; i++) {
			si = new SelectItem();
			si.setValue(i);
			si.setLabel(i + "/7");
			lst.add(si);
		}
		
		return lst;
	}
	
	@Factory("yesNoList")
	public YesNoType[] getYesNoTypeList() {
		return YesNoType.values();
	}
	
	@Factory("chartTypes")
	public List<SelectItem> getChartTypes() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		lst.add(new SelectItem(1, messages.get("charts.hbar")));
		lst.add(new SelectItem(2, messages.get("charts.pie")));
		return lst;
	}
	
	@Factory("xrayBaselines")
	public XRayBaseline[] getXRayBaselines() {
		return XRayBaseline.values();
	}
	
	@Factory("xrayEvolutions")
	public XRayEvolution[] getXRayEvolutions() {
		return XRayEvolution.values();
	}

	/**
	 * Return array of user states
	 * @return Array of {@link UserState}
	 */
	@Factory("userStates")
	public UserState[] getUserStates() {
		return UserState.values();
	}
	
	/**
	 * Return array of visions in the country available for a user 
	 * @return Array of {@link UserView}
	 */
	@Factory("userViews")
	public UserView[] getUserViews() {
		return UserView.values();
	}
	
	@Factory("indicatorSites")
	public IndicatorSite[] getIndicatorSites() {
		return IndicatorSite.values();
	}
	
	@Factory("namesComposition")
	public NameComposition[] getNamesComposition() {
		return NameComposition.values();
	}
	
	@Factory("tbcategories")
	public TbCategory[] getTbCategories() {
		return tbcategories;
	}
	
	@Factory("diagnosisTypes")
	public DiagnosisType[] getDiagnosisTypes() {
		return DiagnosisType.values();
	}
	
	@Factory("drugResistanceTypes")
	public DrugResistanceType[] getDrugResistanceTypes() {
		return DrugResistanceType.values();
	}
	
	public DisplayCaseNumber[] getDisplayCaseNumbers() {
		return DisplayCaseNumber.values();
	}
	
	public CultureResult[] getCultureNotifOptions() {
		return cultureOptionsNotif;
	}
	
	public MicroscopyResult[] getMicroscopyNotifOptions() {
		return microscopyNotifOptions;
	}
	
	@Factory("indicatorDates")
	public IndicatorDate[] getIndicatorDate() {
		return IndicatorDate.values();
	}
}
