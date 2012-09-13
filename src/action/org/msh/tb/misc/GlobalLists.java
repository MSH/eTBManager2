package org.msh.tb.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.Identity;
import org.msh.tb.bd.entities.enums.PulmonaryTypesBD;
import org.msh.tb.cases.FilterHealthUnit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.BufferStockMeasure;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseDispensingType;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DispensingFrequency;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.Dot;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.ForecastNewCaseFreq;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.LocalityType;
import org.msh.tb.entities.enums.MedAppointmentType;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.NameComposition;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.entities.enums.SampleType;
import org.msh.tb.entities.enums.TbCategory;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.entities.enums.UserState;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.XRayBaseline;
import org.msh.tb.entities.enums.XRayEvolution;
import org.msh.tb.entities.enums.XRayResult;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.indicators.core.IndicatorDate;
import org.msh.tb.indicators.core.IndicatorSite;

/**
 * Contains common arrays used in several parts of the application
 * @author Ricardo Memória
 *  
 */
@Name("globalLists")
@BypassInterceptors
public class GlobalLists {
//	@In(create=true) Map<String, String> messages;
//	@In(required=false) Workspace defaultWorkspace;

	private List<SelectItem> numberOfAFBs;
	
	private static final PatientType patientTypes[] = {
		PatientType.NEW,
		PatientType.TRANSFER_IN,
		PatientType.RELAPSE,
		PatientType.AFTER_DEFAULT,
		PatientType.FAILURE_FT,
		PatientType.FAILURE_RT,
		PatientType.RESISTANCE_PATTERN_CHANGED,
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
		TbField.EXTRAPULMONARY_TYPES,
		TbField.ADJUSTMENT
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

	private static final HIVResult hivResults[] = {
		HIVResult.ONGOING,
		HIVResult.NEGATIVE,
		HIVResult.POSITIVE
	};
	
	private static final DstResult dstResults[] = {
		DstResult.NOTDONE,
		DstResult.SUSCEPTIBLE,
		DstResult.RESISTANT,
		DstResult.CONTAMINATED
	};

	
	private static final DrugResistanceType drugResistanceTypes[] = {
		DrugResistanceType.MONO_RESISTANCE,
		DrugResistanceType.POLY_RESISTANCE,
		DrugResistanceType.MULTIDRUG_RESISTANCE,
		DrugResistanceType.EXTENSIVEDRUG_RESISTANCE
		
	};
	
	private static final CaseDispensingType caseDispensingTypes[] = {
		CaseDispensingType.DOT,
		CaseDispensingType.NONDOT	
	};

	/**
	 * Get component according to the workspace in use
	 * @param <E>
	 * @param componentName
	 * @param type
	 * @param result
	 * @return
	 */
	protected <E> E getExtensionComponent(String componentName, Class<E> type, Object result) {
		Workspace defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace");

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
		Workspace defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace");
		
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
		Nationality.NATIVE,
		Nationality.FOREIGN
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
	
	@Factory("pulmonaryTypesBD")
	public PulmonaryTypesBD[]  getPulmonaryTypesBD(){
		return PulmonaryTypesBD.values();
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
		for (CaseClassification cla: CaseClassification.values())
			if (identity.hasRole(cla.toString() + "_CASE_VIEW"))
				lst.add(cla);
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
		return getComponentValueWorkspace("hivResults", HIVResult[].class, hivResults);
	}
	
	@Factory("cultureResults")
	public CultureResult[] getCultureResults() {
		return getComponentValueWorkspace("cultureResults", CultureResult[].class, cultureResults);
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
		return getComponentValueWorkspace("dstResults", DstResult[].class, dstResults);
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
		Map<String, String> messages = Messages.instance();

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
	
	//@Factory("drugResistanceTypes")
	//public DrugResistanceType[] getDrugResistanceTypes() {
	//	return DrugResistanceType.values();
	//}
	
	@Factory("drugResistanceTypes")
	public DrugResistanceType[] getDrugResistanceTypes() {
		return getComponentValueWorkspace("drugResistanceTypes", DrugResistanceType[].class, drugResistanceTypes);
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

	
	/**
	 * Return the list of options for the field number of AFBs, from the culture exam
	 * @return
	 */
	@Factory("numberOfAFBs")
	public List<SelectItem> getNumberOfAFBs() {
		if (numberOfAFBs == null) {
			numberOfAFBs = getComponentValueWorkspace("numberOfAFBs", List.class, null);
			if (numberOfAFBs == null) {
				numberOfAFBs = new ArrayList<SelectItem>();
				
				SelectItem item = new SelectItem();
				item.setLabel("-");
				numberOfAFBs.add(item);
				
				for (int i = 1; i <= 9; i++) {
					item = new SelectItem();
					item.setLabel(Integer.toString(i));
					item.setValue(i);
					numberOfAFBs.add(item);
				}
			}
		}
		return numberOfAFBs;
	}


	public static GlobalLists instance(){
		return (GlobalLists)Component.getInstance("globalLists");
	}

	@Factory("caseDispensingTypes")
	public static CaseDispensingType[] getCasedispensingtypes() {
		return caseDispensingTypes;
	}
	
	public Dot[] getDot() {
		return Dot.values();
	}
}
