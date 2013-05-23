package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.reports2.variables.AdminUnitVariable;
import org.msh.tb.reports2.variables.AgeRangeVariable;
import org.msh.tb.reports2.variables.CaseItemDateVariable;
import org.msh.tb.reports2.variables.ComorbiditiesVariable;
import org.msh.tb.reports2.variables.CountingExamsVariable;
import org.msh.tb.reports2.variables.CountingVariable;
import org.msh.tb.reports2.variables.DateFieldVariable;
import org.msh.tb.reports2.variables.EnumFieldVariable;
import org.msh.tb.reports2.variables.ExtrapulmonarVariable;
import org.msh.tb.reports2.variables.HivResultVariable;
import org.msh.tb.reports2.variables.LabMethodVariable;
import org.msh.tb.reports2.variables.LabResultDiagVariable;
import org.msh.tb.reports2.variables.MonthOfTreatVariable;
import org.msh.tb.reports2.variables.NegativationMonthVariable;
import org.msh.tb.reports2.variables.PrescMedicineVariable;
import org.msh.tb.reports2.variables.PulmonaryVariable;
import org.msh.tb.reports2.variables.RegimenTypeVariable;
import org.msh.tb.reports2.variables.RegimenVariable;
import org.msh.tb.reports2.variables.SideEffectVariable;
import org.msh.tb.reports2.variables.SuspectConfirmedVariable;
import org.msh.tb.reports2.variables.TypeTBCaseVariable;
import org.msh.tb.reports2.variables.TreatOutcomeVariable;


/**
 * Provide all resources used in a report, i.e, list of available filters and variables.
 * 
 * @author Ricardo Memoria
 *
 */
@Name("reportResources")
@BypassInterceptors
public class ReportResources {

	private List<VariableImpl> variables;
	private List<VariableImpl> filters;
	private List<ReportGroup> groups;
	
	/**
	 * Create all variables available
	 */
	protected void initialize() {
		addCaseDataVariables();
		addCultureExamVariables();
		addMicroscopyExamVariables();
		addDstExamVariables();
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
		add(grp, new EnumFieldVariable("state", "CaseState", "tbcase.state", CaseState.class, "#{globalLists.caseStates}"));
		add(grp, new SuspectConfirmedVariable());
		add(grp, new EnumFieldVariable("val", "ValidationState", "tbcase.validationState", ValidationState.class));
		add(grp, new EnumFieldVariable("res", "DrugResistanceType", "tbcase.drugresistancetype", DrugResistanceType.class));
		add(grp, new EnumFieldVariable("is", "InfectionSite", "tbcase.infectionSite", InfectionSite.class));
		add(grp, new PulmonaryVariable());
		add(grp, new ExtrapulmonarVariable());
		add(grp, new EnumFieldVariable("pt", "PatientType", "tbcase.patientType", PatientType.class, "#{globalLists.patientTypes}"));
		add(grp, new DateFieldVariable("regdate", "TbCase.registrationDate", "tbcase.registrationDate", true));
		add(grp, new DateFieldVariable("diagdate", "TbCase.diagnosisDate", "tbcase.diagnosisDate", true));
		add(grp, new DateFieldVariable("outdate", "TbCase.outcomeDate", "tbcase.outcomeDate", true));
		add(grp, new TreatOutcomeVariable());
		add(grp, new AgeRangeVariable());
		add(grp, new AdminUnitVariable("notifaddr", "Address", "tbcase.notif_adminunit_id"));
		add(grp, new SideEffectVariable("sideeffect"));
		add(grp, new ComorbiditiesVariable());
		return grp;
	}

	/**
	 * Add variables of the treatment section
	 * @return instance of {@link ReportGroup} containing the variables of the case data section
	 */
	protected ReportGroup addTreatmentVariables() {
		ReportGroup grp = addGroup("cases.details.treatment");
		add(grp, new DateFieldVariable("initreat", "TbCase.iniTreatmentDate", "tbcase.initreatmentdate", true));
		add(grp, new DateFieldVariable("endtreat", "TbCase.endTreatmentDate", "tbcase.endtreatmentdate", true));
		add(grp, new MonthOfTreatVariable());
		add(grp, new RegimenVariable());
		add(grp, new RegimenTypeVariable());
		add(grp, new PrescMedicineVariable());
		return grp;
	}
	
	/**
	 * Add variables of the culture exam
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addCultureExamVariables() {
		ReportGroup grp = addGroup("cases.examculture");
		addVariable(grp, new LabResultDiagVariable("cult_resdiag", "manag.reportgen.var.cultdiag", "examculture.result", CultureResult.class));
		addVariable(grp, new CaseItemDateVariable("cultcount", "manag.reportgen.var.culturemonth", "examculture.dateCollected", false));
		addVariable(grp, new CaseItemDateVariable("cultcount_y", "manag.reportgen.var.cultureyear", "examculture.dateCollected", true));
		addFilter(grp, new CaseItemDateVariable("cultcollect", "#{messages['cases.examdst']} - #{messages['PatientSample.dateCollected']}", "examdst.dateCollected", false));
		add(grp, new NegativationMonthVariable("cultneg", true));
		add(grp, new LabMethodVariable("cultmethod", "TbField.CULTURE_METHOD", "examculture.method_id", TbField.CULTURE_METHOD));
		return grp;
	}
	
	/**
	 * Add variables of the microscopy exam
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addMicroscopyExamVariables() {
		ReportGroup grp = addGroup("cases.exammicroscopy");
		add(grp, new LabResultDiagVariable("mic_resdiag", "manag.reportgen.var.micdiag", "exammicroscopy.result", MicroscopyResult.class));
		addVariable(grp, new CaseItemDateVariable("miccount", "manag.reportgen.var.micmonth", "exammicroscopy.dateCollected", false));
		addVariable(grp, new CaseItemDateVariable("miccount_y", "manag.reportgen.var.micyear", "exammicroscopy.dateCollected", true));
		addFilter(grp, new CaseItemDateVariable("miccollect", "#{messages['cases.exammicroscopy']} - #{messages['PatientSample.dateCollected']}", "exammicroscopy.dateCollected", false));
		add(grp, new NegativationMonthVariable("micneg", false));
		return grp;
	}


	/**
	 * Add variables of the DST exam
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addDstExamVariables() {
		ReportGroup grp = addGroup("cases.examdst");
		addVariable(grp, new CaseItemDateVariable("dstcount", "manag.reportgen.var.dstmonth", "examdst.dateCollected", false));
		addVariable(grp, new CaseItemDateVariable("dstcount_y", "manag.reportgen.var.dstyear", "examdst.dateCollected", true));
		addFilter(grp, new CaseItemDateVariable("dstcollect", "#{messages['cases.examdst']} - #{messages['PatientSample.dateCollected']}", "examdst.dateCollected", false));
		add(grp, new LabMethodVariable("dstmethod", "TbField.DST_METHOD", "examdst.method_id", TbField.DST_METHOD));
		return grp;
	}
	
	
	/**
	 * Add HIV exam variables
	 * @return instance of the {@link ReportGroup} variable containing the group of variables
	 */
	protected ReportGroup addHivExamVariables() {
		ReportGroup grp = addGroup("cases.examhiv");
		add(grp, new HivResultVariable());
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
		addVariable(grp, new CountingVariable("totalcases", "manag.ind.numc"));
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
	public VariableImpl findVariableById(String id) {
		for (VariableImpl var: getVariables())
			if (var.getId().equals(id))
				return var;
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
	public void addVariable(ReportGroup group, VariableImpl var) {
		if (variables == null)
			variables = new ArrayList<VariableImpl>();
		variables.add(var);

		if (group.getVariables() == null)
			group.setVariables(new ArrayList<VariableImpl>());

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
	public void addFilter(ReportGroup grp, VariableImpl var) {
		if (filters == null)
			filters = new ArrayList<VariableImpl>();
		filters.add(var);
		
		if (grp.getFilters() == null)
			grp.setFilters(new ArrayList<VariableImpl>());

		grp.getFilters().add(var);
	}
	
	/**
	 * @return
	 */
	public List<VariableImpl> getVariables() {
		if (variables == null)
			initialize();
		return variables;
	}

	/**
	 * @return the filters
	 */
	public List<VariableImpl> getFilters() {
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
}
