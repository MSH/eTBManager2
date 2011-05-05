package org.msh.tb.test;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.MedicineReceiving;
import org.msh.tb.entities.MedicineReceivingItem;
import org.msh.tb.entities.enums.Container;
import org.msh.tb.medicines.MedicineReceivingHome;
import org.msh.tb.medicines.MedicineSelection;
import org.msh.utils.ItemSelect;
import org.msh.utils.date.DateUtils;


@Name("medicineReceivingTest")
public class MedicineReceivingTest {

	@In(create=true) MedicineReceivingHome medicineReceivingHome;
	@In(create=true) MedicineSelection medicineSelection;
	
	private String[] manufactures = {"Lab Test", "WWW Laboratories", "MSH Labs", "Copacabana Lab" };
	private Integer[] qtds = {100, 200, 50, 500};
	
	/**
	 * Generate a medicine receiving package
	 */
	public void createReceiving() {
		
		// create a new receiving
		MedicineReceiving mr = medicineReceivingHome.getInstance();
		
		addMedicines();
		
		for (MedicineReceivingItem it: mr.getMedicines()) {
			addBatches(it);			
		}
	}
	
	public void addMedicines() {
		// add medicines
		for (ItemSelect it: medicineSelection.getItems()) {
			it.setSelected(true);
		}
		medicineReceivingHome.addMedicines();
	}
	
	public void addBatches(MedicineReceivingItem it) {
		while (it.getBatches().size() < 3) {
			medicineReceivingHome.newBatch(it);
			Batch b = medicineReceivingHome.getBatch();
			
			Random r = new Random();
			
			// generate expiration date
			int year = r.nextInt(3) + DateUtils.yearOf(new Date());
			int month = r.nextInt(12);
			System.out.println("mes=" + month + " ano=" + year);
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, 1);
			b.setExpiryDate(c.getTime());
			
			String bn = "";
			for (int i = 0; i < 5; i++)
				bn = bn + r.nextInt(10);
			b.setBatchNumber(bn);
			
			b.setBrandName(it.getMovement().getMedicine().getGenericName().getName1());
			b.setContainer(Container.BOX);
			b.setManufacturer(manufactures[r.nextInt(4)]);
			b.setQuantityContainer(qtds[r.nextInt(4)]);
			b.setQuantityReceived((r.nextInt(5) + 2) * 10000);
			b.setUnitPrice(((float)(r.nextInt(15000) + 3000)) / 10000F);
			medicineReceivingHome.addBatch();
		}
	}
	
}
