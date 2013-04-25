package org.msh.tb.az;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.enums.OutputSelectionAZ;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorSite;
import org.msh.tb.indicators.core.OutputSelection;

@Name("customIndicatorAZ")
public class CustomIndicatorAZ extends Indicator2D {
	private static final long serialVersionUID = 5127772756149803047L;
	@In(create=true) Locale locale;

	private OutputSelectionAZ colSelection;
	private int colExams = 0;
	private boolean source = false;
	private String date = null;
	private boolean showPerc;

	@Override
	protected void createIndicators() {
		IndicatorFiltersAZ filters = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (filters == null) 
			return;
		if ((filters.getOutputSelection() == null) || (colSelection == null))
			return;

		if (!validatePeriod())
			return;

		String rowField = getOutputSelectionField( filters.getOutputSelection() );
		String colField = getOutputSelectionField( colSelection );
		List<Object[]> lst;

		if (colExams==0 && !source){
			lst = generateValuesByField(colField + "," + rowField, "c.state >= " + CaseState.WAITING_TREATMENT.ordinal());
		}
		else{
			String hql = "";
			if (colExams<2){
				String rc = getColRow();
				hql = "select "+rc+", count(*) ";
				hql += "from "+getExam()+" ex join ex.tbcase c ";
				//hql += source ? "join ex.source sc " : "";
				hql += getHQLWhere()+" and c.state >= " + CaseState.WAITING_TREATMENT.ordinal();//+(!source ? " and ex1.dateCollected = (select min(sp.dateCollected) from "+getExam()+" sp where sp.tbcase.id = c.id))":"");
				hql += " group by "+rc;
				hql += " order by "+rc;
			}
			else{
				hql = "select ex1.result, ex2.result, count(*) ";
				hql += "from "+colField+" ex1,"+rowField+" ex2 join ex1.tbcase c join ex2.tbcase c ";
				hql += getHQLWhere()+" and c.state >= " + CaseState.WAITING_TREATMENT.ordinal()+" and ex1.tbcase.id = c.id and ex2.tbcase.id = c.id";//" and ex1.dateCollected = (select min(sp.dateCollected) from "+colField+" sp where sp.tbcase.id = c.id)) and ex1.dateCollected = (select min(sp.dateCollected) from "+rowField+" sp where sp.tbcase.id = c.id))";
				hql += " group by ex1.result, ex2.result";
				hql += " order by ex1.result, ex2.result";
			}
			lst = App.getEntityManager().createQuery(hql).getResultList();
		}

		lst = handleItems(colSelection, 0, lst);
		lst = handleItems(filters.getOutputSelection(), 1, lst);

		createItems(lst);
	}

	/**
	 * Return false if one of the outputSelection is PERIOD, but not enough necessary parameters
	 * */
	private boolean validatePeriod() {
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (!OutputSelectionAZ.PERIOD.equals(filters_az.getOutputSelection()) && !OutputSelectionAZ.PERIOD.equals(colSelection))
			return true;

		//if selected date fields more than 1 or no
		IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
		int colSelD = 0;
		if (filters.isUseDiagnosisDate())
			colSelD++;
		if (filters.isUseRegistrationDate()) 
			colSelD++;
		if (filters.isUseIniTreatmentDate()) 
			colSelD++;
		if (colSelD!=1)
			return false;

		//if period not full
		if ((filters.getIniDate() == null) && (filters.getEndDate() == null))
			return false;

		return true;
	}

	private String getColRow(){
		IndicatorFiltersAZ filters = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		OutputSelectionAZ c = colSelection;
		OutputSelectionAZ r = filters.getOutputSelection();
		if (OutputSelectionAZ.MICROSCOPY_RESULT.equals(c)||OutputSelectionAZ.CULTURE_RESULT.equals(c)||OutputSelectionAZ.HIV_RESULT.equals(c))//||OutputSelectionAZ.SOURCE_MED.equals(c))
			return (source ? "ex.source":"ex.result")+","+getOutputSelectionField(r);
		if (OutputSelectionAZ.MICROSCOPY_RESULT.equals(r)||OutputSelectionAZ.CULTURE_RESULT.equals(r)||OutputSelectionAZ.HIV_RESULT.equals(r))//||OutputSelectionAZ.SOURCE_MED.equals(r))
			return getOutputSelectionField(c)+","+(source ? "ex.source":"ex.result");
		return null;
	}

	private String getExam(){
		IndicatorFiltersAZ filters = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		OutputSelectionAZ c = colSelection;
		OutputSelectionAZ r = filters.getOutputSelection();
		if (source) 
			return "prescribedmedicine";
		if (OutputSelectionAZ.MICROSCOPY_RESULT.equals(c)||OutputSelectionAZ.CULTURE_RESULT.equals(c)||OutputSelectionAZ.HIV_RESULT.equals(c))
			return getOutputSelectionField(c);
		if (OutputSelectionAZ.MICROSCOPY_RESULT.equals(r)||OutputSelectionAZ.CULTURE_RESULT.equals(r)||OutputSelectionAZ.HIV_RESULT.equals(r))
			return getOutputSelectionField(r);
		return null;
	}

	/**
	 * Return the field of the HQL query associated to the output selected
	 * @param output
	 * @return
	 */
	protected String getOutputSelectionField(OutputSelectionAZ output) {
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
		case REGIMENS: return "c.regimen.name";
		case VALIDATION_STATE: return "c.validationState";
		case MICROSCOPY_RESULT: colExams++; return "ExamMicroscopy";
		case CULTURE_RESULT: colExams++; return "ExamCulture";
		case DIAGNOSIS_TYPE: return "c.diagnosisType";
		case HIV_RESULT: colExams++; return "ExamHIV";
		//case SOURCE_MED: source = true; return "";//
		case DRUG_RESIST_TYPE: return "c.drugResistanceType";
		case CLASSIFICATION: return "c.classification";
		case PERIOD: return "year("+getDate()+"),month("+getDate()+")";
		}
		return null;
	}

	/**
	 * Update the values of the result list to be displayed
	 * @param output - output to be displayed
	 * @param index - position in the array that shall be updated
	 * @param lst - list of items to be updated
	 * @return updated list
	 */
	protected List<Object[]> handleItems(OutputSelectionAZ output, int index, List<Object[]> lst) {
		if (output == OutputSelectionAZ.REGIMENS) {
			for (Object[] vals: lst) {
				Object val = vals[index];
				if (val == null)
					vals[index] = getMessage("regimens.individualized");
			}
		}

		if (output == OutputSelectionAZ.AGERANGE)
			return groupValuesByAreRange(lst, index, 2);

		if (output == OutputSelectionAZ.ADMINUNIT) {
			for (Object[] vals: lst) {
				String s = getAdminUnitDisplayText((String)vals[index]);
				vals[index] = s;
			}
		}

		if (output == OutputSelectionAZ.PERIOD){
			DateFormatSymbols symbols = new DateFormatSymbols(locale);
			String[] values = symbols.getShortMonths();
			for (Object[] vals: lst) {
				//concatenate month and year and shift other fields
				String m = values[(Integer)vals[index+1]-1];
				vals[index] = m+"/"+vals[index];
				if (index==0)
					vals[1] = vals[2];
				vals[2] = vals[3];
			}
		}

		return lst;
	}


	/**
	 * @return the colSelection
	 */
	public OutputSelectionAZ getColSelection() {
		return colSelection;
	}


	/**
	 * @param colSelection the colSelection to set
	 */
	public void setColSelection(OutputSelectionAZ colSelection) {
		this.colSelection = colSelection;
	}

	public void setColSelectionIndex(Integer index) {
		if (index == null) {
			colSelection = null;
			return;
		}

		for (OutputSelectionAZ sel: OutputSelectionAZ.values()) {
			if (sel.ordinal() == index) {
				colSelection = sel;
				break;
			}
		}
	}

	public Integer getColSelectionIndex() {
		if (colSelection == null)
			return null;
		else return colSelection.ordinal();
	}

	public String getDisplayColumnSelection() {
		return getDisplayOutputSelection(colSelection);
	}

	/**
	 * Return the display value of the {@link OutputSelection} parameter
	 * @param output
	 * @return string display representation of the parameter output
	 */
	public String getDisplayOutputSelection(OutputSelectionAZ output) {
		if (output == null)
			return null;

		for (SelectItem item: getOutputSelections()) {
			if (item.getValue().equals(output.ordinal())) {
				return item.getLabel();
			}
		}
		return null;		
	}

	public boolean isShowPerc() {
		return showPerc;
	}
	public void setShowPerc(boolean showPerc) {
		this.showPerc = showPerc;
	}

	/**
	 * Return the options of output to be selected by the user 
	 * @return Array of {@link OutputSelection} enumeration
	 */
	@Override
	public List<SelectItem> getOutputSelections() {
		IndicatorFilters filters = getIndicatorFilters();
		Map<String, String> messages = Messages.instance();

		List<SelectItem> outputSelections = new ArrayList<SelectItem>();

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
		for (OutputSelectionAZ sel: OutputSelectionAZ.values()) {
			if ((sel != OutputSelectionAZ.ADMINUNIT) || ((sel == OutputSelectionAZ.ADMINUNIT) && (hasAdminUnit))) {
				SelectItem item = new SelectItem();
				item.setValue(sel.ordinal());
				if (sel == OutputSelectionAZ.ADMINUNIT)
					item.setLabel(labelAdminUnit);
				else item.setLabel(messages.get(sel.getKey()));
				outputSelections.add(item);
			}
		}
		setOutputSelections(outputSelections);
		return outputSelections;
	}


	@Override
	public String getDisplayOutputSelection() {
		IndicatorFiltersAZ filters = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		OutputSelectionAZ output = filters.getOutputSelection();

		return getDisplayOutputSelection(output);
	}

	/**
	 * Return selected date from filters
	 * */
	public String getDate() {
		if (date==null){
			IndicatorFilters filters = getIndicatorFilters();
			if (filters.isUseDiagnosisDate())
				date = "c.diagnosisDate";

			if (filters.isUseRegistrationDate()) 
				date = "c.registrationDate";

			if (filters.isUseIniTreatmentDate()) 
				date = "c.treatmentPeriod.iniDate";

		}
		return date;
	}
}
