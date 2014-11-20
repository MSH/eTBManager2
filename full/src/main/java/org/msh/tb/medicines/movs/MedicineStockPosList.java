package org.msh.tb.medicines.movs;

import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.medicines.MedicineGroup;


/**
 * @author Ricardo Memoria
 *
 */
public class MedicineStockPosList extends MedicineGroup<BatchQuantity>{
	public int getQuantityAvailable() {
		int tot = 0;
		for (BatchQuantity b: getItems()) {
			tot += b.getQuantity();
		}
		return tot;
	}
}
