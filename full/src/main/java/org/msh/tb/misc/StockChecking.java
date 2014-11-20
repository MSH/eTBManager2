package org.msh.tb.misc;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.FinalExpiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.msh.tb.entities.MedicineUnit;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.medicines.movs.MinBufferStockList;
import org.msh.tb.medicines.movs.StockPositionList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
 
@Name("stockChecking")
public class StockChecking {

	@In(create=true) List<Tbunit> storageUnits;
	@In(create=true) StockPositionList stockPositionList;
	@In(create=true) MinBufferStockList minBufferStockList;
	
	/**
	 * Check if stock quantity is under minimum
	 */
	@Asynchronous
	public void asyncCheckUnits(@Expiration Date date, @IntervalDuration Long val, @FinalExpiration Date finalDate) {
		System.out.println("Checking units...");
		checkUnits();
	}

	/**
	 * Check the minimum buffer stock in every unit that store medicines
	 */
	public void checkUnits() {
		for (Tbunit unit: storageUnits) {
			checkUnit(unit);
		}
	}
	
	/**
	 * Check stock position in unit. If quantity is under the minimum, send e-mails to users responsibles
	 * @param unit
	 */
	public void checkUnit(Tbunit unit) {
		minBufferStockList.initialize(unit, null);
		
		if (minBufferStockList.getItems().size() == 0)
			return;

		List<StockPosition> stockpos = stockPositionList.generate(unit, null);

		List<StockPosition> meds = new ArrayList<StockPosition>();
		
		// check if medicines are under low quantity
		for (StockPosition sp: stockpos) {			
			MedicineUnit mu = minBufferStockList.findItem(sp.getMedicine(), sp.getSource());

			if ((mu != null) && (mu.getMinBufferStock() != null)) {
				if (sp.getQuantity() < mu.getMinBufferStock()) {
					meds.add(sp);
				}
			}
		}
		
		// if there are medicines with low quantity, notify users
		if (meds.size() < 0)
			notifyStockShortage(meds);
	}
	
	private void notifyStockShortage(List<StockPosition> meds) {
		// notifica 
	}
}
