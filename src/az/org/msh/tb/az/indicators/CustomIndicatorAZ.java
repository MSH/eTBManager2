package org.msh.tb.az.indicators;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.enums.OutputSelectionAZ;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.indicators.core.IndicatorSite;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;

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
		
		//sort rows and columns in all case except agerange
		if (!OutputSelectionAZ.AGERANGE.equals(filters.getOutputSelection()))
			Collections.sort(getTable().getRows(), new Comparator<TableRow>() {
				public int compare(TableRow row1, TableRow row2) {
					return row1.getTitle().compareTo(row2.getTitle());
				}
			});
		if (!OutputSelectionAZ.AGERANGE.equals(colSelection))
		Collections.sort(getTable().getColumns(), new Comparator<TableColumn>() {
			public int compare(TableColumn col1, TableColumn col2) {
				return col1.getTitle().compareTo(col2.getTitle());
			}
		});
			
	}
	
	private String condition() {
		return "c.notificationUnit!=null and c.state >= " + CaseState.WAITING_TREATMENT.ordinal();
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
		if (someSelectionEqualsTo(OutputSelectionAZ.GENDER))
			s+=	"join c.patient p ";
		if (someSelectionEqualsTo(OutputSelectionAZ.HEALTH_SYSTEM) || someSelectionEqualsTo(OutputSelectionAZ.TBUNIT))
			s+= "join c.notificationUnit nu ";
		if (someSelectionEqualsTo(OutputSelectionAZ.ADMINUNIT)){
			if (getIndicatorFilters().getIndicatorSite() == IndicatorSite.PATIENTADDRESS)
				s+= "join c.notifAddress.adminUnit naad ";
			else
				s+= "join c.notificationUnit nu join c.notificationUnit.adminUnit nuad ";
		}
		
		if (!someSelectionEqualsTo(OutputSelectionAZ.REAL_UNIT))
			return s;

		if (someSelectionEqualsTo(OutputSelectionAZ.HEALTH_SYSTEM) || 
				someSelectionEqualsTo(OutputSelectionAZ.OWNER_UNIT))
			s+= "join c.ownerUnit ou";
		return s;
	}
	
	@Override
	protected boolean someSelectionEqualsTo(OutputSelectionAZ sel){
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

		if (output == OutputSelectionAZ.AGERANGE){
			return groupValuesByAreRange(lst, index, lst.get(0).length-1);
		}

		if (output == OutputSelectionAZ.ADMINUNIT) {
			for (Object[] vals: lst) {
				String s = getAdminUnitDisplayText((String)vals[index]);
				vals[index] = s;
			}
		}

		if (output == OutputSelectionAZ.YEAR_MONTH){
			for (Object[] vals: lst) {
				//concatenate month and year and shift other fields
				String m = ((Integer)vals[index+1]<10?"0":"")+vals[index+1];
				Object[] n = new Object[vals.length-1];
				n[index] = vals[index]+"-"+m;
				
				cutLastElement(index, lst, vals, n);
				
			}
		}
		
		if (output == OutputSelectionAZ.YEAR_WEEK){
			for (Object[] vals: lst) {
				//split year and week to other fields
				String yw = Integer.toString(((Integer)vals[index]));
				String y = yw.substring(0, 4).toString();
				String w = yw.substring(4).toString();
				vals[index] = y+" \""+w+"\"";
			}
		}
		
		if (output == OutputSelectionAZ.REAL_UNIT){
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
}
