package org.msh.tb.medicines.dispensing;

import org.msh.tb.medicines.MedicineGroup;
import org.msh.tb.medicines.dispensing.BatchDispensingUIHome.BatchItem;

/**
 * @author Ricardo Memoria
 *
 */
public class MedicineDispensingItem extends MedicineGroup<BatchItem>{
	public int getQuantityAvailable() {
		int tot = 0;
		for (BatchItem b: getItems()) {
			tot += b.getBatchQuantity().getQuantity();
		}
		return tot;
	}

}
