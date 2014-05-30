package org.msh.tb.medicines.dispensing;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.MedicineDispensing;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.medicines.movs.StockPositionList;

@Name("batchDispensingUIHome")
public class BatchDispensingUIHome extends AbstractDispensigUIHome {

	@In EntityManager entityManager;
	@In(create=true) StockPositionList stockPositionList;



	/**
	 * Create list of sources and its medicines and batches, ready for editing
	 */
	@Override
	protected void loadSources(List<SourceItem> sources) {
		Tbunit unit = getUnit();
		List<BatchQuantity> lst = stockPositionList.getBatchAvailable(unit, null);

		for (BatchQuantity item: lst) {
			if (item.getQuantity() > 0)
				addSourceRow(item);
		}

		// check if it's a dispensing being edited, and include the quantity dispensed previously
		if (getDispensingHome().isManaged()) {
			MedicineDispensing medDisp = getDispensingHome().getMedicineDispensing();
			for (Movement mov: medDisp.getMovements()) {
				for (BatchMovement bm: mov.getBatches()) {
					DispensingRow row = addSourceRow(mov.getSource(), bm.getBatch());
					row.setDispensingQuantity(bm.getQuantity());
				}
			}
		}
	}

}
