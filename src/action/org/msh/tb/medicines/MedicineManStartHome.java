package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.Medicine;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.SourceGroup;
import org.msh.tb.SourcesQuery;

/**
 * Home class to handle a TB Unit that start medicine management in e-TB Manager
 * @author Ricardo Memoria
 *
 */
@Name("medicineManStartHome")
public class MedicineManStartHome {

	@In(create=true) SourcesQuery sources;
	@In(create=true) MedicinesQuery medicines;
	
	private Date startDate;
	
	
	/**
	 * Information about the medicines by source
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineInfo {
		private Medicine medicine;
		private List<Batch> batches = new ArrayList<Batch>();

		public Medicine getMedicine() {
			return medicine;
		}
		public void setMedicine(Medicine medicine) {
			this.medicine = medicine;
		}
		public List<Batch> getBatches() {
			return batches;
		}
		public void setBatches(List<Batch> batches) {
			this.batches = batches;
		}
	}
	
	public class MedicinesSource extends SourceGroup<MedicineInfo> {}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	};
}
