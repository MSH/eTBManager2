package org.msh.tb.medicines.movs;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.BatchMovement;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Movement;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.StockPosition;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.utils.date.DateUtils;


@Name("movementHome")
public class MovementHome {

	@In(create=true)
	private EntityManager entityManager;
	
	private int qtd;
	private float totalPrice;


	/**
	 * Generates a new stock medicine transaction. The new movement will influence in the stock quantity of the medicine in the TB Unit
	 * @param date
	 * @param unit
	 * @param source
	 * @param medicine
	 * @param type
	 * @param batches
	 * @param comment
	 * @return
	 * @throws Exception 
	 */
	@Transactional
	public Movement newMovement(Date date, Tbunit unit, Source source, Medicine medicine, MovementType type, Map<Batch, Integer> batches, String comment) {
		int oper = type.getOper();
		date = DateUtils.getDatePart(date);
		
		if (!unit.isMedicineManagementStarted())
			throw new MovementException("Error creating movement. Unit not in medicine management control");
		
		if (date.before(unit.getMedManStartDate()))
			throw new MovementException("Date cannot be before unit start date of medicine management on " + unit.getMedManStartDate().toString());

		Movement mov = new Movement();
		mov.setDate(date);
		mov.setTbunit(unit);
		mov.setSource(source);
		mov.setMedicine(medicine);
		mov.setType(type);
		mov.setComment(comment);
		mov.setOper(oper);
		mov.setRecordDate(new Date());

		qtd = 0;
		totalPrice = 0;
		// create batches
		if (batches != null)
			updateBatches(batches, mov);
		
		if (qtd == 0)
			return null;
		
		mov.setUnitPrice(totalPrice / qtd);
		mov.setQuantity(qtd);
		
		qtd = qtd * oper;
		totalPrice = totalPrice * oper;
		
		// update stock position
		StockPosition sp = getDayPosition(date, unit, source, medicine);

		if ((sp != null) && (sp.getDate().equals(date))) {
			sp.setQuantity(sp.getQuantity() + qtd);
			sp.setTotalPrice(sp.getTotalPrice() + totalPrice);
		}
		else {
			StockPosition aux = new StockPosition();
			aux.setDate(date);
			aux.setMedicine(medicine);
			aux.setSource(source);
			aux.setTbunit(unit);
			aux.setQuantity(qtd);
			if (sp != null) {
				 aux.setQuantity(sp.getQuantity() + qtd);
				 aux.setTotalPrice(sp.getTotalPrice() + totalPrice);
			}
			else {
				aux.setQuantity(qtd);
				aux.setTotalPrice(totalPrice);
			}
			sp = aux;
		}
		
		if (sp.getQuantity() < 0)
			throw new MovementException("The movement operation resulted in a negative stock quantity for " + sp.getMedicine().toString() + " from " + sp.getSource().toString());
		
		mov.setStockQuantity(sp.getQuantity());
		
		entityManager.persist(sp);
		
		entityManager.persist(mov);
		
		// update stock positions over the date of the movement
		entityManager.createQuery("update StockPosition sp " +
				"set sp.quantity = sp.quantity + :qtd, " +
				"sp.totalPrice = sp.totalPrice + :price " +
				"where sp.tbunit.id = :unitid and sp.source.id = :sourceid and sp.medicine.id = :medid " +
				"and sp.date > :dt")
				.setParameter("qtd", qtd)
				.setParameter("price", totalPrice)
				.setParameter("unitid", unit.getId())
				.setParameter("sourceid", source.getId())
				.setParameter("medid", medicine.getId())
				.setParameter("dt", date)
				.executeUpdate();

		// update stock position in the movement records
		entityManager.createQuery("update Movement m set m.stockQuantity = m.stockQuantity + :qtd " + 
				"where (m.date > :data or (m.date = :data and m.recordDate < :datarec)) " +
				"and m.medicine.id = :medid and m.source.id = :sourceid " +
				"and m.tbunit.id = :unitid")
				.setParameter("data", date)
				.setParameter("datarec", mov.getRecordDate())
				.setParameter("medid", mov.getMedicine().getId())
				.setParameter("sourceid", mov.getSource().getId())
				.setParameter("unitid", mov.getTbunit().getId())
				.setParameter("qtd", qtd)
				.executeUpdate();

		entityManager.flush();
		
		return mov;
	}


	/**
	 * Update batch quantities
	 * @param batches
	 * @param mov
	 * @throws MovementException 
	 */
	protected void updateBatches(Map<Batch, Integer> batches, Movement mov) {
		MovementType type = mov.getType();
		int oper = type.getOper();

		for (Batch b: batches.keySet()) {
			BatchMovement bm = new BatchMovement();
			bm.setBatch(b);
			int bqtd = batches.get(b);
			bm.setQuantity(bqtd);
			bm.setMovement(mov);
			mov.getBatches().add(bm);
			entityManager.persist(b);

			BatchQuantity batchQtd = loadBatchQuantity(b, mov.getTbunit(), mov.getSource());
			
			int qtdoper = bqtd * oper;
			int remqtd = batchQtd.getQuantity() + qtdoper;
			if (remqtd < 0)
				throw new MovementException("The batch remaining quantity cannot be negative");

			batchQtd.setQuantity(remqtd);

			// is a health unit receiving from an order ?
/*			if ((type == MovementType.ORDERRECEIVING) && (mov.getTbunit().isTreatmentHealthUnit())) {
				batchQtd.setQuantityDispensing(batchQtd.getQuantityDispensing() + bqtd);
			}

			// is dispensing ?
			if (type == MovementType.DISPENSING) {
				// check if there is reserved batch
				Integer resqtd = batchQtd.getQuantityDispensing();
				if ((resqtd == null) || (resqtd + qtdoper < 0))
					throw new MovementException("The reserved quantity is null or lower than the quantity dispensed");
				batchQtd.setQuantityDispensing(resqtd + qtdoper);
			}
			else {
				// adjust reserved quantity for dispensing if necessary
				int qtdDisp = batchQtd.getQuantityDispensing();
				if (qtdDisp > batchQtd.getQuantity())
					batchQtd.setQuantityDispensing(qtdDisp);
			}
*/
			// save batch quantity
			if (batchQtd.getQuantity() == 0)
				 entityManager.remove(batchQtd);
			else entityManager.persist(batchQtd);
			
			qtd += bqtd;
			totalPrice += bm.getBatch().getUnitPrice() * bqtd;
		}
	}
	
	
	/**
	 * Load batch quantity information from a unit and source
	 * @param batch to load information
	 * @param unit to find batch information
	 * @param source to find batch information
	 * @return BatchQuantity instance
	 */
	public BatchQuantity loadBatchQuantity(Batch batch, Tbunit unit, Source source) {
		// load information about batch quantity
		try {
			return (BatchQuantity)entityManager.createQuery("from BatchQuantity b where b.batch.id = :batchid " +
				"and b.tbunit.id = :unitid and b.source.id = :sourceid")
				.setParameter("batchid", batch.getId())
				.setParameter("unitid", unit.getId())
				.setParameter("sourceid", source.getId())
				.getSingleResult();
			
		} catch (NoResultException e) {
			BatchQuantity bq = new BatchQuantity();
			bq.setBatch(batch);
			bq.setTbunit(unit);
			bq.setSource(source);
			return bq;
		}
		
	}

	/**
	 * Remove a stock transaction updating the stock position
	 * @param mov
	 */
	@Transactional
	public void removeMovement(Movement mov) {
		int qtd = mov.getQtdOperation();
		
		entityManager.createQuery("update StockPosition sp set sp.quantity = sp.quantity - :qtd " +
				"where sp.date >= :data and sp.medicine.id = :medid and sp.source.id = :sourceid " +
				"and sp.tbunit.id = :unitid")
				.setParameter("data", mov.getDate())
				.setParameter("medid", mov.getMedicine().getId())
				.setParameter("sourceid", mov.getSource().getId())
				.setParameter("unitid", mov.getTbunit().getId())
				.setParameter("qtd", qtd)
				.executeUpdate();

		// update quantity recorded in movements
		entityManager.createQuery("update Movement m set m.stockQuantity = m.stockQuantity - :qtd " + 
				"where (m.date > :data or (m.date = :data and m.recordDate < :datarec)) " +
				"and m.medicine.id = :medid and m.source.id = :sourceid " +
				"and m.tbunit.id = :unitid")
				.setParameter("data", mov.getDate())
				.setParameter("datarec", mov.getRecordDate())
				.setParameter("medid", mov.getMedicine().getId())
				.setParameter("sourceid", mov.getSource().getId())
				.setParameter("unitid", mov.getTbunit().getId())
				.setParameter("qtd", qtd)
				.executeUpdate();
		
//		MovementType type = mov.getType();
		for (BatchMovement bm: mov.getBatches()) {
			Batch b = bm.getBatch();
			
			BatchQuantity bq = loadBatchQuantity(b, mov.getTbunit(), mov.getSource());
			
			bq.setQuantity(bq.getQuantity() - bm.getQtdOperation());
			
/*			// update reserved batch, in case of dispensing
			if (type == MovementType.DISPENSING) {
				int res = bq.getQuantityDispensing();
				if (res == 0)
					 res = bm.getQtdOperation();
				else res -= bm.getQtdOperation();
				bq.setQuantityDispensing(res);
			}
			
			if ((bq.getQuantity() < 0) || (bq.getQuantityDispensing() < 0))
*/
			if (bq.getQuantity() < 0)
				throw new MovementException("The batch quantity cannot be negative");
			
			if (bq.getQuantity() == 0)
				 entityManager.remove(bq);
			else entityManager.persist(bq);
		}

		entityManager.remove(mov);
		entityManager.flush();
	}
	
	/**
	 * Retorna a posição de estoque em uma determinada data
	 * @param dt
	 * @param tbunit
	 * @param source
	 * @param prod
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public StockPosition getDayPosition(Date dt, Tbunit tbunit, Source source, Medicine prod) {
		List<StockPosition> lst = entityManager.createQuery("from StockPosition sp where sp.date = " +
				"(select max(sp2.date) from StockPosition sp2 where sp2.tbunit.id = sp.tbunit.id " +
				"and sp2.date <= :data " + 
				"and sp2.source.id = sp.source.id and sp2.medicine.id = sp.medicine.id) " +
				"and sp.tbunit.id = :unitid and sp.source.id = :sourceid and sp.medicine.id = :prodid")
				.setParameter("data", dt)
				.setParameter("prodid", prod.getId())
				.setParameter("sourceid", source.getId())
				.setParameter("unitid", tbunit.getId())
				.getResultList();

		if (lst.size() == 0)
		 	 return null;
		else return lst.get(0);
	}



	/**
	 * Check if stock can be decreased in the specific date with the specific quantity
	 * @param tbunit
	 * @param source
	 * @param medicine
	 * @param movQuantity
	 * @param movDate
	 * @return
	 */
	public boolean canDecreaseStock(Tbunit tbunit, Source source, Medicine medicine, int movQuantity, Date movDate) {
		Long count = (Long)entityManager
			.createQuery("select count(*) from StockPosition sp " +
					"where sp.tbunit.id = :unitid " +
					"and sp.source.id = :sourceid " +
					"and sp.medicine.id = :medicineid " +
					"and date >= (select max(a.date) from StockPosition a " +
					"where a.tbunit.id = sp.tbunit.id " +
					"and a.medicine.id = sp.medicine.id " +
					"and a.source.id = sp.source.id " +
					"and a.date <= :dt) " +
					"and sp.quantity < :qtd")
/*			.createQuery("select min(sp.quantity), min(sp.date) from StockPosition sp " + 
					"where sp.tbunit.id = :unitid " +
					"and sp.source.id = :sourceid " +
					"and sp.medicine.id = :medicineid " +
					"and date >= :dt")
*/			.setParameter("unitid", tbunit.getId())
			.setParameter("sourceid", source.getId())
			.setParameter("medicineid", medicine.getId())
			.setParameter("dt", movDate)
			.setParameter("qtd", movQuantity)
			.getSingleResult();
		
		return (count == 0);
/*		Integer num = (Integer)vals[0];
		Date dt = (Date)vals[1];
		
		if (num == null)
			return true;
		
		return (((num != null) && (num - movQuantity >= 0)) 
				&& ((dt != null) && (!dt.after(movDate))));
*/	}
}
