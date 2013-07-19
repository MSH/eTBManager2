package org.msh.tb.az.indicators;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.az.entities.enums.OutputSelectionAZ;

@Name("indicatorFiltersAZ")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class IndicatorFiltersAZ{
	
	private OutputSelectionAZ outputSelection = OutputSelectionAZ.ADMINUNIT;
	private Boolean referToThisUnit;
	
	@Observer("change-workspace")
	public void initializeFilters() {
		outputSelection = OutputSelectionAZ.ADMINUNIT;
		setReferToThisUnit(null);
	}

	/**
	 * @param outputSelection the outputSelection to set
	 */
	public void setOutputSelection(OutputSelectionAZ outputSelection) {
		this.outputSelection = outputSelection;
	}

	/**
	 * @return the outputSelection
	 */
	public OutputSelectionAZ getOutputSelection() {
		return outputSelection;
	}

	public void setOutputSelectionInt(Integer value) {
		if (value == null) {
			outputSelection = null;
			return;
		}
		
		for (OutputSelectionAZ sel: OutputSelectionAZ.values()) {
			if (sel.ordinal() == value) {
				outputSelection = sel;
				break;
			}
		}
	}
	
	public Integer getOutputSelectionInt() {
		if (outputSelection != null)
			 return outputSelection.ordinal();
		else return null;
	}

	public void setReferToThisUnit(Boolean referToThisUnit) {
		this.referToThisUnit = referToThisUnit;
	}

	public Boolean getReferToThisUnit() {
		return referToThisUnit;
	}
}
