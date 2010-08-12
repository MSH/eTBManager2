package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.tb.medicines.movs.MovementHome;

//@Name("dispensingMovements")
//@Scope(ScopeType.CONVERSATION)
public class DispensingMovements extends Controller {
	private static final long serialVersionUID = -176047726524743646L;

	@In EntityManager entityManager;
//	@In(required=true, value="#{medicineDispensingHome.unitDispensing}") UnitDispensing unitDispensing;
//	@In(required=true, value="medicineDispensingHome") MedicineDispensingHome medicineDispensingHome;
	@In(create=true) MovementHome movementHome;
	@In(create=true) Locale locale;
	@In(create=true) FacesMessages facesMessages;
	
	private List<MedicineDispensingInfo> infoItems;
//	private List<BatchQuantity> batches;
	
	public List<MedicineDispensingInfo> getInfoItems() {
		return infoItems;
	}
	
	/**
	 * Loads information about reserved quantity and unit price 
	 */
	public void loadReservedInfo() {
		String hql = "select b.batch.medicine.id, b.source.id, sum(b.quantityDispensing), avg(b.batch.unitPrice) " + 
			"from BatchQuantity b " +
			"where b.tbunit = #{unitDispensing.tbunit} " +
			"group by b.batch.medicine.id, b.source.id";
		
		List<Object[]> lst = entityManager.createQuery(hql).getResultList();
		
		for (Object[] vals: lst) {
			Integer medId = (Integer)vals[0];
			Integer sourceId = (Integer)vals[1];
			Long resQtd = (Long)vals[2];
			Double unitPrice = (Double)vals[3];
			
			MedicineDispensingInfo di = getMedicineDispensingInfo(sourceId, medId);
			if (resQtd != null)
				di.setQtdReserved(resQtd.intValue());
			if (unitPrice != null)
				di.setUnitPrice(unitPrice.floatValue());
		}
	}
	
	/**
	 * Loads information about movement quantity dispensed by source and medicine 
	 */
/*	public void loadMovementInfo(Date dtIni, Date dtEnd, UnitDispensing unitDispensing) {
		String hql = "select m.medicine.id, m.source.id, sum(m.quantity) " + 
			"from Movement m " +
			"where m.tbunit.id = :unit " +
			"and m.type = :type " +
			"and m.date between :dtini and :dtend " +
			"group by m.medicine.id, m.source.id";

		List<Object[]> lst = entityManager
		.createQuery(hql)
		.setParameter("unit", unitDispensing.getTbunit().getId())
		.setParameter("dtini", dtIni)
		.setParameter("dtend", dtEnd)
		.setParameter("type", MovementType.DISPENSING)
		.getResultList();
	
		for (Object[] vals: lst) {
			Integer medId = (Integer)vals[0];
			Integer sourceId = (Integer)vals[1];
			Long movQtd = (Long)vals[2];
			
			MedicineDispensingInfo di = getMedicineDispensingInfo(sourceId, medId);
			di.setQtdMovements(movQtd.intValue());
		}
	}
*/
	
	
	/**
	 * Search for the MedicineDispensingInfo object according to the source and medicine ids
	 * @param sourceId
	 * @param medId
	 * @return
	 */
	public MedicineDispensingInfo findMedicineDispensingInfo(Integer sourceId, Integer medId) {
		if (infoItems == null)
			return null;
		
		for (MedicineDispensingInfo di: infoItems) {
			if ((di.getMedicineId().equals(medId)) && (di.getSourceId().equals(sourceId)))
				return di;
		}
		return null;
	}
	
	public MedicineDispensingInfo getMedicineDispensingInfo(Integer sourceId, Integer medId) {
		if (infoItems == null)
			infoItems = new ArrayList<MedicineDispensingInfo>();
		
		for (MedicineDispensingInfo di: infoItems) {
			if ((di.getMedicineId().equals(medId)) && (di.getSourceId().equals(sourceId)))
				return di;
		}
		
		MedicineDispensingInfo di = new MedicineDispensingInfo();
		di.setMedicineId(medId);
		di.setSourceId(sourceId);
		infoItems.add(di);
		
		return di;
	}


	/**
	 * Create the dispensing movements based on the list of MedicineDispensingInfo objects
	 * @param week
	 * @param day
	 */
/*	public void createMovements(int week, int day, UnitDispensing unitDispensing) {
		if (infoItems == null)
			return;
		
		Date movDt = calcMovementDate(week, day, unitDispensing);
		
		// erase previous movement records
		eraseMovements(movDt, unitDispensing);
		
		Tbunit unit = unitDispensing.getTbunit();
		
		// generate new movements
		for (MedicineDispensingInfo mdi: infoItems) {
			Source source = mdi.getSource();
			Medicine medicine = mdi.getMedicine();
			
			Map<Batch, Integer> batches = selectBatches(unitDispensing, mdi.getSource(), mdi.getMedicine(), mdi.getQtdDispensed());
			movementHome.newMovement(movDt, unit, source, medicine, MovementType.DISPENSING, batches, null);
		}
		batches = null;
	}
*/
	
	
	/**
	 * Select batches for dispensing
	 * @param source
	 * @param med
	 * @param qtd
	 * @return
	 */
/*	protected Map<Batch, Integer> selectBatches(UnitDispensing unitDispensing, Source source, Medicine med, int qtd) {
		if (batches == null) {
			batches = entityManager.createQuery("from BatchQuantity b " +
					"join fetch b.batch bat " +
					"where b.tbunit.id = " + unitDispensing.getTbunit().getId() + 
					"order by bat.expiryDate")
					.getResultList();			
		}
		
		Map<Batch, Integer> res = new HashMap<Batch, Integer>();
		
		for (BatchQuantity b: batches) {
			// is batch of the medicine and source selected ?
			if ((b.getSource().equals(source)) && (b.getBatch().getMedicine().equals(med))) {
				// get quantity reserved
				int resqtd = b.getQuantityDispensing();
				// select the batch and the quantity to be removed from the batch
				if (qtd > resqtd) {
					res.put(b.getBatch(), resqtd);
					qtd -= resqtd;
				}
				else {
					res.put(b.getBatch(), qtd);
					break;
				}
			}
		}
		
		return res;
	}
*/
	

	/**
	 * Erase the previous dispensing medicine movements according to the entered date
	 * @param movDt
	 */
/*	protected void eraseMovements(Date movDt, UnitDispensing unitDispensing) {
		String hql = "from Movement m where m.date = :dt and m.tbunit.id = :id and m.type = :type";
		
		List<Movement> lst = entityManager.createQuery(hql)
			.setParameter("dt", movDt)
			.setParameter("id", unitDispensing.getTbunit().getId())
			.setParameter("type", MovementType.DISPENSING)
			.getResultList();
		
		for (Movement mov: lst) {
			movementHome.removeMovement(mov);
		}
	}
*/

	/**
	 * Check if the quantity dispensing is compatible with the quantity in stock
	 * @param dtMov
	 * @return
	 */
/*	public boolean checkStockQuantity(int week, int day, UnitDispensing unitDispensing) {
		if (infoItems == null)
			return false;
		
		Date dtMov = calcMovementDate(week, day, unitDispensing);
		
		String hql = "select s.source.id, s.medicine.id, s.quantity " +
			"from StockPosition s " +
			"where s.tbunit.id = #{unitDispensing.tbunit.id} " +
			"and s.date = (select max(aux.date) from StockPosition aux " +
			"where aux.tbunit.id = s.tbunit.id and aux.source.id = s.source.id " +
			"and aux.medicine.id = s.medicine.id and aux.date <= :dt)";
		
		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("dt", dtMov)
			.getResultList();
		
		String s = "";
		boolean ret = true;

		for (MedicineDispensingInfo di: infoItems) {
			Integer quantity = null;
			if (di.getQtdDispensed() > 0) {
				for (Object[] vals: lst) {
					Integer sourceId = (Integer)vals[0];
					Integer medicineId = (Integer)vals[1];
					Integer qtd = (Integer)vals[2];

					if ((di.getMedicineId().equals(medicineId)) && (di.getSourceId().equals(sourceId))) {
						quantity = qtd;
						break;
					}
				}
				
				if ((quantity == null) || (di.getQtdDispensed() > quantity)) {
					if (!s.isEmpty())
						s += ", "; 
					s += di.getMedicine().toString() + " (" + di.getSource().getName() + ")";
					ret = false;				
				}				
			}
		}

		
		
//		for (Object[] vals: lst) {
//			Integer sourceId = (Integer)vals[0];
//			Integer medicineId = (Integer)vals[1];
//			Integer quantity = (Integer)vals[2];
//			
//			MedicineDispensingInfo mdi = findMedicineDispensingInfo(sourceId, medicineId);
//			if (mdi != null) {
//				if (mdi.getQtdDispensed() > quantity) {
//					if (!s.isEmpty())
//						s += ", "; 
//					s += mdi.getMedicine().toString() + " (" + mdi.getSource().getName() + ")";
//					ret = false;
//				}
//			}
//		}
		
		if (!ret) {
			String datedisp = DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(dtMov);
			Object[] params = {datedisp, s};
			facesMessages.addFromResourceBundle("medicines.dispensing.nostockqtd", params);
		}
		
		return ret;
	}
*/
	
	
	/**
	 * Clear all information about dispensing
	 */
	public void clearInfo() {
		infoItems = null;
	}

	
	/**
	 * Calculate the movement date
	 * @param week
	 * @param day
	 * @return
	 */
/*	protected Date calcMovementDate(int week, int day, UnitDispensing unitDispensing) {
		// calculate movement day
		UnitDispensing unitDisp = unitDispensing;
		int year = unitDisp.getYear();
		int month = unitDisp.getMonth();
		
		Calendar c = Calendar.getInstance();
		c.clear();
		
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		
		if (day != 0)
			c.set(Calendar.DAY_OF_MONTH, day);
		else 
		if (week != 0) {
			int d = DateUtils.calcMonthDay(year, month-1, week, Calendar.SATURDAY);
			c.set(Calendar.DAY_OF_MONTH, d);
		}
		else c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		
		return c.getTime();		
	}
*/


	/**
	 * Clear dispensing info and refresh movements 
	 */
/*	public void clearDispensing(Date dtIni, Date dtEnd, UnitDispensing unitDispensing) {
		clearInfo();
		loadReservedInfo();
		loadMovementInfo(dtIni, dtEnd, unitDispensing);
	}
*/
}
