package org.msh.tb.indicators.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.framework.Controller;
import org.jboss.seam.international.Messages;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.MicroscopyResult;

/**
 * Base class to generate HQL queries in the TB Cases database based on the filters in the {@link IndicatorFilters} component
 * @author Ricardo Memoria
 *
 */
public class CaseHQLBase extends Controller {
	private static final long serialVersionUID = -1817340544631871046L;

	private String groupFields;
	private String orderByFields;
	private String condition;
	private boolean consolidated;
	private CaseState caseState;
	private IndicatorFilters indicatorFilter;
	private boolean newCasesOnly = true;
	private boolean outputSelected = false;
	private String hqlSelect;
	
	private List<SelectItem> outputSelections;


	/**
	 * Add an HQL condition to the HQL instruction 
	 * @param hql HQL instruction
	 * @param condition HQL condition to be included in the instruction
	 * @return HQL instruction with the condition included
	 */
	private String addCondition(String hql, String condition) {
		if (condition == null)
			return hql;
		
		return hql + " and " + condition;
	}


	/**
	 * Create HQL instruction to query the database for an specific indicator (based on the filters)
	 * @return String containing HQL instruction
	 */
	protected String createHQL() {
		String s = getHQLSelect();
		
		String hql = getHQLFrom() + " ";
		if (s != null)
			hql = s + " " + hql;
		
		String joins = getHQLJoin();
		if (joins != null)
			hql += joins + " ";

		hql += getHQLWhere();
			
		if (groupFields != null)
			hql += " group by " + groupFields + " order by " + groupFields;
		
		return hql;		
	}

	
	protected String getHQLFrom() {
		return "from TbCase c";
	}


	/**
	 * Return HQL instruction 
	 * @return
	 */
	protected String createHQLByOutputSelection() {
		IndicatorFilters filters = getIndicatorFilters();
		OutputSelection out = filters.getOutputSelection();
		
		if (out == null)
			return null;
		
		String fields = getOutputSelectionField(out);

		// check if the filter is compatible with the output
		if ((filters.getOutputSelection() == OutputSelection.PULMONARY) || (filters.getOutputSelection() == OutputSelection.EXTRAPULMONARY)) {
			if (filters.getOutputSelection() == OutputSelection.PULMONARY)
				 filters.setInfectionSite(InfectionSite.PULMONARY);
			else filters.setInfectionSite(InfectionSite.EXTRAPULMONARY);
		}

		setGroupFields(fields);
		return createHQL();
	}


	/**
	 * Return the field of the HQL query associated to the output selected
	 * @param output
	 * @return
	 */
	protected String getOutputSelectionField(OutputSelection output) {
		switch (output) {
		case GENDER: return "c.patient.gender";
		case INFECTION_SITE: return "c.infectionSite";
		case PATIENT_TYPE: return "c.patientType";
		case ADMINUNIT:
			if (getIndicatorFilters().getIndicatorSite() == IndicatorSite.PATIENTADDRESS)
				 return "c.notifAddress.adminUnit.code";
			else return "c.notificationUnit.adminUnit.code";
		case TBUNIT: return "c.notificationUnit.name.name1";
		case NATIONALITY: return "c.nationality";
		case AGERANGE: return "c.age";
		case PULMONARY: return "c.pulmonaryType.name";
		case EXTRAPULMONARY: return "c.extrapulmonaryType.name";
		}
		return null;
	}
	
	/**
	 * Create HQL WHERE instruction to return the values selected in the {@link IndicatorFilters} component 
	 * @return HQL WHERE instruction
	 */
	protected String getHQLWhere() {
		String hql = "where c.notificationUnit.workspace.id = " + getWorkspace().getId().toString();
		
		IndicatorFilters filters = getIndicatorFilters();

		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if (userWorkspace.getHealthSystem() != null)
			hql += " and c.notificationUnit.healthSystem.id = " + userWorkspace.getHealthSystem().getId();
//		hql += " and c.notificationUnit.healthSystem.id = #{userWorkspace.tbunit.healthSystem.id}";

		if (getClassification() != null)
			hql += " and c.classification = :classification";

		// include filter by gender
		if (filters.getGender() != null)
			hql += " and c.patient.gender = #{indicatorFilters.gender}";
		
		// include filter by regimen
		if (filters.getRegimen() != null)
			hql += " and c.regimen.id = #{indicatorFilters.regimen.id}";
		
		// include filter by patient type
		if (filters.getPatientType() != null)
			hql += " and c.patientType = #{indicatorFilters.patientType}";
		
		// include filter by source
		if (filters.getSource() != null)
			hql += " and exists(select pm.id from PrescribedMedicine pm where pm.source.id = #{indicatorFilters.source.id} and pm.tbcase.id = c.id)";

		// include filter by infectionSite
		String s = getHQLInfectionSite();
		if (s != null)
			hql += " and " + s;
		
		// 39- include filter by supervised
		//if (filters.getSupervisedTreatment() != null)
		//	hql += " and c.getSupervised = #{indicatorFilters.supervisedTreatment}";
		
		if (filters.getAgeRange() != null) {
			hql += " and c.age between #{indicatorFilters.ageRange.iniAge} and #{indicatorFilters.ageRange.endAge}";
		}
		
		if (filters.getDrugResistanceType() != null) {
			hql += " and c.drugResistanceType = #{indicatorFilters.drugResistanceType}";
		}
		
		if ((isUseDiagnosisTypeFilter()) && (filters.getDiagnosisType() != null)) {
			hql += " and c.diagnosisType = #{indicatorFilters.diagnosisType}";
		}

		// add filters by culture
		s = getHQLCultureCondition();
		if (s != null)
			hql += " and " + s;

		// add filters by HIV result
		s = getHQLHivResultCondition();
		if (s != null)
			hql += " and " + s;
		
		// add filters by microscopy
		s = getHQLMicroscopyCondition();
		if (s != null)
			hql += " and " + s;
		
		s = getHQLValidationState();
		if (s != null)
			hql += " and " + s;
		
		// include filter by unit
		Tbunit unit = filters.getTbunitselection().getTbunit();
		if (unit != null)
			hql += " and c.notificationUnit.id = " + unit.getId().toString();
		
		hql = addCondition(hql, getCaseStateCondition());
		hql = addCondition(hql, getPeriodCondition());
		hql = addCondition(hql, getAdminUnitCondition());
		hql = addCondition(hql, condition);
		return hql;
	}


	/**
	 * Return HQL condition to the infection site filter 
	 * @return HQL condition of the infection site filter selection, otherwise returns null if infection site not selected
	 */
	protected String getHQLInfectionSite() {
		if (getIndicatorFilters().getInfectionSite() != null) 
			 return "c.infectionSite = #{indicatorFilters.infectionSite}";
		else return null;
	}


	/**
	 * Return HQL condition of the validation state of the cases to be selected
	 * @return
	 */
	protected String getHQLValidationState() {
		if (getIndicatorFilters().getValidationState() != null) 
			 return "c.validationState = #{indicatorFilters.validationState}";
		else return null;
	}


	/**
	 * Return microscopy HQL condition depending on the microscopy filter
	 * @return if there is a filter (positive or negative) return HQL condition, otherwise, return null
	 */
	protected String getHQLMicroscopyCondition() {
		IndicatorFilters filters = getIndicatorFilters();
		if (filters.getMicroscopyResult() == null)
			return null;
		
		String cond = getHQLMicroscopyResultCondition(filters.getMicroscopyResult());

		return "exists(select ex.id from ExamMicroscopy ex where ex.result " + cond + " and ex.tbcase.id = c.id" +
		" and ex.dateCollected = (select min(sp.dateCollected) from ExamMicroscopy sp where sp.tbcase.id = c.id))";
	}


	/**
	 * Return the condition to be used in a HQL expression to include all microscopy results positive or negative
	 * @param result indicates if it shall return HQL condition to include all positive or negative results
	 * @return HQL expression to be used as condition
	 */
	protected String getHQLMicroscopyResultCondition(IndicatorMicroscopyResult result) {
		MicroscopyResult[] lst;
		if (result.equals(IndicatorMicroscopyResult.POSITIVE))
			 lst = MicroscopyResult.getPositiveResults();
		else lst = MicroscopyResult.getNegativeResults();
		
		if (lst.length == 0)
			return null;
		
		String cond = null;
		if (lst.length == 1) {
			cond = " = " + lst[0].ordinal();
		}
		else {
			for (MicroscopyResult res: lst) {
				if (cond != null)
					 cond += ",";
				else cond = " in ("; 
				cond += res.ordinal();
			}
			cond += ")";
		}
		return cond;
	}

	
	/**
	 * Return microscopy HQL condition depending on the microscopy filter
	 * @return if there is a filter (positive or negative) return HQL condition, otherwise, return null
	 */
	protected String getHQLCultureCondition() {
		IndicatorFilters filters = getIndicatorFilters();
		if (filters.getCultureResult() == null)
			return null;
		
		String cond = getHQLCultureResultCondition(filters.getCultureResult());

		return "exists(select ex.id from ExamCulture ex where ex.result " + cond + " and ex.tbcase.id = c.id" +
		" and ex.dateCollected = (select min(sp.dateCollected) from ExamCulture sp where sp.tbcase.id = c.id))";
	}


	/**
	 * Return the condition to be used in a HQL expression to include all culture exam results positive or negative
	 * @param result indicates if it shall return HQL condition to include all positive or negative results
	 * @return HQL expression to be used as condition in culture exams
	 */
	protected String getHQLCultureResultCondition(IndicatorCultureResult result) {
		CultureResult[] lst;
		if (result.equals(IndicatorCultureResult.POSITIVE))
			 lst = CultureResult.getPositiveResults();
		else lst = CultureResult.getNegativeResults();
		
		if (lst.length == 0)
			return null;
		
		String cond = null;
		if (lst.length == 1) {
			cond = " = " + lst[0].ordinal();
		}
		else {
			for (CultureResult res: lst) {
				if (cond != null)
					 cond += ",";
				else cond = " in (";
				cond += res.ordinal();
			}
			cond += ")";
		}
		return cond;
	}

	
	/**
	 * Generate HQL restriction by HIV result
	 * @return
	 */
	public String getHQLHivResultCondition() {
		IndicatorFilters filters = getIndicatorFilters();
		
		HIVResult result = filters.getHivResult();
		
		if (result == null)
			return null;
		
		String cond = null;

		switch (result) {
		case NOTDONE: cond = "not exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id = c.id )";
			break;

		case NEGATIVE: cond = "exists(select id from ExamHIV where tbcase.id=c.id and result=" + HIVResult.NEGATIVE.ordinal() + ") and exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id=c.id and hiv.result!=" + HIVResult.POSITIVE.ordinal() + ")";
			break;

		case POSITIVE: cond = "exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id=c.id and hiv.result=" + HIVResult.POSITIVE.ordinal() + ")";
			break;

		case ONGOING: cond = "not exists(select id from ExamHIV where tbcase.id=c.id and result!=" + HIVResult.ONGOING.ordinal() + ") and exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id=c.id and hiv.result=" + HIVResult.ONGOING.ordinal() + ")";
		}
		
		return cond;
	}
	
	
	/**
	 * Create an HQL query and set the default parameters
	 * @return {@link Query} instance
	 */
	public Query createQuery() {
		String hql = createHQL();

		Query query = getEntityManager().createQuery(hql);
		setQueryParameters(query);
		return query;
	}



	/**
	 * @return the caseState
	 */
	public CaseState getCaseState() {
		return caseState;
	}
	

	/**
	 * Get the HQL condition for case state
	 * @return Case state condition
	 */
	protected String getCaseStateCondition() {
		if (caseState != null)
			 return "c.state = :state";
		else return null;
	}


	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}


	/**
	 * @return the EntityManager instance available
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)getComponentInstance("entityManager");
	}

	
	/**
	 * @return the groupFields
	 */
	public String getGroupFields() {
		return groupFields;
	}


	
	/**
	 * Return the HQL join instruction to be included in the HQL instruction
	 * @return HQL join instruction
	 */
	protected String getHQLJoin() {
		if (!consolidated)
			 return "join fetch c.patient p";
		else {
			return null;
		}
	}


	/**
	 * Return the HQL select instruction, based on the {@link #isConsolidated()} return value
	 * @return HQL select instruction
	 */
	protected String getHQLSelect() {
		if (consolidated)
			 return "select " + (groupFields != null? groupFields + ", ": "") + "count(*)";
		else return hqlSelect;
	}
	
	
	protected void setHQLSelect(String hqlSelect) {
		this.hqlSelect = hqlSelect;
	}
	
	
	/**
	 * Return the filters selected by the user
	 * @return instance of the {@link IndicatorFilters IndicatorFilters} class
	 */
	protected IndicatorFilters getIndicatorFilters() {
		if (indicatorFilter == null)
			indicatorFilter = (IndicatorFilters)Contexts.getSessionContext().get("indicatorFilters"); 
		return indicatorFilter;
	}


	
	/**
	 * @return the orderByFields
	 */
	public String getOrderByFields() {
		return orderByFields;
	}


	
	/**
	 * Return the display value of the {@link OutputSelection} parameter
	 * @param output
	 * @return string display representation of the parameter output
	 */
	public String getDisplayOutputSelection(OutputSelection output) {
		if (output == null)
			return null;
		
		for (SelectItem item: getOutputSelections()) {
			if (item.getValue().equals(output.ordinal())) {
				return item.getLabel();
			}
		}
		return null;		
	}


	
	/**
	 * Return the display value of the selected output
	 * @return String to be displayed as the selected output
	 */
	public String getDisplayOutputSelection() {
		IndicatorFilters filters = getIndicatorFilters();
		OutputSelection output = filters.getOutputSelection();
	
		return getDisplayOutputSelection(output);
	}
	
	
	/**
	 * Return the options of output to be selected by the user 
	 * @return Array of {@link OutputSelection} enumeration
	 */
	public List<SelectItem> getOutputSelections() {
		if (outputSelections == null) {
			IndicatorFilters filters = getIndicatorFilters();
			Map<String, String> messages = Messages.instance();
			
			outputSelections = new ArrayList<SelectItem>();
			
			// checks administrative unit selection
			AdminUnitSelection auselection = filters.getTbunitselection().getAuselection();
			AdministrativeUnit adminUnit = auselection.getSelectedUnit();

			String labelAdminUnit = null;
			boolean hasAdminUnit = true;
			
			if (adminUnit != null) {
				hasAdminUnit = adminUnit.getUnitsCount() > 0;
				labelAdminUnit = auselection.getLabelLevel(adminUnit.getLevel() + 1);
			}
			else labelAdminUnit = auselection.getLabelLevel(1);
			
			// mount the list of options
			for (OutputSelection sel: OutputSelection.values()) {
				if ((sel != OutputSelection.ADMINUNIT) || ((sel == OutputSelection.ADMINUNIT) && (hasAdminUnit))) {
					SelectItem item = new SelectItem();
					item.setValue(sel.ordinal());
					if (sel == OutputSelection.ADMINUNIT)
						 item.setLabel(labelAdminUnit);
					else item.setLabel(messages.get(sel.getKey()));
					outputSelections.add(item);
				}
			}
		}
		return outputSelections;
	}

	
	/**
	 * Generate HQL period condition using the field passed by argument
	 * @param field
	 * @return
	 */
	private String getPeriodConditionByField(String field) {
		IndicatorFilters filters = getIndicatorFilters();

		if (filters.isPeriodCompleted()) {
			if (newCasesOnly)
				 return field + " between #{indicatorFilters.iniDate} and #{indicatorFilters.endDate}";
			else return field + " <= #{indicatorFilters.endDate} and c.treatmentPeriod.endDate > #{indicatorFilters.iniDate}";
		}
		else {
			if (filters.getIniDate() != null) {
				if (newCasesOnly)
					 return field + " >= #{indicatorFilters.iniDate}";
				else return "(c.treatmentPeriod.endDate >= #{indicatorFilters.iniDate} or c.treatmentPeriod.endDate is null)";
			}
			else
			if (filters.getEndDate() != null) {
				if (newCasesOnly)
					return field +" <= #{indicatorFilters.endDate}";
				else return "c.diagnosisDate <= #{indicatorFilters.endDate}";
			}
		}
		return null;
	}


	/**
	 * Get the HQL declaration to filter by region
	 * @return
	 */
	protected String getPeriodCondition() {
		IndicatorFilters filters = getIndicatorFilters();

		if ((filters.getIniDate() == null) && (filters.getEndDate() == null))
			return null;

		String cond = null;

		if (filters.isUseDiagnosisDate())
			cond = getPeriodConditionByField("c.diagnosisDate");
		
		if (filters.isUseRegistrationDate()) {
			String s = getPeriodConditionByField("c.registrationDate");
			if (cond != null)
				cond += " and " + s;
			else cond = s;
		}
		
		if (filters.isUseIniTreatmentDate()) {
			String s = getPeriodConditionByField("c.treatmentPeriod.iniDate");
			if (cond != null)
				cond += " and " + s;
			else cond = s;
		}
		
		return cond;
	}
	
	
	/**
	 * Get the HQL declaration for the region filter
	 * @return
	 */
	protected String getAdminUnitCondition() {
		IndicatorFilters filters = getIndicatorFilters();
		AdministrativeUnit adminUnit = filters.getTbunitselection().getAuselection().getSelectedUnit();
		if (adminUnit != null) {
			if (filters.getIndicatorSite() == IndicatorSite.PATIENTADDRESS)
				 return "c.notifAddress.adminUnit.code like '" + adminUnit.getCode() + "%'";
			else return "c.notificationUnit.adminUnit.code like '" + adminUnit.getCode() + "%'";
		}
		else return null;
	}

	/**
	 * Return the user's workspace
	 * @return instance of the {@link Workspace Workspace} class
	 */
	protected Workspace getWorkspace() {
		return (Workspace)getComponentInstance("defaultWorkspace");
	}

	/**
	 * @return the consolidated
	 */
	public boolean isConsolidated() {
		return consolidated;
	}

	/**
	 * Specify if during the period specified in the filters, the indicator will contain just new cases 
	 * or also include on-going cases valid after the initial date of the filter
	 * @return
	 */
	public boolean isNewCasesOnly() {
		return newCasesOnly;
	}
	
	
	public void setNewCasesOnly(boolean value) {
		this.newCasesOnly = value;
	}
	

	/**
	 * @param caseState the caseState to set
	 */
	public void setCaseState(CaseState caseState) {
		this.caseState = caseState;
	}


	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}


	/**
	 * Set if query will generate consolidated values
	 * @param value
	 */
	public void setConsolidated(boolean value) {
		consolidated = value;
	}


	/**
	 * @param groupFields the groupFields to set
	 */
	public void setGroupFields(String groupFields) {
		this.groupFields = groupFields;
	}
	

	/**
	 * @param orderByFields the orderByFields to set
	 */
	public void setOrderByFields(String orderByFields) {
		this.orderByFields = orderByFields;
	}

	
	/**
	 * Set the query parameters
	 * @param query
	 */
	protected void setQueryParameters(Query query) {
		if (getClassification() != null)
			query.setParameter("classification", getClassification());

		if (caseState != null)
			query.setParameter("state", caseState);
	}

	/**
	 * @param outputSelected the outputSelected to set
	 */
	public void setOutputSelected(boolean outputSelected) {
		this.outputSelected = outputSelected;
	}


	/**
	 * @return the outputSelected
	 */
	public boolean isOutputSelected() {
		return outputSelected;
	}
	
	/**
	 * Indicate if the 'Diagnosis Type' filter will be available for the indicator
	 * @return
	 */
	public boolean isUseDiagnosisTypeFilter() {
		return true;
	}

	
	public CaseClassification getClassification() {
		return getIndicatorFilters().getClassification();
	}
}
