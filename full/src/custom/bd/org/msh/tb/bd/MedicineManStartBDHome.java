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

	/**
	 * Start the medicine management control, specifying the starting date of the control
	 * and initializing the stock in unit based on the medicines and batches selected by the user in the UI
	 * using as medManStartDate the first date of the selected quarter 
	 * @return
	 */
	public String startMedicineManagement() {
		String s = medicineManStartHome.startMedicineManagement();
		
		Tbunit unit = medicineManStartHome.getUnit();
		tbunitHome.setId(unit.getId());
		tbunitHome.getInstance().setLimitDateMedicineMovement(medicineManStartHome.getStartDate());
		tbunitHome.persist();
		tbunitHome.clearInstance();
		
		return s;
	}
}
