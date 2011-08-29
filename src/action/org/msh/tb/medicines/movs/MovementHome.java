package org.msh.tb.medicines.movs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.LocaleDateConverter;


@Name("movementHome")
public class MovementHome {

	@In(create=true)
	private EntityManager entityManager;
	
/*	private int qtd;
	private float totalPrice;
*/
	private List<PreparedMovement> preparedMovements;
	private List<Movement> movementsToBeRemoved;
	private List<StockPosition> stockPositions;
	private List<BatchQuantity> batchQuantities;
	
	private String errorMessage;


	/**
	 * Initialize the class {@link MovementHome} to start recording movement transactions.
	 * This is the first method to be called before starting saving or updating movements
	 */
	public void initMovementRecording() {
		preparedMovements = null;
		movementsToBeRemoved = null;
	}


	/**
	 * Prepare a new movement to be registered and include it in the list of prepared movements.
	 * When all movements are ready to be saved, the method savePreparedMovements() must be called to
	 * commit the movement recording.
	 * While preparing the movement, the system will test dates and quantities checking if somewhere the
	 * quantity will be negative. If the test fails, the system will return null and the error described
	 * in the getErrorMessage() method
	 * @param date
	 * @param unit
	 * @param source
	 * @param medicine
	 * @param type
	 * @param batches
	 * @param comment
	 * @return an instance of {@link Movement} class, that will be saved when the method savePreparedMovements() is called, or null if the movement could not be prepared
	 */
	public Movement prepareNewMovement(Date date, Tbunit unit, Source source, Medicine medicine, MovementType type, Map<Batch, Integer> batches, String comment) {
		try {
			PreparedMovement pm = createPreparedMovement(date, unit, source, medicine,
					type, batches, comment);
			return pm.getMovement();
		} catch (MovementException e) {
			errorMessage = e.getMessage();
			return null;
		}
	}




	
	/**
	 * Prepare movements to be removed. The movement will just be removed when the method savePreparedMovements() is called. 
	 * The movement to be removed will be considered when preparing new movements.   
	 * @param mov instance of {@link Movement} to be removed from the system
	 */
	public void prepareMovementsToRemove(Movement mov) {
		// there are movements already prepared to be saved ?
		if (preparedMovements != null) {
			// check if a movement for the same medicine, source and unit was already prepared to be saved
			boolean canRemove = true;
			Integer medId = mov.getMedicine().getId();
			Integer sourceId = mov.getSource().getId();
			Integer unitId = mov.getTbunit().getId();
			
			for (PreparedMovement pm: preparedMovements) {
				Movement aux = pm.getMovement();
				if ((aux.getMedicine().getId().equals(medId)) &&
					(aux.getSource().getId().equals(sourceId)) &&
					(aux.getTbunit().getId().equals(unitId))) 
				{
					canRemove = false;
					break;
				}
			}
			
			if (!canRemove)
				throw new MovementException("Movements must be prepared to be removed before preparing new movements");
		}
		
		if (movementsToBeRemoved == null)
			movementsToBeRemoved = new ArrayList<Movement>();

		movementsToBeRemoved.add(mov);
	}



	/**
	 * Save prepared movements in database and update stock position of medicines. Before saving the prepared movements,
	 * the system checks if there are movements to be removed, and execute it making an adjustment in the stock quantity
	 */
	@Transactional
	public void savePreparedMovements() {
		// remove previous movement
		if (movementsToBeRemoved != null) {
			for (Movement mov: movementsToBeRemoved) {
				StockPosition sp = findStockPosition(mov.getTbunit(), mov.getSource(), mov.getMedicine());
				sp.setQuantity( sp.getQuantity() - (mov.getQuantity() * mov.getOper()) );
				
				for (BatchMovement bm: mov.getBatches()) {
					BatchQuantity bq = findBatchQuantity(mov.getTbunit(), mov.getSource(), bm.getBatch());
					bq.setQuantity( bq.getQuantity() - (bm.getQuantity() * mov.getOper()) );
				}
				
				entityManager.remove(mov);
			}
			movementsToBeRemoved = null;
		}

		// create batch movements and save changes to the batch quantities of the unit
		if (preparedMovements != null)
		for (PreparedMovement pm: preparedMovements) {
			Movement mov = pm.getMovement();
			for (Batch batch: pm.getBatches().keySet()) {
				int batchqtd = pm.getBatches().get(batch);

				BatchMovement bm = new BatchMovement();
				BatchQuantity bq = findBatchQuantity(mov.getTbunit(), mov.getSource(), batch);
				bq.setQuantity( bq.getQuantity() + (batchqtd * mov.getOper()));

				bm.setBatch(batch);
				bm.setQuantity(batchqtd);
				bm.setMovement(mov);
				mov.getBatches().add(bm);
			}

			entityManager.persist(mov);

			StockPosition sp = findStockPosition(mov.getTbunit(), mov.getSource(), mov.getMedicine());
			sp.setQuantity( sp.getQuantity() + mov.getQtdOperation());
			sp.setTotalPrice( sp.getTotalPrice() + (mov.getTotalPrice() * mov.getOper()) );
			entityManager.persist(sp);
		}

		for (StockPosition sp: stockPositions)
			entityManager.persist(sp);
		
		for (BatchQuantity bq: batchQuantities)
			entityManager.persist(bq);
		
		entityManager.flush();

		preparedMovements = null;
	}



	/**
	 * Find {@link StockPosition} instance by its unit, source and medicine
	 * @param unit
	 * @param source
	 * @param medicine
	 * @return
	 */
	protected StockPosition findStockPosition(Tbunit unit, Source source, Medicine medicine) {
		if (stockPositions == null)
			stockPositions = new ArrayList<StockPosition>();
		
		for (StockPosition sp: stockPositions) {
			if ((sp.getTbunit().equals(unit)) && (sp.getSource().equals(source)) && (sp.getMedicine().equals(medicine)))
				return sp;
		}
		
		List<StockPosition> lst = entityManager.createQuery("from StockPosition where tbunit.id = :unit and source.id = :source and medicine.id = :med")
			.setParameter("unit", unit.getId())
			.setParameter("source", source.getId())
			.setParameter("med", medicine.getId())
			.getResultList();
		
		if (lst.size() > 0) {
			StockPosition sp = lst.get(0);
			stockPositions.add(sp);
			return sp;
		}
		
		StockPosition sp = new StockPosition();
		stockPositions.add(sp);
		sp.setTbunit(unit);
		sp.setSource(source);
		sp.setMedicine(medicine);
		return sp;
	}
	
	
	
	/**
	 * Find or create an instance of {@link BatchQuantity} class by its unit, source and batch 
	 * @param unit
	 * @param source
	 * @param batch
	 * @return
	 */
	protected BatchQuantity findBatchQuantity(Tbunit unit, Source source, Batch batch) {
		BatchQuantity bq = loadBatchQuantity(batch, unit, source);

		if (batchQuantities == null)
			batchQuantities = new ArrayList<BatchQuantity>();
		batchQuantities.add(bq);
	
		return bq;
	}
	
	/**
	 * Update stock position and movements after the movement passed as parameter, increment the values 
	 * (quantity and price) in stock according to the parameters qtd and price 
	 * @param mov
	 * @param qtd
	 * @param price
	 */
/*	protected void execMovementUpdate(Movement mov, int qtd, float price) {
		entityManager.createQuery("update StockPosition sp " +
				"set sp.quantity = sp.quantity + :qtd, " +
				"sp.totalPrice = sp.totalPrice + :price " +
				"where sp.tbunit.id = :unitid and sp.source.id = :sourceid and sp.medicine.id = :medid " +
				"and sp.date > :dt")
				.setParameter("qtd", qtd)
				.setParameter("price", price)
				.setParameter("unitid", mov.getTbunit().getId())
				.setParameter("sourceid", mov.getSource().getId())
				.setParameter("medid", mov.getMedicine().getId())
				.setParameter("dt", mov.getDate())
				.executeUpdate();

		// update stock position in the movement records
		entityManager.createQuery("update Movement m set m.stockQuantity = m.stockQuantity + :qtd " + 
				"where ((m.date > :data) or (m.date = :data and m.recordDate > :recdate)) " +
				"and m.medicine.id = :medid and m.source.id = :sourceid " +
				"and m.tbunit.id = :unitid")
				.setParameter("data", mov.getDate())
				.setParameter("medid", mov.getMedicine().getId())
				.setParameter("sourceid", mov.getSource().getId())
				.setParameter("unitid", mov.getTbunit().getId())
				.setParameter("recdate", mov.getRecordDate())
				.setParameter("qtd", qtd)
				.executeUpdate();
	}
*/
	
	/**
	 * Create a new prepared movement. In case any problem with the new movement is detected, a {@link MovementException}
	 * will be generated
	 * @param date
	 * @param unit
	 * @param source
	 * @param medicine
	 * @param type
	 * @param batches
	 * @param comment
	 * @return
	 */
	private PreparedMovement createPreparedMovement(Date date, Tbunit unit,
			Source source, Medicine medicine, MovementType type,
			Map<Batch, Integer> batches, String comment) {
		// check unit dates
		if (!unit.isMedicineManagementStarted())
			throw new MovementException("Error creating movement. Unit not in medicine management control");

		if (date.before(unit.getMedManStartDate())) {
			String s = Messages.instance().get("meds.movs.datebefore");
			s = MessageFormat.format(s, unit.getMedManStartDate());
			throw new MovementException(s);
		}

		// create batches
		if ((batches == null) || (batches.size() == 0)) 
			throw new MovementException("No batches were specified for movement of " + medicine.toString());
		

		// initialize some variables
		date = DateUtils.getDatePart(date);
		int oper = type.getOper();
		float totalPrice = 0;
		int qtd = 0;
		for (Batch b: batches.keySet()) {
			int qtdaux = batches.get(b);
			qtd += qtdaux;
			totalPrice += b.getUnitPrice() * qtdaux;
		}

		if (qtd == 0)
			throw new MovementException("No movement to be executed because the quantity is 0");

		// validates batches quantities
		for (Batch batch: batches.keySet()) {
			int newqtd = batches.get(batch) * oper;
			ReturnedValue val = calcQuantityToBeReturned(date, medicine, source, unit, batch);
			int remqtd = val.getQuantity();

			// only test if it's reducing the total quantity
			if ((newqtd < 0) || (remqtd < 0)) {
				// test the worst scenario: both movements will reduce quantity and they are in different dates
				if ((!(remqtd < 0) && (newqtd < 0)) || (date.equals(val.getDate()))) {
					// ok, just one movement is reducing the quantity
					Date dt = date;
					if (remqtd < 0)
						dt = val.getDate();
					testBathQuantityReducing(batch, newqtd + remqtd, dt, source, unit);
				}
				else {
					// both of the movements reduces the quantity
					if (val.getDate().before(date)) {
						testBathQuantityReducing(batch, remqtd, val.getDate(), source, unit);
						testBathQuantityReducing(batch, newqtd + remqtd, date, source, unit);
					}
					else {
						testBathQuantityReducing(batch, newqtd + remqtd, val.getDate(), source, unit);
						testBathQuantityReducing(batch, newqtd, date, source, unit);
					}
				}
			}
		}
		
/*		// validate and calculate the quantity to be included/reduced from the batches of the unit
		List<BatchOperation> batchOperations = prepareBatches(unit, source, batches, oper);
*/
		// get quantity to be removed from movements
/*		ReturnedValue val = calcQuantityToBeReturned(date, medicine, source, unit);
		int qtdRem = (val != null? val.getQuantity(): 0);
		float priceRem = (val != null? val.getPrice(): 0F);

		// get stock position of the day
		StockPosition sp = getDayPosition(date, unit, source, medicine);

		if ((sp == null) || (!sp.getDate().equals(date))) {
			StockPosition aux = new StockPosition();
			aux.setDate(date);
			aux.setMedicine(medicine);
			aux.setSource(source);
			aux.setTbunit(unit);
			aux.setQuantity(qtd);
			if (sp != null) {
				// there is a stock position record before
				aux.setQuantity( sp.getQuantity() );
				aux.setTotalPrice( sp.getTotalPrice() );
			}
			else {
				// there is nothing before
				aux.setQuantity( 0 );
				aux.setTotalPrice( 0F );
			}
			sp = aux;
		}
*/

		// calculate new quantities in stock
/*		int dxQtd = (qtd * oper) + qtdRem;
		float dxPrice = (totalPrice * oper) + priceRem;
		
		// check in the current StockPosition object if quantity will be negative because of this transaction
		if (sp.getQuantity() + dxQtd < 0)
			throw new MovementException(MessageFormat.format(Messages.instance().get("MovementException.NEGATIVE_STOCK"), sp.getMedicine().toString() + "[" + sp.getSource().getAbbrevName().toString() + "]"));

		// if operation will result in a decrease of stock quantity, check if somewhere in the future it will be negative
		if (dxQtd < 0) {
			// select number of records where the movement will result in a negative stock quantity from the date
			Long num = (Long)entityManager.createQuery("select count(m.id) from Movement m " +
				"where m.date > :dt and m.stockQuantity + :qtd < 0 " + 
				"and m.tbunit.id = :unitid and m.source.id = :sourceid and m.medicine.id = :medid")
				.setParameter("qtd", dxQtd)
				.setParameter("unitid", unit.getId())
				.setParameter("sourceid", source.getId())
				.setParameter("medid", medicine.getId())
				.setParameter("dt", sp.getDate())
				.getSingleResult();
			if (num > 0)
				throw new MovementException(MessageFormat.format(Messages.instance().get("MovementException.NEGATIVE_STOCK"), sp.getMedicine().toString() + "[" + sp.getSource().getAbbrevName().toString() + "]"));
		}

		// update quantity of the object, so it will be saved later
		sp.setQuantity( sp.getQuantity() + dxQtd );
		sp.setTotalPrice( sp.getTotalPrice() + dxPrice );
*/		
		// prepare the new movement to be saved later
		PreparedMovement pm = new PreparedMovement();

		Movement mov = new Movement();
		mov.setDate(date);
		mov.setTbunit(unit);
		mov.setSource(source);
		mov.setMedicine(medicine);
		mov.setType(type);
		mov.setComment(comment);
		mov.setOper(oper);
		mov.setRecordDate(new Date());
		mov.setQuantity(qtd);
		mov.setTotalPrice(totalPrice);

		pm.setMovement(mov);
		pm.setBatches(batches);
/*		pm.setBatchOperations(batchOperations);
		pm.setStockPosition(sp);
*/		
		if (preparedMovements == null)
			preparedMovements = new ArrayList<PreparedMovement>();
		preparedMovements.add(pm);
		return pm;
	}

	
	/**
	 * Test if batch quantity can be reduced from a specific date. If quantity cannot be reduced, a {@link MovementException} will be rose with
	 * descriptions about the error 
	 * @param batch
	 * @param qtd
	 * @param date
	 * @param source
	 * @param unit
	 */
	protected void testBathQuantityReducing(Batch batch, int qtd, Date date, Source source, Tbunit unit) {
		// execute quantity validation
		// verify if, in case of reduction (i.e, dispensing/transfer/etc) if the quantity will be negative from the date to the future 
		String hql = "select m.date from BatchMovement a join a.movement m " +
				"where (select sum(b.quantity*b.movement.oper) - :qtd from BatchMovement b " +
				"join b.movement m2 " +
				"where b.batch.id=a.batch.id and m2.tbunit.id=m.tbunit.id and m2.source.id=m.source.id " +
				"and ((m2.date < m.date) or (m2.date = m.date and m2.recordDate <= m.recordDate))) < 0 " +
				"and a.batch.id = :batch and m.tbunit.id=:unit and m.source.id=:source and m.date >= :dt";

		if (qtd < 0) {
			List<Date> lst = entityManager.createQuery(hql)
				.setParameter("qtd", (long)qtd)
				.setParameter("batch", batch.getId())
				.setParameter("source", source.getId())
				.setParameter("unit", unit.getId())
				.setParameter("dt", date)
				.getResultList();

			if (lst.size() > 0)
				throw new MovementException(MessageFormat.format(Messages.instance().get("MovementException.NEGATIVE_STOCK"), batch.getId(), LocaleDateConverter.getDisplayDate(lst.get(0))));
		}
	}
	
	/**
	 * Prepare batches and check if quantity of the movement is compatible with the quantity available 
	 * @param unit
	 * @param source
	 * @param batches
	 * @return
	 */
/*	protected List<BatchOperation> prepareBatches(Tbunit unit, Source source, Map<Batch, Integer> batches, int oper) {
		List<BatchOperation> lst = new ArrayList<MovementHome.BatchOperation>();

		for (Batch b: batches.keySet()) {
			Integer qtd = batches.get(b);
			if ((qtd != null) && (qtd != 0)) {
				BatchQuantity batchQuantity = loadBatchQuantity(b, unit, source);
				
				if (batchQuantity == null)
					throw new MovementException("No batch of " + b.getMedicine().toString() + 
							" found for " + unit.toString() + ", " + source.toString());
				
				int availableQtd = batchQuantity.getQuantity() + (qtd * oper);
				
				// get quantity to be removed from movements
				int qtdRem = calcQuantityToBeReturnedToBatch(batchQuantity);

				availableQtd += qtdRem;
				
				if (availableQtd < 0)
					throw new MovementException(MessageFormat.format(Messages.instance().get("MovementException.NEGATIVE_STOCK"), b.getMedicine().toString()));

				// the available quantity is the final quantity that will remain when all movements will be
				// removed and inserted in the system.

				// save quantity to be increased/decreased from the batchQuantity
				int qtdOper = (qtd * oper) + qtdRem;
				
				BatchOperation batchOper = new BatchOperation();
				batchOper.setBatchQuantity(batchQuantity);
				batchOper.setMovementQuantity(qtd);
				batchOper.setQuantityToBeIncluded(qtdOper);
				lst.add(batchOper);
			}
		}
		
		return lst;
	}
*/	

	/**
	 * Calculate the quantity of the medicine from the source x unit to be removed based on the 
	 * movements prepared to be removed.
	 * @param dt
	 * @param med
	 * @param source
	 * @param unit
	 * @return
	 */
	protected ReturnedValue calcQuantityToBeReturned(Date dt, Medicine med, Source source, Tbunit unit, Batch batch) {
		if (movementsToBeRemoved == null)
			return new ReturnedValue(0, 0, new Date());

		int qtd = 0;
		float price = 0;
		Date dtrem = null;

		for (Movement mov: movementsToBeRemoved) {
			if ((!mov.getDate().after(dt)) &&
				(mov.getMedicine().equals(med)) &&
				(mov.getSource().equals(source)) &&
				(mov.getTbunit().equals(unit))) 
			{
				for (BatchMovement bm: mov.getBatches()) {
					if (bm.getBatch().equals(batch)) {
						qtd += mov.getQuantity() * (-mov.getOper());
						price += mov.getTotalPrice() * (-mov.getOper());
						if ((dtrem == null) || (dtrem.after(mov.getDate())))
							dtrem = mov.getDate();
					}
				}
			}
		}
		return new ReturnedValue(qtd, price, dtrem);
	}


	/**
	 * Calculate the quantity that will be returned to an specific batch because of a prepared movement to be removed
	 * @param dt
	 * @param batchQtd
	 * @return
	 */
/*	protected int calcQuantityToBeReturnedToBatch(BatchQuantity batchQtd) {
		if (movementsToBeRemoved == null)
			return 0;
		
		Batch batch = batchQtd.getBatch();
		Medicine med = batch.getMedicine();
		Source source = batchQtd.getSource();
		Tbunit unit = batchQtd.getTbunit();
		
		int qtd = 0;
		for (Movement mov: movementsToBeRemoved) {
			if ((mov.getMedicine().equals(med)) &&
				(mov.getSource().equals(source)) &&
				(mov.getTbunit().equals(unit))) 
			{
				for (BatchMovement bm: mov.getBatches()) {
					if (bm.getBatch().getId().equals(batch.getId())) {
						qtd += bm.getQuantity() * (-mov.getOper());
						break;
					}
				}
			}
		}

		return qtd;
	}
*/
	
	/**
	 * Load batch quantity information from a unit and source
	 * @param batch to load information
	 * @param unit to find batch information
	 * @param source to find batch information
	 * @return BatchQuantity instance
	 */
	protected BatchQuantity loadBatchQuantity(Batch batch, Tbunit unit, Source source) {
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
	 * Return information about the stock position at the specified date
	 * @param dt
	 * @param tbunit
	 * @param source
	 * @param med
	 * @return
	 */
/*	protected Movement getDatePosition(Date dt, Tbunit tbunit, Source source, Medicine med) {
		List<Movement> lst = entityManager.createQuery("from Movement m " +
				"where m.date = " +
				"(select max(aux.date) from Movement aux where aux.tbunit.id = m.tbunit.id " +
				"and aux.date <= :data " + 
				"and aux.source.id = m.source.id and aux.medicine.id = m.medicine.id) " +
				"and m.tbunit.id = :unitid and m.source.id = :sourceid and m.medicine.id = :medid " +
				"order by m.recordDate desc")
				.setParameter("data", dt)
				.setParameter("medid", med.getId())
				.setParameter("sourceid", source.getId())
				.setParameter("unitid", tbunit.getId())
				.setMaxResults(1)
				.getResultList();

		if (lst.size() == 0)
		 	 return null;
		else return lst.get(0);
	}
*/	

	/**
	 * Retorna a posição de estoque em uma determinada data
	 * @param dt
	 * @param tbunit
	 * @param source
	 * @param prod
	 * @return
	 */
	protected StockPosition getDayPosition(Date dt, Tbunit tbunit, Source source, Medicine prod) {
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
	 * Store information about a prepared movement
	 * @author Ricardo Memoria
	 *
	 */
	private class PreparedMovement {
		private Movement movement;
		private Map<Batch, Integer> batches;
		/**
		 * @return the movement
		 */
		public Movement getMovement() {
			return movement;
		}
		/**
		 * @param movement the movement to set
		 */
		public void setMovement(Movement movement) {
			this.movement = movement;
		}
		/**
		 * @return the batches
		 */
		public Map<Batch, Integer> getBatches() {
			return batches;
		}
		/**
		 * @param batches the batches to set
		 */
		public void setBatches(Map<Batch, Integer> batches) {
			this.batches = batches;
		}
	}


	/**
	 * Returned value from removed movements
	 * @author Ricardo Memoria
	 *
	 */
	private class ReturnedValue {
		private int quantity;
		private float price;
		private Date date;

		public ReturnedValue(int quantity, float price, Date date) {
			super();
			this.quantity = quantity;
			this.price = price;
			this.date = date;
		}
		/**
		 * @return the quantity
		 */
		public int getQuantity() {
			return quantity;
		}
		/**
		 * @return the price
		 */
		public float getPrice() {
			return price;
		}
		
		/**
		 * Return the date of the first movement to be removed
		 * @return
		 */
		public Date getDate() {
			return date;
		}
	}


	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
