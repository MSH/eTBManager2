package org.msh.tb.test;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.enums.Container;
import org.msh.tb.medicines.MedicineReceivingHome;
import org.msh.utils.date.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;


@Name("medicineReceivingTest")
public class MedicineReceivingTest {

	@In(create=true) MedicineReceivingHome medicineReceivingHome;
	@In(create=true) MedicinesQuery medicines;
	
	private String[] manufactures = {"Lab Test", "WWW Laboratories", "MSH Labs", "Copacabana Lab" };
	private Integer[] qtds = {100, 200, 50, 500};
	
	/**
	 * Generate a medicine receiving package
	 */
	public void createReceiving() {
		for (Medicine med: medicines.getResultList()) {
			addBatches(med);
		}
	}

	
	public void addBatches(Medicine med) {
		for (int loop = 0; loop < 3; loop++) {
			medicineReceivingHome.startNewBatch(med);

			Batch b = medicineReceivingHome.getBatch();
			
			Random r = new Random();
			
			// generate expiration date
			int year = r.nextInt(3) + DateUtils.yearOf(new Date());
			int month = r.nextInt(12);
//			System.out.println("mes=" + month + " ano=" + year);
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, 1);
			b.setExpiryDate(c.getTime());
			
			String bn = "";
			for (int i = 0; i < 5; i++)
				bn = bn + r.nextInt(10);
			b.setBatchNumber(bn);
			
			b.setMedicine(med);
			b.setContainer(Container.BOX);
			b.setManufacturer(manufactures[r.nextInt(4)]);
			b.setQuantityContainer(qtds[r.nextInt(4)]);
			b.setQuantityReceived((r.nextInt(5) + 2) * 10000);
			b.setUnitPrice(((float)(r.nextInt(15000) + 3000)) / 10000F);
			medicineReceivingHome.finishBatchEditing();
		}
	}
	
}
