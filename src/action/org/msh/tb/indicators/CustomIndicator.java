package org.msh.tb.indicators;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.indicators.core.OutputSelection;

@Name("customIndicator")
public class CustomIndicator extends Indicator2D {
	private static final long serialVersionUID = 5127772756149803047L;

	private OutputSelection colSelection;
	
	@Override
	protected void createIndicators() {
		IndicatorFilters filters = getIndicatorFilters();
		if ((filters.getOutputSelection() == null) || (colSelection == null))
			return;
		
		String rowField = filters.getOutputSelection().getField();
		String colField = colSelection.getField();
		
		List<Object[]> lst = generateValuesByField(colField + "," + rowField, "c.state >= " + CaseState.WAITING_TREATMENT.ordinal());
		
		lst = handleItems(colSelection, 0, lst);
		lst = handleItems(filters.getOutputSelection(), 1, lst);
		
		createItems(lst);
	}

	
	/**
	 * Update the values of the result list to be displayed
	 * @param output - output to be displayed
	 * @param index - position in the array that shall be updated
	 * @param lst - list of items to be updated
	 * @return updated list
	 */
	protected List<Object[]> handleItems(OutputSelection output, int index, List<Object[]> lst) {
		if (output == OutputSelection.AGERANGE)
			return groupValuesByAreRange(lst, index, 2);

		if (output == OutputSelection.ADMINUNIT) {
			for (Object[] vals: lst) {
				String s = getAdminUnitDisplayText((String)vals[index]);
				vals[index] = s;
			}
		}
		
		return lst;
	}
	
	/**
	 * @return the colSelection
	 */
	public OutputSelection getColSelection() {
		return colSelection;
	}

	/**
	 * @param colSelection the colSelection to set
	 */
	public void setColSelection(OutputSelection colSelection) {
		this.colSelection = colSelection;
	}

	public void setColSelectionIndex(Integer index) {
		if (index == null) {
			colSelection = null;
			return;
		}
		
		for (OutputSelection sel: OutputSelection.values()) {
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
}
