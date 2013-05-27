package org.msh.tb.az;

import java.text.DateFormatSymbols;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.enums.OutputSelectionAZ;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.indicators.core.IndicatorFilters;

@Name("customIndicatorAZ")
public class CustomIndicatorAZ extends OutputSelectionIndicator2D {
	private static final long serialVersionUID = 5127772756149803047L;
	
	private OutputSelectionAZ colSelection;
	private boolean source = false;
	private boolean showPerc;

	@Override
	protected void createIndicators() {
		if (!validate())
			return;
		
		IndicatorFiltersAZ filters = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		String rowField = getOutputSelectionField(filters.getOutputSelection());
		String colField = getOutputSelectionField(colSelection);
		List<Object[]> lst;

		if (getColExams()==0 && !source){
			lst = generateValuesByField(colField + "," + rowField,condition());
		}
		else{
			String hql = "";
			if (getColExams()<2){
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
	
	private String condition() {
		return "c.notificationUnit!=null and c.state >= " + CaseState.WAITING_TREATMENT.ordinal();
		/*IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if ((!OutputSelectionAZ.TBUNIT_OWN.equals(filters_az.getOutputSelection()) && !OutputSelectionAZ.TBUNIT_OWN.equals(colSelection)))
			return "c.state >= " + CaseState.WAITING_TREATMENT.ordinal();
		IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
		Integer id = filters.getTbunitselection().getTbunit().getId();
		return 	" c.notificationUnit!=null"+
		 		" or c.ownerUnit.id="+id +
				" or (c.ownerUnit=null and c.notificationUnit.id="+id +
				" and c.state="+CaseState.WAITING_TREATMENT.ordinal()+")";*/
	}

	/**
	 * Return false if no selected tb-unit in filters, but it is need
	 */
	@Override
	protected boolean validateTBUnit() {
		if (!someSelectionEqualsTo(OutputSelectionAZ.TBUNIT_OWN))
			return true;
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (OutputSelectionAZ.TBUNIT_OWN.equals(filters_az.getOutputSelection()) && OutputSelectionAZ.TBUNIT_OWN.equals(colSelection))
			return false;
		
		//if no selected tb-unit
		IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
		if (filters.getTbunitselection()!=null)
			if (filters.getTbunitselection().getTbunit()!=null)
				if (filters.getTbunitselection().getTbunit().getId()!=null)
					return true;
					
		return false;
	}

	/**
	 * Return false if one of the outputSelection is PERIOD, but not enough necessary parameters
	 * */
	@Override
	protected boolean validatePeriod() {
		if (!someSelectionEqualsTo(OutputSelectionAZ.YEAR_MONTH) &&
			!someSelectionEqualsTo(OutputSelectionAZ.YEAR) &&
			!someSelectionEqualsTo(OutputSelectionAZ.YEAR_WEEK))
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



	@Override
	protected String getHQLFrom() {
		String s = "from TbCaseAZ c ";
		if (!someSelectionEqualsTo(OutputSelectionAZ.TBUNIT_OWN))
			return s;
		if (someSelectionEqualsTo(OutputSelectionAZ.GENDER))
			s+=	"join c.patient p ";
		if (someSelectionEqualsTo(OutputSelectionAZ.TBUNIT) || someSelectionEqualsTo(OutputSelectionAZ.PULMONARY) || someSelectionEqualsTo(OutputSelectionAZ.EXTRAPULMONARY))
			s+= "join c.notificationUnit nu ";
		if (someSelectionEqualsTo(OutputSelectionAZ.PULMONARY) || someSelectionEqualsTo(OutputSelectionAZ.EXTRAPULMONARY) || someSelectionEqualsTo(OutputSelectionAZ.HEALTH_SYSTEM))
			s+= "join c.ownerUnit ou";
		return s;
	}
	
	private boolean someSelectionEqualsTo(OutputSelectionAZ sel){
		IndicatorFiltersAZ filters_az = (IndicatorFiltersAZ)App.getComponent("indicatorFiltersAZ");
		if (sel.equals(filters_az.getOutputSelection()) || sel.equals(colSelection))
			return true;
		return false;
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

		if (output == OutputSelectionAZ.YEAR_MONTH){
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
		
		if (output == OutputSelectionAZ.YEAR_WEEK){
			DateFormatSymbols symbols = new DateFormatSymbols(locale);
			String[] values = symbols.getShortMonths();
			for (Object[] vals: lst) {
				//concatenate month and year and shift other fields
				String m = values[(Integer)vals[index+1]-1];
				vals[index] = vals[index+2]+"("+m+")/"+vals[index];
				if (index==0)
					vals[1] = vals[3];
				vals[2] = vals[4];
			}
		}
		
		if (output == OutputSelectionAZ.TBUNIT_OWN){
			return groupValuesByTbUnit(lst, index, 2);
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

	
	public boolean isShowPerc() {
		return showPerc;
	}
	public void setShowPerc(boolean showPerc) {
		this.showPerc = showPerc;
	}

	
	@Override
	protected String getHQLWhere() {
		String hql = super.getHQLWhere();
		return hql+getHQLWhereCond();
	}
}
