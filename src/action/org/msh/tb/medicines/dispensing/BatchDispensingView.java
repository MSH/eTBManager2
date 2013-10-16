package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.BatchMovement;

@Name("batchDispensingView")
public class BatchDispensingView {

	@In EntityManager entityManager;
	@In DispensingSelection dispensingSelection;

	private BatchDispensingTable table;
	private List<DispensingInfo> dispensingDays;
	
	/**
	 * Return instance of {@link BatchDispensingTable} containing quantity dispensed 
	 * of the unit for the month and year selected
	 * @return
	 */
	public BatchDispensingTable getTable() {
		if (table == null)
			createTable();
		return table;
	}
	
	/**
	 * Create instance of {@link BatchDispensingTable} and fills it with dispensed quantity
	 * of the unit for the month and year selected
	 */
	private void createTable() {
		if (!dispensingSelection.isMonthYearSelected())
			return;
		
		String hql = "select c from MedicineDispensing a join a.movements b " +
				"join b.batches c where a.tbunit.id = #{userSession.tbunit.id} " +
				"and a.dispensingDate between :dt1 and :dt2 " +
				"and a.id not in (select mdc.dispensing.id from MedicineDispensingCase mdc where mdc.dispensing.id = a.id)";

		List<BatchMovement> lst = entityManager.createQuery(hql)
			.setParameter("dt1", dispensingSelection.getIniDate())
			.setParameter("dt2", dispensingSelection.getEndDate())
			.getResultList();
		
		table = new BatchDispensingTable();
		for (BatchMovement bm: lst) {
			DispensingRow row = table.addRow(bm.getMovement().getSource(), bm.getBatch());
			row.setQuantity( row.getQuantity() + bm.getQuantity() );
		}
		
		table.updateLayout();
	}


	/**
	 * Create list of dispensing by date done by the unit during the month
	 */
	protected void createDispensingDays() {
		if (!dispensingSelection.isMonthYearSelected())
			return;
		
		String hql = "select c, a.id from MedicineDispensing a join a.movements b " +
				"join b.batches c " +
				"join fetch c.batch join fetch c.movement " +
				"where a.tbunit.id = #{userSession.tbunit.id} " +
				"and a.dispensingDate between :dt1 and :dt2 " +
				"and a.id not in (select mdc.dispensing.id from MedicineDispensingCase mdc where mdc.dispensing.id = a.id) " + 
				"order by c.movement.date";

		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("dt1", dispensingSelection.getIniDate())
			.setParameter("dt2", dispensingSelection.getEndDate())
			.getResultList();
		
		dispensingDays = new ArrayList<DispensingInfo>();
		// create tables with dispensing information
		for (Object[] vals: lst) {
			BatchMovement bm = (BatchMovement)vals[0];
			Integer medDispId = (Integer)vals[1];
			DispensingInfo info = dispensingInfoByDate(bm.getMovement().getDate());
			info.setMedicineDispensingId(medDispId);
			DispensingRow row = info.getTable().addRow(bm.getMovement().getSource(), bm.getBatch());
			row.addQuantity( bm.getQuantity() );
		}
		
		// update layout of tables
		for (DispensingInfo info: dispensingDays)
			info.getTable().updateLayout();
	}


	/**
	 * Return information about dispensing by its date of dispensing
	 * @param dt
	 * @return
	 */
	private DispensingInfo dispensingInfoByDate(Date dt) {
		for (DispensingInfo info: dispensingDays) {
			if (info.getDate().equals(dt)) 
				return info;
		}
		
		DispensingInfo info = new DispensingInfo();
		info.setDate(dt);
		dispensingDays.add(info);
		return info;
	}
	
	/**
	 * @return the dispensingDays
	 */
	public List<DispensingInfo> getDispensingDays() {
		if (dispensingDays == null)
			createDispensingDays();
		return dispensingDays;
	}


	/**
	 * Store temporary information about a dispensing of a specific date 
	 * @author Ricardo Memoria
	 *
	 */
	public class DispensingInfo {
		private Date date;
		private int medicineDispensingId;
		private BatchDispensingTable table = new BatchDispensingTable();
		/**
		 * @return the date
		 */
		public Date getDate() {
			return date;
		}
		/**
		 * @param date the date to set
		 */
		public void setDate(Date date) {
			this.date = date;
		}
		/**
		 * @return the table
		 */
		public BatchDispensingTable getTable() {
			return table;
		}
		/**
		 * @return the medicineDispensingId
		 */
		public int getMedicineDispensingId() {
			return medicineDispensingId;
		}
		/**
		 * @param medicineDispensingId the medicineDispensingId to set
		 */
		public void setMedicineDispensingId(int medicineDispensingId) {
			this.medicineDispensingId = medicineDispensingId;
		}
	}
}
