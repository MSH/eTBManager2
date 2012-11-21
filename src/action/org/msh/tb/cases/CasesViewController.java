package org.msh.tb.cases;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.cases.treatment.TreatmentsInfoHome;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;

/**
 * Base controller to store temporary selection of the user view in the case management module.
 * This component will coordinate the feeding of values to display the content of the index pages
 * of the case management module
 * 
 * @author Ricardo Memoria
 *
 */
@Name("casesViewController")
@BypassInterceptors
public class CasesViewController {

	/**
	 * selected unit from the user
	 */
	private Tbunit selectedUnit;

	
	/**
	 * Check if the view of the main case home is of an specific unit
	 * @return
	 */
	public boolean isUnitView() {
		return (selectedUnit != null) || (UserSession.getUserWorkspace().getView() == UserView.TBUNIT);
	}
	
	/**
	 * @return the selectedUnit
	 */
	public Tbunit getSelectedUnit() {
		return selectedUnit;
	}

	/**
	 * @param selectedUnit the selectedUnit to set
	 */
	public void setSelectedUnit(Tbunit selectedUnit) {
		this.selectedUnit = selectedUnit;
		
		// sync selected unit with other components
		TreatmentsInfoHome comp1 = (TreatmentsInfoHome)App.getComponent("treatmentsInfoHome");
		if (comp1 != null)
			comp1.setTbunit(selectedUnit);

		CaseStateReport comp2 = (CaseStateReport)App.getComponent("caseStateReport");
		comp2.setTbunit(selectedUnit);
	}
	
	/**
	 * Set the selected unit id
	 * @param id
	 */
	public void setSelectedUnitId(Integer id) {
		if (id == null)
			 setSelectedUnit(null);
		else setSelectedUnit( App.getEntityManager().find(Tbunit.class, id) );
	}
	
	/**
	 * Return the selected unit identification
	 * @return
	 */
	public Integer getSelectedUnitId() {
		return selectedUnit != null? selectedUnit.getId() : null;
	}
	
	public Integer getSelectedUnitIdParam() {
		return selectedUnit == null? -1: selectedUnit.getId();
	}
	
	public Integer getFiltersUnitId() {
		CaseFilters filters = (CaseFilters)App.getComponent("caseFilters", true);
		return filters == null? null: filters.getUnitId();
	}
	
	public void setFiltersUnitId(Integer id) {
		CaseFilters filters = (CaseFilters)App.getComponent("caseFilters", true);
		if (filters != null) {
			if (id == -1)
				 filters.setUnitId(null);
			else filters.setUnitId(id);
		}
	}
}
