package org.msh.tb.bd;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.medicines.MedicineManStartHome;
import org.msh.tb.tbunits.TbUnitHome;

 
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
	 * @return the selectedQuarter
	 */
	public Quarter getSelectedQuarter() {
		if(selectedQuarter == null)
			this.selectedQuarter = new Quarter();
		return selectedQuarter;
	}

	/**
	 * @param selectedQuarter the selectedQuarter to set
	 */
	public void setSelectedQuarter(Quarter selectedQuarter) {
		this.selectedQuarter = selectedQuarter;
	}

}
