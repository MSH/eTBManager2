package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.bd.entities.enums.Quarter;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.medicines.MedicineManStartHome;
import org.msh.tb.tbunits.TbUnitHome;
import org.msh.utils.date.DateUtils;

 
/**
 * Handle actions to include and remove a TB unit from the medicine module control in Bangladesh
 * @author Mauricio Santos
 *
 */
@Name("medicineManStartBDHome")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class MedicineManStartBDHome{

	@In(required=true) MedicineManStartHome medicineManStartHome;
	@In(create=true) TbUnitHome tbunitHome;
	
	private Quarter selectedQuarter;
	private List<Integer> years;
	
	/**
	 * Start the medicine management control, specifying the starting date of the control
	 * and initializing the stock in unit based on the medicines and batches selected by the user in the UI
	 * using as medManStartDate the first date of the selected quarter 
	 * @return
	 */
	public String startMedicineManagement() {
		if(selectedQuarter == null || selectedQuarter.getYear() == 0)
			return "error";
		
		medicineManStartHome.setStartDate(selectedQuarter.getIniDate());
		
		String s = medicineManStartHome.startMedicineManagement();
		
		Tbunit unit = medicineManStartHome.getUnit();
		tbunitHome.setInstance(unit);
		tbunitHome.getInstance().setLimitDateMedicineMovement(selectedQuarter.getIniDate());
		tbunitHome.persist();
		tbunitHome.clearInstance();
		
		return s;
	}
	
	/**
	 * Load the list of years that is shown in the selection list
	 */
	public void loadYears(){
		if(years == null || years.size() == 0){
			Integer currYear = DateUtils.yearOf(DateUtils.getDate());
			years = new ArrayList<Integer>();
			for(int i = currYear ; i >= 2010 ; i--){
				years.add(i);
			}
		}
	}

	/**
	 * @return the selectedQuarter
	 */
	public Quarter getSelectedQuarter() {
		//Solve JSF problem when trying to set year before setting the quarter, the year depends on the quarter.
		if(this.selectedQuarter == null)
			return Quarter.FIRST;
		return selectedQuarter;
	}

	/**
	 * @param selectedQuarter the selectedQuarter to set
	 */
	public void setSelectedQuarter(Quarter selectedQuarter) {
		this.selectedQuarter = selectedQuarter;
	}

	/**
	 * @return the years
	 */
	public List<Integer> getYears() {
		if(years == null || years.size() < 1)
			loadYears();
		return years;
	}

	/**
	 * @param years the years to set
	 */
	public void setYears(List<Integer> years) {
		this.years = years;
	}	
}
