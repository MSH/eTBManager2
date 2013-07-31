package org.msh.tb.medicines.movs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;
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
	private List<Tbunit> unitAmcList;
	
	private String errorMessage;
	private Batch errorBatch;


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
			errorBatch = e.getBatch();
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
		if ((movementsToBeRemoved == null) && (preparedMovements == null))
			return;

		// remove previous movement
		if (movementsToBeRemoved != null) {
			for (Movement mov: movementsToBeRemoved) {
				StockPosition sp = findStockPosition(mov.getTbunit(), mov.getSource(), mov.getMedicine(), mov.getDate());
				sp.setQuantity( sp.getQuantity() - (mov.getQuantity() * mov.getOper()) );
				sp.setTotalPrice( sp.getTotalPrice() - (mov.getTotalPrice() * mov.getOper()));
				
				for (BatchMovement bm: mov.getBatches()) {
					BatchQuantity bq = findBatchQuantity(mov.getTbunit(), mov.getSource(), bm.getBatch());
					bq.setQuantity( bq.getQuantity() - (bm.getQuantity() * mov.getOper()) );
				}

				checkAMCUpdate(mov);
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

			// save the new movement
			mov.setRecordDate(new Date());
			checkAMCUpdate(mov);
			entityManager.persist(mov);

			StockPosition sp = findStockPosition(mov.getTbunit(), mov.getSource(), mov.getMedicine(), mov.getDate());
			sp.setQuantity( sp.getQuantity() + mov.getQtdOperation());
			sp.setTotalPrice( sp.getTotalPrice() + (mov.getTotalPrice() * mov.getOper()) );
			if ((sp.getLastMovement() == null) || (sp.getLastMovement().before(mov.getDate())))
				sp.setLastMovement(mov.getDate());
			entityManager.persist(sp);
		}

		// save medicine quantities
		for (StockPosition sp: stockPositions) {
			// remove stock quantity if it's 0 (just keep the movements)
			if (sp.getQuantity() == 0) {
				if (entityManager.contains(sp))
					entityManager.remove(sp);
			}
			else entityManager.persist(sp);
		}

		// save batches quantities
		for (BatchQuantity bq: batchQuantities) {
//			if (bq.getQuantity() == 0) {
//				if (entityManager.contains(bq))
//					entityManager.remove(bq);
//			}
//			else 
			entityManager.persist(bq);
		}
		
		entityManager.flush();
		entityManager.createQuery("delete BatchQuantity where quantity=0").executeUpdate();

		// update average monthly consumption
		updateAMC();

		preparedMovements = null;
	}



	/**
	 * Find {@link StockPosition} instance by its unit, source and medicine
	 * @param unit
	 * @param source
	 * @param medicine
	 * @return
	 */
	protected StockPosition findStockPosition(Tbunit unit, Source source, Medicine medicine, Date movDate) {
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
		sp.setLastMovement(movDate);
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
		if (batchQuantities == null)
			batchQuantities = new ArrayList<BatchQuantity>();

		for (BatchQuantity bq: batchQuantities) {
			if ((bq.getTbunit().equals(unit)) && (bq.getSource().equals(source)) && (bq.getBatch().equals(batch)))
				return bq;
		}

		BatchQuantity bq = loadBatchQuantity(batch, unit, source);

		batchQuantities.add(bq);
	
		return bq;
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
			String s = Messages.instance().get("meds.movs.datebefore");
			s = MessageFormat.format(s, unit.getMedManStartDate());
			throw new MovementException(s);
		}

		// checks if the unit has a date limit to create movements
		if(!UserSession.isCanGenerateMovements(date, unit))
			throw new MovementException("Date must be after " + DateUtils.formatAsLocale(unit.getLimitDateMedicineMovement(), false) + ".");
	
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

		if (qtd == 0){
			throw new MovementException("No movement to be executed because the quantity is 0");
		}
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
		mov.setQuantity(qtd);
		mov.setTotalPrice(totalPrice);

		pm.setMovement(mov);
		pm.setBatches(batches);

		if (preparedMovements == null)
			preparedMovements = new ArrayList<PreparedMovement>();
		preparedMovements.add(pm);
		return pm;
	}

	
	/**
	 * Test if batch quantity can be reduced from a specific date. If quantity cannot be reduced, a {@link MovementException} will be rose with
	 * descriptions about the error 
	 * @param batch
	 * @param qtd negative quantity to be reduced
	 * @param date
	 * @param source
	 * @param unit
	 */
	protected void testBathQuantityReducing(Batch batch, int qtd, Date date, Source source, Tbunit unit) {
		if (qtd >= 0)
			return;
		
		// execute quantity validation
		// verify if, in case of reduction (i.e, dispensing/transfer/etc) if the quantity will be negative from the date to the future 
		String hql = "select m.date from BatchMovement a join a.movement m " +
				"where (select sum(b.quantity*b.movement.oper) + :qtd from BatchMovement b " +
				"join b.movement m2 " +
				"where b.batch.id=a.batch.id and m2.tbunit.id=m.tbunit.id and m2.source.id=m.source.id " +
				"and ((m2.date < m.date) or (m2.date = m.date and m2.recordDate <= m.recordDate))) < 0 " +
				"and a.batch.id = :batch and m.tbunit.id=:unit and m.source.id=:source " +
				"and (m.date > :dt or m.date = (select max(c.movement.date) from BatchMovement c " +
				"where c.batch.id = a.batch.id and c.movement.tbunit.id=m.tbunit.id and c.movement.source.id=m.source.id and c.movement.date <= :dt))";

		List<Date> lst = entityManager.createQuery(hql)
			.setParameter("qtd", (long)qtd)
			.setParameter("batch", batch.getId())
			.setParameter("source", source.getId())
			.setParameter("unit", unit.getId())
			.setParameter("dt", date)
			.getResultList();

		if (lst.size() > 0)
			throw new MovementException(batch, MessageFormat.format(Messages.instance().get("MovementException.NEGATIVE_STOCK"), batch.getBatchNumber(), LocaleDateConverter.getDisplayDate(date, false)));
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
	protected ReturnedValue calcQuantityToBeReturned(Date dt, Medicine med, Source source, Tbunit unit, Batch batch) {
		if (movementsToBeRemoved == null)
			return new ReturnedValue(0, new Date());

		int qtd = 0;
//		float price = 0;
		Date dtrem = null;

		for (Movement mov: movementsToBeRemoved) {
			if ((!mov.getDate().after(dt)) &&
				(mov.getMedicine().equals(med)) &&
				(mov.getSource().equals(source)) &&
				(mov.getTbunit().equals(unit))) 
			{
				for (BatchMovement bm: mov.getBatches()) {
					if (bm.getBatch().equals(batch)) {
						qtd += bm.getQuantity() * (-mov.getOper());
//						price += bm.getTotalPrice() * (-mov.getOper());
						if ((dtrem == null) || (dtrem.after(mov.getDate())))
							dtrem = mov.getDate();
					}
				}
			}
		}
		return new ReturnedValue(qtd, dtrem);
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
	 * Check if AMC of the unit related to the movement must be update. If it must be updated,
	 * the unit is kept in memory and AMC is updated later when calling <code>updateAMC()</code> method
	 * @param mov
	 */
	protected void checkAMCUpdate(Movement mov) {
		if (mov.getType() != MovementType.DISPENSING)
			return;
		
		if (unitAmcList == null)
			unitAmcList = new ArrayList<Tbunit>();
		
		if (!unitAmcList.contains(mov.getTbunit()))
			unitAmcList.add(mov.getTbunit());
	}
	
	
	/**
	 * Update the AMC of the units. The list of units was created by calling the method <code>checkAMCUpdate()</code> 
	 */
	protected void updateAMC() {
		if (unitAmcList == null)
			return;
		
		for (Tbunit unit: unitAmcList) {
			updateUnitAMC(unit);
		}
	}


	/**
	 * Update the average monthly consumption, from the dispensing registration
	 */
	protected void updateUnitAMC(Tbunit unit) {
		StockPositionList posList = (StockPositionList)Component.getInstance("stockPositionList", true);
		List<StockPosition> splist = posList.generate(unit, null);
		
		String hql = "select month(m.date), m.source.id, m.medicine.id, sum(m.quantity) " +
				"from Movement m " +
				"where m.date >= :dt and m.tbunit.id=:unit " +
				"and m.type = :type " +
				"group by month(m.date), m.source.id, m.medicine.id";

		// get 6 months behind
		Date dt = DateUtils.getDate();
//		int currentMonth = DateUtils.monthOf(dt);
//		int currentDay = DateUtils.dayOf(dt);
		dt = DateUtils.incMonths(dt, -6);
		dt = DateUtils.newDate(DateUtils.yearOf(dt), DateUtils.monthOf(dt), 1);
		
		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("unit", unit.getId())
			.setParameter("type", MovementType.DISPENSING)
			.setParameter("dt", dt)
			.getResultList();
		
		for (StockPosition sp: splist) {
			Map<Integer, Integer> months = new HashMap<Integer, Integer>(); 

			// mount list of months with monthly consumption
			for (Object[] vals: lst) {
				Integer sourceid = (Integer)vals[1];
				Integer medicineid = (Integer)vals[2];
				if ((sp.getSource().getId().equals(sourceid)) && (sp.getMedicine().getId().equals(medicineid))) {
					Integer month = (Integer)vals[0];
					Long qtd = (Long)vals[3];
					if (qtd != null)
						months.put(month, qtd.intValue());
				}
			}

			int numMonths = months.size();
			float amc = 0;
			// calculate average monthly consumption
			for (Integer month: months.keySet()) {
				int qtd = months.get(month);
				amc += qtd/numMonths; 
			}
			
			sp.setAmc( Math.round(amc) );
			entityManager.persist(sp);
		}
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

	public Movement prepareNewAdjustment(Date date, Tbunit unit, Source source, Medicine medicine, 
			Map<Batch, Integer> batches, FieldValueComponent adjustReason){
		
		Movement m = prepareNewMovement(date, unit, source, medicine, MovementType.ADJUSTMENT, batches, (adjustReason == null ? null : adjustReason.getComplement()));
		
		if(m!=null)
			m.setAdjustmentType((adjustReason == null ? null : adjustReason.getValue()));
		
		return m;
	}
	/**
	 * Returned value from removed movements
	 * @author Ricardo Memoria
	 *
	 */
	private class ReturnedValue {
		private int quantity;
		private Date date;

		public ReturnedValue(int quantity, Date date) {
			super();
			this.quantity = quantity;
			this.date = date;
		}
		/**
		 * @return the quantity
		 */
		public int getQuantity() {
			return quantity;
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


	/**
	 * @return the errorBatch
	 */
	public Batch getErrorBatch() {
		return errorBatch;
	}
}
