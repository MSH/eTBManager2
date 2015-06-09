package org.msh.tb.reports2;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;
import org.msh.reports.filters.Filter;
import org.msh.reports.variables.Variable;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.entities.SystemConfig;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.*;
import org.msh.tb.reports2.VariableImpl.UnitType;
import org.msh.tb.reports2.variables.*;
import org.msh.tb.reports2.variables.HivCptArtVariable.ReportType;

import java.util.ArrayList;
import java.util.List;


/**
 * Provide all resources used in a report, i.e, list of available filters and variables.
 * 
 * @author Ricardo Memoria
 *
 */
@Name("reportResources")
@BypassInterceptors
public class ReportResources {

	private List<Variable> variables;
	private List<Filter> filters;
	private List<ReportGroup> groups;
	
	/**
	 * Create all variables available
	 */
	protected void initialize() {
		addCaseDataVariables();
		addMicroscopyExamVariables();
		addCultureExamVariables();
		addDstExamVariables();
		addXpertExamVariables();
		addHivExamVariables();
		addXRayVariables();
		addTreatmentVariables();
		addPrevTreatmentVariables();
//		addMedExaminationVariables();
		addOtherVariables();
	}




	/**
	 * Add variables of the case data section
	 * @return instance of {@link ReportGroup} containing the variables of the case data section
	 */
	protected ReportGroup addCaseDataVariables() {
		ReportGroup grp = addGroup("cases.details.case");
		add(grp, new EnumFieldVariable("gender", "Gender", "patient.gender", Gender.class));
		add(grp, new EnumFieldVariable("nat", "Nationality", "tbcase.nationality", Nationality.class, "#{globalLists.nationalities}"));
		add(grp, new EnumFieldVariable("classif", "CaseClassification", "tbcase.classification", CaseClassification.class));
		add(grp, new TreatOutcomeVariable());
		add(grp, new EnumFieldVariable("state", "CaseState", "tbcase.state", CaseState.class, "#{globalLists.caseStates}"));
		add(grp, new SuspectConfirmedVariable());
		add(grp, new AdminUnitVariable("notifaddr", "Address", "tbcase.notif_adminunit_id"));
		add(grp, new TbunitVariable("notifunit", "TbCase.notificationUnit", "tbcase.notification_unit_id"));
		add(grp, new EnumFieldVariable("val", "ValidationState", "tbcase.validationState", ValidationState.class));
		add(grp, new EnumFieldVariable("res", "DrugResistanceType", "tbcase.drugresistancetype", DrugResistanceType.class, "#{drugResistanceTypes}"));
		add(grp, new EnumFieldVariable("is", "InfectionSite", "tbcase.infectionSite", InfectionSite.class));
		add(grp, new PulmonaryVariable());
		add(grp, new ExtrapulmonarVariable());
		add(grp, new EnumFieldVariable("pt", "PatientType", "tbcase.patientType", PatientType.class, "#{globalLists.patientTypes}"));
		add(grp, new DateFieldVariable("regdate", "TbCase.registrationDate", "tbcase.registrationDate", true));
		addVariable(grp, new DateFieldVariable("regdateM", "#{messages['TbCase.registrationDate']} (#{messages['global.months']})", "tbcase.registrationDate", false));
		add(grp, new DateFieldVariable("diagdate", "TbCase.diagnosisDate", "tbcase.diagnosisDate", true));
		addVariable(grp, new DateFieldVariable("diagdateM", "#{messages['TbCase.diagnosisDate']} (#{messages['global.months']})", "tbcase.diagnosisDate", false));
		add(grp, new DateFieldVariable("outdate", "TbCase.outcomeDate", "tbcase.outcomeDate", true));
		addVariable(grp, new DateFieldVariable("outdateM", "#{messages['TbCase.outcomeDate']} (#{messages['global.months']})", "tbcase.outcomeDate", false));
		add(grp, new AgeRangeVariable());
		add(grp, new SideEffectVariable("sideeffect"));
		add(grp, new ComorbiditiesVariable());
        add(grp, new EnumFieldVariable("caseDef", "CaseDefinition", "tbcase.caseDefinition", CaseDefinition.class));
		return grp;
	}

	/**
	 * Add variables of the treatment section
	 * @return instance of {@link ReportGroup} containing the variables of the case data section
	 */
	protected ReportGroup addTreatmentVariables() {
		ReportGroup grp = addGroup("cases.details.treatment");
		add(grp, new TbunitVariable("treatunit", "FilterHealthUnit.TREATMENT_UNIT", "tbcase.owner_unit_id"));
		add(grp, new DateFieldVariable("initreat", "TbCase.iniTreatmentDate", "tbcase.initreatmentdate", true));
		addVariable(grp, new DateFieldVariable("initreatM", "#{messages['TbCase.iniTreatmentDate']} (#{messages['global.months']})", "tbcase.initreatmentdate", false));
		add(grp, new DateFieldVariable("endtreat", "TbCase.endTreatmentDate", "tbcase.endtreatmentdate", true));
		addVariable(grp, new DateFieldVariable("endtreatM", "#{messages['TbCase.endTreatmentDate']} (#{messages['global.months']})", "tbcase.endtreatmentdate", false));
		add(grp, new MonthOfTreatVariable());
		add(grp, new RegimenVariable());
		add(grp, new RegimenTypeVariable());
		add(grp, new PrescMedicineVariable());
		add(grp, new TreatmentSourceVariable());
		return grp;
	}
	
	/**
	 * Add variables of the culture exam
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addCultureExamVariables() {
		ReportGroup grp = addGroup("manag.reportgen.culture");
		addVariable(grp, new LabResultDiagVariable("cult_resdiag", "manag.reportgen.var.cultdiag", "examculture.result", CultureResult.class, UnitType.DEFAULT));
		addVariable(grp, new LabResultVariable("cult_res", "CultureResult", "examculture.result", CultureResult.class, UnitType.EXAM_CULTURE));
		addVariable(grp, new LabExamDateVariable("cultcount", "manag.reportgen.var.culturemonth", "examculture.dateCollected", false, UnitType.EXAM_CULTURE));
		addVariable(grp, new LabExamDateVariable("cultcount_y", "manag.reportgen.var.cultureyear", "examculture.dateCollected", true, UnitType.EXAM_CULTURE));
		addFilter(grp, new CaseItemDateVariable("cultcollect", "#{messages['manag.reportgen.culture']} - #{messages['manag.reportgen.collect']}", "examculture.dateCollected", false));
		add(grp, new NegativationMonthVariable("cultneg", true));
		add(grp, new LabMethodVariable("cultmethod", "TbField.CULTURE_METHOD", "examculture.method_id", TbField.CULTURE_METHOD, UnitType.EXAM_CULTURE));
        add(grp, new CultureResultGroupVariable());
		return grp;
	}
	
	/**
	 * Add variables of the microscopy exam
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addMicroscopyExamVariables() {
		ReportGroup grp = addGroup("manag.reportgen.microscopy");
		add(grp, new LabResultDiagVariable("mic_resdiag", "manag.reportgen.var.micdiag", "exammicroscopy.result", MicroscopyResult.class, UnitType.DEFAULT));
		add(grp, new LabResultVariable("mic_res", "MicroscopyResult", "exammicroscopy.result", MicroscopyResult.class, UnitType.EXAM_MICROSCOPY));
		addVariable(grp, new LabExamDateVariable("miccount", "manag.reportgen.var.micmonth", "exammicroscopy.dateCollected", false, UnitType.EXAM_MICROSCOPY));
		addVariable(grp, new LabExamDateVariable("miccount_y", "manag.reportgen.var.micyear", "exammicroscopy.dateCollected", true, UnitType.EXAM_MICROSCOPY));
		addFilter(grp, new CaseItemDateVariable("miccollect", "#{messages['manag.reportgen.microscopy']} - #{messages['manag.reportgen.collect']}", "exammicroscopy.dateCollected", false));
		add(grp, new NegativationMonthVariable("micneg", false));
		return grp;
	}


	/**
	 * Add specific variables for Expert exams
	 * @return
	 */
	protected ReportGroup addXpertExamVariables() {
		ReportGroup grp = addGroup("cases.examxpert");
		add(grp, new LabResultDiagVariable("genx_diag", "manag.reportgen.var.xpertdiag", "examxpert.result", XpertResult.class, UnitType.EXAM_XPERT));
		add(grp, new LabResultVariable("genx_res", "XpertResult", "examxpert.rifResult", XpertRifResult.class, UnitType.EXAM_XPERT));
		addFilter(grp, new CaseItemDateVariable("xp-collect", "#{messages['manag.reportgen.xpert']} - #{messages['manag.reportgen.collect']}", "exammicroscopy.dateCollected", false));
		return grp;
	}


	/**
	 * Add variables of the DST exam
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addDstExamVariables() {
		ReportGroup grp = addGroup("manag.reportgen.dst");
		add(grp, new ResistancePatternVariable("dstpatt", false));
		add(grp, new ResistancePatternVariable("dstpatt_diag", true));
		addVariable(grp, new LabExamDateVariable("dstcount", "manag.reportgen.var.dstmonth", "examdst.dateCollected", false, UnitType.EXAM_DST));
		addVariable(grp, new LabExamDateVariable("dstcount_y", "manag.reportgen.var.dstyear", "examdst.dateCollected", true, UnitType.EXAM_DST));
		addFilter(grp, new CaseItemDateVariable("dstcollect", "#{messages['manag.reportgen.dst']} - #{messages['manag.reportgen.collect']}", "examdst.dateCollected", false));
		add(grp, new LabMethodVariable("dstmethod", "TbField.DST_METHOD", "examdst.method_id", TbField.DST_METHOD, UnitType.EXAM_DST));
		return grp;
	}
	
	
	/**
	 * Add HIV exam variables
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addHivExamVariables() {
		ReportGroup grp = addGroup("cases.examhiv");
		add(grp, new HivResultVariable());
		add(grp, new HivCptArtVariable(ReportType.ART_REPORT));
		add(grp, new HivCptArtVariable(ReportType.CPT_REPORT));
		return grp;
	}

	
	/**
	 * Add variables of x-ray tests
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addXRayVariables() {
		ReportGroup grp = addGroup("cases.examxray");
		return grp;
	}

	
	/**
	 * Add previous treatment variables
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addPrevTreatmentVariables() {
		ReportGroup grp = addGroup("cases.prevtreat");
		add(grp, new TypeTBCaseVariable());
		return grp;
	}
	
	
	/**
	 * Add medical examination variables
	 * @return
	 */
	protected ReportGroup addMedExaminationVariables() {
		ReportGroup grp = addGroup("cases.details.medexam");
		return grp;
	}

	
	protected ReportGroup addOtherVariables() {
		ReportGroup grp = addGroup("manag.reportgen.var.groupothers");
		addVariable(grp, new CountingVariable("totalcases", "manag.ind.numc", UnitType.CASE_ONLY));
		addVariable(grp, new CountingExamsVariable());
		return grp;
	}

	/**
	 * Remove a variable from the list of filters and variables
	 * @param varid
	 */
	public void removeVariable(String varid) {
		
	}
	
	/**
	 * Search a variable by its id
	 * @param id
	 * @return
	 */
	public Variable findVariableById(String id) {
		for (Variable var: getVariables())
			if (var.getId().equals(id))
				return var;
		return null;
	}
	
	
	/**
	 * Search a filter by its ID
	 * @param id the ID of the filter
	 * @return implementation of the {@link Filter} interface or null if filter was not found
	 */
	public Filter findFilterById(String id) {
		for (Filter filter: getFilters()) {
			if (filter.getId().equals(id)) {
				return filter;
			}
		}
		return null;
	}
	
	
	/**
	 * Add report group
	 * @param key
	 * @return
	 */
	public ReportGroup addGroup(String key) {
		ReportGroup grp = new ReportGroup(key);
		if (groups == null)
			groups = new ArrayList<ReportGroup>();
		groups.add(grp);
		return grp;
	}
	
	/**
	 * Add a new variable
	 * @param var
	 */
	public void addVariable(ReportGroup group, Variable var) {
		if (variables == null)
			variables = new ArrayList<Variable>();
		variables.add(var);

		if (group.getVariables() == null)
			group.setVariables(new ArrayList<Variable>());

		group.getVariables().add(var);
	}

	
	/**
	 * Add a new variable and filter
	 * @param group
	 * @param var
	 */
	public void add(ReportGroup group, VariableImpl var) {
		addVariable(group, var);
		addFilter(group, var);
	}

	/**
	 * Add a new filter
	 * @param var
	 */
	public void addFilter(ReportGroup grp, Filter var) {
		if (filters == null)
			filters = new ArrayList<Filter>();
		filters.add(var);
		
		if (grp.getFilters() == null)
			grp.setFilters(new ArrayList<Filter>());

		grp.getFilters().add(var);
	}
	
	/**
	 * @return
	 */
	public List<Variable> getVariables() {
		if (variables == null)
			initialize();
		return variables;
	}

	/**
	 * @return the filters
	 */
	public List<Filter> getFilters() {
		if (filters == null)
			initialize();
		return filters;
	}

	/**
	 * @return the groups
	 */
	public List<ReportGroup> getGroups() {
		if (groups == null)
			initialize();
		return groups;
	}
	
	
	/**
	 * Return the instance of the {@link ReportResources} component. The class may be reimplemented
	 * in a workspace by declaring a component with name <code>"reportResource.wsext"</code>, where
	 * <code>wsext</code> is the workspace extension 
	 * @return instance of {@link ReportResources} class
	 */
	public static ReportResources instance() {
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace");
		if ((ws != null) && (ws.getExtension() != null)) {
			ReportResources res = (ReportResources)Component.getInstance("reportResources." + ws.getExtension());
			if (res != null)
				return res;
		}
		return (ReportResources)Component.getInstance("reportResources");
	}


    /**
     * Prepare the environment for the execution of the dashboard. The dashboard can
     * be generated in a public view, so some variables must be included in the event scope
     */
    public void prepareDashboard() {
        if (Identity.instance().isLoggedIn()) {
            return;
        }

        SystemConfig cfg = EtbmanagerApp.instance().getConfiguration();
        if (cfg.getPubDashboardWorkspace() == null) {
            throw new RuntimeException("Not authorized");
        }

        Contexts.getEventContext().set("defaultWorkspace", cfg.getPubDashboardWorkspace());

        UserWorkspace uw = new UserWorkspace();
        uw.setWorkspace(cfg.getPubDashboardWorkspace());
        uw.setView(UserView.COUNTRY);

        Contexts.getEventContext().set("userWorkspace", uw);
    }
}
