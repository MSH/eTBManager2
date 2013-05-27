package org.msh.tb.az;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.In;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.Messages;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.enums.OutputSelectionAZ;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.IndicatorSite;
import org.msh.tb.indicators.core.OutputSelection;


public abstract class OutputSelectionIndicator extends Indicator {
	private static final long serialVersionUID = 3273555218834941066L;
	
	@In(create=true) Locale locale;
	
	private int colExams = 0;
	private boolean source = false;
	private String date = null;
	
	@Override
	protected String createHQLByOutputSelection() {
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
		OutputSelectionAZ out = filters_az.getOutputSelection();
		
		if (out == null)
			return null;
		
		String fields = getOutputSelectionField(out);

		// check if the filter is compatible with the output
		if ((filters_az.getOutputSelection() == OutputSelectionAZ.PULMONARY) || (filters_az.getOutputSelection() == OutputSelectionAZ.EXTRAPULMONARY)) {
			if (filters_az.getOutputSelection() == OutputSelectionAZ.PULMONARY)
				 filters.setInfectionSite(InfectionSite.PULMONARY);
			else filters.setInfectionSite(InfectionSite.EXTRAPULMONARY);
		}

		setGroupFields(fields);
		setSortRows(false);
		return createHQL();
	}	
	
	protected boolean validate() {
		IndicatorFiltersAZ filters = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (filters == null) 
			return false;
		
		if (!validatePeriod())
			return false;

		if (!validateTBUnit())
			return false;
		
		return true;
	}
	
		
	@Override
	public String getDisplayOutputSelection() {
		IndicatorFiltersAZ filters = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		OutputSelectionAZ output = filters.getOutputSelection();

		return getDisplayOutputSelection(output);
	}
	
	
	/**
	 * Translate the key to a string 
	 * @param key Object to be translated
	 * @return String representation of the key
	 */
	@Override
	protected String translateKey(Object key) {
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (key == null) {
			if ((isOutputSelected()) && (filters_az.getOutputSelection() == OutputSelectionAZ.REGIMENS))
				 return getMessage("regimens.individualized");
			else return getMessage("global.notdef");
		}
		else {
			if (key instanceof Enum)
				 return getEnumDisplayText((Enum)key);
			else
			if ((isOutputSelected()) && (filters_az.getOutputSelection() == OutputSelectionAZ.ADMINUNIT))
				return getAdminUnitDisplayText((String)key);
			else return key.toString();
		}
	}
	
	/**
	 * Automatically generate the list of indicators based on a list of object values.
	 * The first item of the object values must be the label and the second item is the value
	 * @param lst list of indicator values
	 */
	@Override
	protected void createItems(List<Object[]> lst) {
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		OutputSelectionAZ output = filters_az.getOutputSelection();
		if ((isOutputSelected()) && (output == OutputSelectionAZ.AGERANGE)) {
			lst = groupValuesByAreRange(lst,0, 1);
		}

		if (output == OutputSelectionAZ.YEAR_MONTH){
			DateFormatSymbols symbols = new DateFormatSymbols(locale);
			String[] values = symbols.getShortMonths();
			for (Object[] vals: lst) {
				//concatenate month and year and shift other fields
				String m = values[(Integer)vals[1]-1];
				vals[0] = m+"/"+vals[0];
				vals[1] = vals[2];
			}
		}
		
		if (output == OutputSelectionAZ.YEAR_WEEK){
			DateFormatSymbols symbols = new DateFormatSymbols(locale);
			String[] values = symbols.getShortMonths();
			for (Object[] vals: lst) {
				//concatenate month and year and shift other fields
				String m = values[(Integer)vals[1]-1];
				vals[0] = vals[2]+"("+m+")/"+vals[0];
				vals[1] = vals[3];
			}
		}
		
		if (output == OutputSelectionAZ.TBUNIT_OWN){
			lst = groupValuesByTbUnit(lst,0, 2);
		}
		
		for (Object[] vals: lst) {
			int val = ((Long)vals[1]).intValue();
			if (val > 0)
				addValue(translateKey(vals[0]), val);
		}
	}
	
	@Override
	protected void generateIndicatorByOutputSelection(String condition) {
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		OutputSelectionAZ out = filters_az.getOutputSelection();
		String fields = getOutputSelectionField(out);
		if (colExams==0)
			super.generateIndicatorByOutputSelection(condition);
		else{
			String hql="";
			hql = "select ex.result, count(*) ";
			hql += "from "+fields+" ex join ex.tbcase c ";
			//hql += source ? "join ex.source sc " : "";
			hql += getHQLWhere()+" and c.state >= " + CaseState.WAITING_TREATMENT.ordinal();//+(!source ? " and ex1.dateCollected = (select min(sp.dateCollected) from "+getExam()+" sp where sp.tbcase.id = c.id))":"");
			hql += " group by ex.result";
			hql += " order by ex.result";
			createItems(App.getEntityManager().createQuery(hql).getResultList());
		}
	}
	
	protected boolean validatePeriod() {
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (!OutputSelectionAZ.YEAR_MONTH.equals(filters_az.getOutputSelection()) &&
			!OutputSelectionAZ.YEAR.equals(filters_az.getOutputSelection()) &&
			!OutputSelectionAZ.YEAR_WEEK.equals(filters_az.getOutputSelection()))
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
	
	/**
	 * Return false if no selected tb-unit in filters, but it is need
	 */
	protected boolean validateTBUnit() {
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (!OutputSelectionAZ.TBUNIT_OWN.equals(filters_az.getOutputSelection()))
			return true;
		
		//if no selected tb-unit
		IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
		if (filters.getTbunitselection()!=null)
			if (filters.getTbunitselection().getTbunit()!=null)
				if (filters.getTbunitselection().getTbunit().getId()!=null)
					return true;
					
		return false;
	}

	/**
	 * @param index
	 * @param keyCount
	 * @param new_lst
	 * @param vals
	 */
	protected void addValueToUnit(String tbu,int index, int keyCount,
			List<Object[]> new_lst, Object[] vals) {
		long value = (Long)vals[vals.length - 1];
		Object[] keys = new Object[2];
		if (index==0){
			keys[0]=tbu;
			keys[1]=vals[keyCount-index*2];
		}
		else{
			keys[0]=vals[keyCount-index*2];
			keys[1]=tbu;
		}
		
		Object[] newvalues = findRecord(new_lst, keys);

		// record was found ?
		if (newvalues == null) {
			// rebuild new values
			newvalues = new Object[3];
			int ind = 0;
			while (ind < 2) {
				newvalues[ind] = keys[ind];
				ind++;
			}
			newvalues[2] = 0L;
			new_lst.add(newvalues);
		}
		
		newvalues[newvalues.length - 1] = ((Long)newvalues[newvalues.length - 1]) + value;
	}
	
	
	protected List<Object[]> groupValuesByTbUnit(List<Object[]> lst, int index, int keyCount) {
		List<Object[]> new_lst = new ArrayList<Object[]>();
		IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
		Integer id = filters.getTbunitselection().getTbunit().getId();
		for(Object[] vals:lst){
			Tbunit nU = (Tbunit)vals[index];
			if (nU.getId()==id){
				addValueToUnit(App.getMessage("TbCase.notificationUnit"),index, keyCount, new_lst, vals);
				if (vals[index+1]==null)
					addValueToUnit(App.getMessage("TbCase.realUnit"),index, keyCount, new_lst, vals);
			}
			if (vals[index+1]!=null){
				Tbunit oU = (Tbunit)vals[index+1];
				if (oU.getId()==id){
					addValueToUnit(App.getMessage("TbCase.ownerUnit"),index, keyCount, new_lst, vals);
					addValueToUnit(App.getMessage("TbCase.realUnit"),index, keyCount, new_lst, vals);
				}
			}
		}
		return new_lst;
	}

	/**
	 * Return selected date from filters
	 * */
	public String getDate() {
		if (date==null){
			IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
			if (filters.isUseDiagnosisDate())
				date = "c.diagnosisDate";

			if (filters.isUseRegistrationDate()) 
				date = "c.registrationDate";

			if (filters.isUseIniTreatmentDate()) 
				date = "c.treatmentPeriod.iniDate";

		}
		return date;
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
	
	/**
	 * Return the filters selected by the user
	 * @return instance of the {@link IndicatorFilters IndicatorFilters} class
	 */
	protected IndicatorFilters getIndicatorFilters() {
		return (IndicatorFilters)Contexts.getSessionContext().get("indicatorFilters");
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
		case YEAR_MONTH: return "year("+getDate()+"),month("+getDate()+")";
		case YEAR: return "year("+getDate()+")";
		case YEAR_WEEK: return "year("+getDate()+"),month("+getDate()+"),week("+getDate()+")";
		case HEALTH_SYSTEM: return "c.notificationUnit.healthSystem";
		case TBUNIT_OWN: return "c.notificationUnit,c.ownerUnit"; 
		case CREATOR_USER: return "c.createUser";
		}
		return null;
	}

	public int getColExams() {
		return colExams;
	}
	
	protected String getHQLWhereCond() {
		String hql = "";
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (filters_az.getReferToThisUnit()!=null)
			hql += " and c.referToOtherTBUnit="+(filters_az.getReferToThisUnit()==true ? 0:1);
		return hql;
	}
}
