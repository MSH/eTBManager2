package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.reports2.variables.AdminUnitVariable;
import org.msh.tb.reports2.variables.AgeRangeVariable;
import org.msh.tb.reports2.variables.DateFieldVariable;
import org.msh.tb.reports2.variables.MonthOfTreatVariable;
import org.msh.tb.reports2.variables.PrescMedicineVariable;
import org.msh.tb.reports2.variables.RegimenTypeVariable;
import org.msh.tb.reports2.variables.RegimenVariable;
import org.msh.tb.reports2.variables.TreatOutcomeVariable;


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
		// case data variables
		ReportGroup grp = addGroup("cases.details.case");
		addVariable(grp, new VariableImpl("gender", "Gender", "patient.gender", Gender.values()), true);
		addVariable(grp, new VariableImpl("nat", "Nationality", "tbcase.nationality", Nationality.values()), true);
		addVariable(grp, new VariableImpl("cla", "CaseClassification", "tbcase.classification", CaseClassification.values()), true);
		addVariable(grp, new VariableImpl("sta", "CaseState", "tbcase.state", CaseState.values()), true);
		addVariable(grp, new TreatOutcomeVariable(), true);
		addVariable(grp, new VariableImpl("val", "ValidationState", "tbcase.validationState", ValidationState.values()), true);
		addVariable(grp, new VariableImpl("res", "DrugResistanceType", "tbcase.drugresistancetype", DrugResistanceType.values()), true);
		addVariable(grp, new VariableImpl("is", "InfectionSite", "tbcase.infectionSite", InfectionSite.values()), true);
		addVariable(grp, new VariableImpl("pt", "PatientType", "tbcase.patientType", PatientType.values()), true);
		addVariable(grp, new DateFieldVariable("regdate", "TbCase.registrationDate", "tbcase.registrationDate", true), false);
		addVariable(grp, new DateFieldVariable("diagdate", "TbCase.diagnosisDate", "tbcase.diagnosisDate", true), false);
		addVariable(grp, new DateFieldVariable("outdate", "TbCase.outcomeDate", "tbcase.outcomeDate", true), false);
		addVariable(grp, new AgeRangeVariable(), true);
		addVariable(grp, new AdminUnitVariable("notifaddress", "Address", "tbcase.notif_adminunit_id"), true);
		addVariable(grp, new MonthOfTreatVariable(), true);
		addVariable(grp, new RegimenVariable(), true);
		addVariable(grp, new RegimenTypeVariable(), true);
		addVariable(grp, new PrescMedicineVariable(), true);

		grp = addGroup("cases.exammicroscopy");
		addVariable(grp, new VariableImpl("tst1", "Test 1", "", null), true);
//		addVariable(grp, new VariableImpl("mic.date", "PatientSample.dateCollected", "exammicroscopy.dateCollected", null), true);
//		addVariable(grp, new VariableImpl("mic.lab", "Laboratory", "exammicroscopy.sample_id", null), true);

		grp = addGroup("cases.examculture");
		addVariable(grp, new VariableImpl("tst1", "Test 1", "", null), true);
//		addVariable(grp, new VariableImpl("cul.date", "PatientSample.dateCollected", "exammicroscopy.dateCollected", null), true);
//		addVariable(grp, new VariableImpl("cul.lab", "Laboratory", "exammicroscopy.sample_id", null), true);

		grp = addGroup("cases.examdst");
		addVariable(grp, new VariableImpl("tst1", "Test 1", "", null), true);
//		addVariable(grp, new VariableImpl("dst.date", "PatientSample.dateCollected", "exammicroscopy.dateCollected", null), true);
//		addVariable(grp, new VariableImpl("dst.lab", "Laboratory", "exammicroscopy.sample_id", null), true);

		grp = addGroup("cases.examhiv");
		addVariable(grp, new VariableImpl("tst1", "Test 1", "", null), true);
//		addVariable(grp, new VariableImpl("hiv.date", "PatientSample.dateCollected", "exammicroscopy.dateCollected", null), true);
//		addVariable(grp, new VariableImpl("hiv.lab", "Laboratory", "exammicroscopy.sample_id", null), true);

		grp = addGroup("cases.examxray");
		addVariable(grp, new VariableImpl("tst1", "Test 1", "", null), true);

		grp = addGroup("cases.prevtreat");
		addVariable(grp, new VariableImpl("tst1", "Test 1", "", null), true);

		grp = addGroup("cases.details.medexam");
		addVariable(grp, new VariableImpl("tst1", "Test 1", "", null), true);
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
	public void addVariable(ReportGroup group, VariableImpl var, boolean filter) {
		if (variables == null)
			variables = new ArrayList<VariableImpl>();
		variables.add(var);

		if (group.getVariables() == null)
			group.setVariables(new ArrayList<VariableImpl>());

		group.getVariables().add(var);
		
		if (filter)
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
