package org.msh.tb.medicines.movs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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


@Name("movementHome")
public class MovementHome {

	@In(create=true)
	private EntityManager entityManager;
	
/*	private int qtd;
	private float totalPrice;
*/
	private List<PreparedMovement> preparedMovements;
	private List<Movement> movementsToBeRemoved;
	
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
	 * quantity will be negative. If the test fails, the system will raise a {@link MovementException} exception
	 * with a message describing the error
	 * @param date
	 * @param unit
	 * @param source
	 * @param medicine
	 * @param type
	 * @param batches
	 * @param comment
	 * @return an instance of {@link Movement} class, that will be saved when the method savePreparedMovements() is called.
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
				execMovementUpdate(mov, mov.getQuantity() * (-mov.getOper()), mov.getTotalPrice() * (-mov.getOper()));
				entityManager.remove(mov);
			}
			movementsToBeRemoved = null;
		}

		if (preparedMovements == null)
			return;

		// create batch movements and save changes to the batch quantities of the unit
		for (PreparedMovement pm: preparedMovements) {
			Movement mov = pm.getMovement();
			for (BatchQuantity batchQuantity: pm.getBatchQuantities().keySet()) {
				Batch batch = batchQuantity.getBatch();
				int qtd = pm.getBatchQuantities().get(batchQuantity);
				
				BatchMovement bm = new BatchMovement();
				bm.setBatch(batch);
				bm.setQuantity(qtd);
				bm.setMovement(mov);
				mov.getBatches().add(bm);
				if (batchQuantity.getId() != null) {
					batchQuantity = entityManager.merge( batchQuantity );
					if (batchQuantity.getQuantity() == 0)
						entityManager.remove(batchQuantity);
				}
				else entityManager.persist( batchQuantity );
			}

			entityManager.persist(pm.getStockPosition());
			entityManager.persist(mov);

			int qtd = mov.getQuantity() * mov.getOper();
			float totalPrice = mov.getTotalPrice() * mov.getOper();
			
			// update stock positions over the date of the movement
			execMovementUpdate(mov, qtd, totalPrice);
		}

		entityManager.flush();

		preparedMovements = null;
	}

	
	/**
	 * Update stock position and movements after the movement passed as parameter, increment the values 
	 * (quantity and price) in stock according to the parameters qtd and price 
	 * @param mov
	 * @param qtd
	 * @param price
	 */
	protected void execMovementUpdate(Movement mov, int qtd, float price) {
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
			String s = Messages.instance().get("medicines.movs.datebefore");
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
//			b = entityManager.merge(b);
		}

		if (qtd == 0)
			throw new MovementException("No movement to be executed because the quantity is 0");
		
		Map<BatchQuantity, Integer> batchQuantities = prepareBatches(unit, source, batches, oper);

		// get quantity to be removed from movements
		ReturnedValue val = calcQuantityToBeReturned(date, medicine, source, unit);
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

		// calculate new quantities in stock
		int dxQtd = (qtd * oper) + qtdRem;
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
		mov.setUnitPrice(totalPrice / qtd);
		mov.setStockQuantity(sp.getQuantity());

		pm.setMovement(mov);
		pm.setBatchQuantities(batchQuantities);
		pm.setStockPosition(sp);
		
		if (preparedMovements == null)
			preparedMovements = new ArrayList<PreparedMovement>();
		preparedMovements.add(pm);
		return pm;
	}

	
	/**
	 * Prepare batches to be saved
	 * @param unit
	 * @param source
	 * @param batches
	 * @return
	 */
	protected Map<BatchQuantity, Integer> prepareBatches(Tbunit unit, Source source, Map<Batch, Integer> batches, int oper) {
		Map<BatchQuantity, Integer> lst = new HashMap<BatchQuantity, Integer>();

		for (Batch b: batches.keySet()) {
			Integer qtd = batches.get(b);
			if ((qtd != null) && (qtd != 0)) {
				BatchQuantity batchQuantity = loadBatchQuantity(b, unit, source);
				
				if (batchQuantity == null)
					throw new MovementException("No batch of " + b.getMedicine().toString() + 
							" found for " + unit.toString() + ", " + source.toString());
				
//				System.out.println(batchQuantity.getBatch().getBatchNumber() + " qtd available=" + batchQuantity.getQuantity());

				int availableQtd = batchQuantity.getQuantity() + (qtd * oper);
				
				// get quantity to be removed from movements
				int qtdRem = calcQuantityToBeReturnedToBatch(batchQuantity);

				availableQtd += qtdRem;
				
				if (availableQtd < 0)
					throw new MovementException(MessageFormat.format(Messages.instance().get("MovementException.NEGATIVE_STOCK"), b.getMedicine().toString()));

				// the available quantity is the final quantity that will remain when all movements will be
				// removed and inserted in the system.

				// update batch quantity
				batchQuantity.setQuantity( availableQtd );
				lst.put(batchQuantity, qtd);
			}
		}
		
		return lst;
	}
	

	/**
	 * Calculate the quantity of the medicine from the source x unit to be removed based on the 
	 * movements prepared to be removed.
	 * @param dt
	 * @param med
	 * @param source
	 * @param unit
	 * @return
	 */
	protected ReturnedValue calcQuantityToBeReturned(Date dt, Medicine med, Source source, Tbunit unit) {
		if (movementsToBeRemoved == null)
			return null;

		int qtd = 0;
		float price = 0;

		for (Movement mov: movementsToBeRemoved) {
			if ((!mov.getDate().after(dt)) &&
				(mov.getMedicine().equals(med)) &&
				(mov.getSource().equals(source)) &&
				(mov.getTbunit().equals(unit))) {
				qtd += mov.getQuantity() * (-mov.getOper());
				price += mov.getTotalPrice() * (-mov.getOper());
			}
		}
		return new ReturnedValue(qtd, price);
	}


	/**
	 * Calculate the quantity that will be returned to an specific batch because of a prepared movement to be removed
	 * @param dt
	 * @param batchQtd
	 * @return
	 */
	protected int calcQuantityToBeReturnedToBatch(BatchQuantity batchQtd) {
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
		private StockPosition stockPosition;
		private Map<BatchQuantity, Integer> batchQuantities;
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
		 * @return the stockPosition
		 */
		public StockPosition getStockPosition() {
			return stockPosition;
		}
		/**
		 * @param stockPosition the stockPosition to set
		 */
		public void setStockPosition(StockPosition stockPosition) {
			this.stockPosition = stockPosition;
		}
		/**
		 * @return the batchQuantities
		 */
		public Map<BatchQuantity, Integer> getBatchQuantities() {
			return batchQuantities;
		}
		/**
		 * @param batchQuantities the batchQuantities to set
		 */
		public void setBatchQuantities(Map<BatchQuantity, Integer> batchQuantities) {
			this.batchQuantities = batchQuantities;
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

		public ReturnedValue(int quantity, float price) {
			super();
			this.quantity = quantity;
			this.price = price;
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
	}


	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
