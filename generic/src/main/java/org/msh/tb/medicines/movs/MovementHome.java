package org.msh.tb.medicines.movs;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.MovementType;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.LocaleDateConverter;

import javax.persistence.EntityManager;
import java.text.MessageFormat;
import java.util.*;

/**
 * Operations to modify the available quantity of medicines and batches and handle medicine
 * and batch movements. The two types of medicine transactions available are: create a new movement
 * and remove an existing movement
 *
 * @author Ricardo Memoria
 */
@Name("movementHome")
public class MovementHome {

    @In(create=true)
    private EntityManager entityManager;

    private List<PreparedMovement> preparedMovements;
    private List<Movement> movementsToBeRemoved;
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
     * @param date the date that movement will be recorded
     * @param unit instance of Unit representing the health facility
     * @param source instance of Source representing the medicine source
     * @param medicine instance of the {@link Medicine} representing the medicine to generate the movement to
     * @param type the movement type {@link org.msh.tb.entities.enums.MovementType}
     * @param batches map containing the batches and its quantities to be used in the movement
     * @param comment an optional comment to be attached to the movement
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
    @Transactional(TransactionPropagationType.REQUIRED)
    public void savePreparedMovements() {
        if ((movementsToBeRemoved == null) && (preparedMovements == null))
            return;

        // remove previous movement
        if (movementsToBeRemoved != null) {
            for (Movement mov: movementsToBeRemoved) {
                removeMovement(mov);
                checkAMCUpdate(mov);
            }
            movementsToBeRemoved = null;
        }


        if (preparedMovements != null) {
            for (PreparedMovement pm: preparedMovements) {
                Movement mov = pm.getMovement();

                // create batch movements
                for (Batch batch: pm.getBatches().keySet()) {
                    int batchqtd = pm.getBatches().get(batch);

                    BatchMovement bm = new BatchMovement();
                    bm.setBatch(batch);
                    bm.setQuantity(batchqtd);
                    bm.setMovement(mov);

                    // get available quantity of the batch
                    BatchMovement lastBatchMov = getLastBatchMovement(mov.getDate(),
                            batch,
                            mov.getTbunit(),
                            mov.getSource());
                    int batchAvailableQtd = lastBatchMov != null? lastBatchMov.getAvailableQuantity(): 0;
                    // update the new batch quantity
                    batchAvailableQtd += bm.getQuantity() * mov.getOper();
                    bm.setAvailableQuantity( batchAvailableQtd );

                    mov.getBatches().add(bm);
                }

                // get stock position at the given movement
                Movement prevMov = getLastMovementAt(mov.getDate(), mov.getMedicine(), mov.getTbunit(), mov.getSource());
                int availableQuantity = prevMov != null? prevMov.getAvailableQuantity(): 0;
                float totalPriceInventory = prevMov != null? prevMov.getTotalPriceInventory(): 0;

                // update quantity and price
                availableQuantity += mov.getQuantity() * mov.getOper();
                totalPriceInventory += mov.getTotalPrice() * mov.getOper();

                // se the new quantity and price
                mov.setAvailableQuantity(availableQuantity);
                mov.setTotalPriceInventory(totalPriceInventory);
                mov.setRecordDate(new Date());

                // register a new movement and update stock quantity
                addMovement(mov);
            }
        }

        // update average monthly consumption
        updateAMC();

        preparedMovements = null;
    }


    /**
     * Return the movement containing the available quantity of the given medicine in stock
     * for the given health unit and source
     * @param date the date to calculate the available quantity
     * @param medicine instance of the {@link Medicine} representing the medicine
     * @param unit instance of Unit representing the health facility
     * @param source instance of Source representing the medicine source
     * @return instance of {@link org.msh.tb.entities.Movement}
     */
    protected Movement getLastMovementAt(Date date, Medicine medicine, Tbunit unit, Source source) {
        String hql = "from Movement m " +
                "where m.date = (select max(m2.date) from Movement m2 " +
                "where m2.tbunit.id=m.tbunit.id " +
                "and m2.source.id=m.source.id and m2.medicine.id=m.medicine.id and m2.date <= :dt) " +
                "and m.source.id=" + source.getId() + " and m.tbunit.id=" + unit.getId() +
                " and m.medicine.id=" + medicine.getId() +
                " order by m.recordDate desc";

        List<Movement> lst = entityManager.createQuery(hql)
                .setParameter("dt", date)
                .getResultList();

        if (lst.size() == 0) {
            return null;
        }

        return lst.get(0);
    }


    /**
     * Return the movement containing the available quantity of the given batch in stock for the
     * health unit and source
     * @param date the date to calculate the available quantity
     * @param batch instance of the {@link org.msh.tb.entities.Batch} representing the batch
     * @param unit instance of Unit representing the health facility
     * @param source instance of Source representing the medicine source
     * @return instance of {@link org.msh.tb.entities.Movement}
     */
    protected BatchMovement getLastBatchMovement(Date date, Batch batch, Tbunit unit, Source source) {
        String hql = "from BatchMovement bm join fetch bm.movement m " +
                "where m.date = (select max(m2.date) from BatchMovement bm2 join bm2.movement m2 " +
                "where m2.tbunit.id=m.tbunit.id " +
                "and m2.source.id=m.source.id and bm2.batch.id=bm.batch.id and m2.date <= :dt) " +
                "and m.source.id=" + source.getId() + " and m.tbunit.id=" + unit.getId() +
                " and bm.batch.id=" + batch.getId() +
                " order by m.recordDate desc";

        List<BatchMovement> lst = entityManager.createQuery(hql)
                .setParameter("dt", date)
                .getResultList();

        if (lst.size() == 0) {
            return null;
        }

        return lst.get(0);
    }



    /**
     * Create a new prepared movement. In case any problem with the new movement is detected, a {@link MovementException}
     * will be generated
     * @param date is the date of the movement
     * @param unit instance of Unit representing the health facility
     * @param source instance of Source representing the medicine source
     * @param medicine instance of the {@link Medicine} representing the medicine
     * @param type is the movement type
     * @param batches map containing the batches and its quantities to be used in the movement
     * @param comment an optional comment to be attached to the movement
     * @return instance of {@link org.msh.tb.medicines.movs.MovementHome.PreparedMovement} class
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
            // check if batch medicine is the same used in movement
            if (!b.getMedicine().getId().equals(medicine.getId())) {
                throw new MovementException("Batch doesn't belong to medicine: batch = " + b.getBatchNumber() + " and medicine = " + medicine.toString());
            }

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
     * Test if batch can be reduced to the given quantity (must be negative value indicating a reduction).
     * If it's not enough, the system will raise a MovementException
     * @param batch instance of {#link Batch} representing the batch
     * @param quantity the quantity to reduce the inventory
     * @param date the date to test if quantity can be reduced
     * @param source instance of Source representing the medicine source
     * @param unit instance of Unit representing the health facility
     */
    protected void testBathQuantityReducing(Batch batch, int quantity, Date date, Source source, Tbunit unit) {
        if (quantity >= 0) {
            return;
        }

        Integer minQuantity = calcBatchMinQuantity(batch, date, source, unit);

        if ((minQuantity == null) || (minQuantity + quantity < 0)) {
            throw new MovementException(batch, MessageFormat.format(Messages.instance().get("MovementException.NEGATIVE_STOCK"), batch.getBatchNumber(), LocaleDateConverter.getDisplayDate(date, false)));
        }
    }


    /**
     * Return the minimal quantity that the batch can be reduced in a specific date
     * based on the available quantity of a health unit for a given source
     * @param batch instance of Batch representing the batch
     * @param date the reference date (present or past) to check from that
     * @param source the medicine source
     * @param unit the health facility
     * @return the minimal quantity allowed
     */
    protected Integer calcBatchMinQuantity(Batch batch, Date date, Source source, Tbunit unit) {
        String sql = "select min(bm.availableQuantity) from BatchMovement bm " +
                "inner join Movement m on m.id=bm.movement_id " +
                "where m.unit_id = " + unit.getId() +
                " and m.source_id = " + source.getId() +
                " and bm.batch_id = " + batch.getId();

        // get the batch movement before the date
        BatchMovement bm = getLastBatchMovement(date, batch, unit, source);
        if (bm == null) {
            return 0;
        }

        sql += " and (m.mov_date > :dt or bm.id = " + bm.getId() + ")";

        Integer minQuantity = (Integer)entityManager.createNativeQuery(sql)
                .setParameter("dt", date)
                .getSingleResult();

        return minQuantity;
    }


    /**
     * Calculate the quantity of the medicine from the source x unit to be removed based on the
     * movements prepared to be removed.
     * @param dt the reference date to calculate from
     * @param med instance of the {@link org.msh.tb.entities.Medicine} representing the medicine
     * @param source instance of Source representing the medicine source
     * @param unit instance of Unit representing the health facility
     * @return the quantity to be removed
     */
    protected ReturnedValue calcQuantityToBeReturned(Date dt, Medicine med, Source source, Tbunit unit, Batch batch) {
        if (movementsToBeRemoved == null)
            return new ReturnedValue(0, new Date());

        int qtd = 0;
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
                        if ((dtrem == null) || (dtrem.after(mov.getDate())))
                            dtrem = mov.getDate();
                    }
                }
            }
        }
        return new ReturnedValue(qtd, dtrem);
    }




    /**
     * Check if AMC of the unit related to the movement must be update. If it must be updated,
     * the unit is kept in memory and AMC is updated later when calling <code>updateAMC()</code> method
     * @param mov instance of {@link Movement} to have AMC updated
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
     * Remove a movement and update the available quantity
     * @param mov instance of {@link Movement} to be removed from the system
     */
    private void removeMovement(Movement mov) {
        // update stock quantity
        updateAvailableQuantity(mov, false);

        // remove medicine movement
        entityManager.remove(mov);
        // remove batch movement
        for (BatchMovement bm: mov.getBatches()) {
            entityManager.remove(bm);
        }
        entityManager.flush();
    }


    /**
     * Add a new movement and update the available quantity
     * @param mov instance of {@link Movement} to be removed from the system
     */
    private void addMovement(Movement mov) {
        updateAvailableQuantity(mov, true);

        entityManager.persist(mov);
        for (BatchMovement bm: mov.getBatches()) {
            entityManager.persist(bm);
        }

        entityManager.flush();
    }


    /**
     * Update the available quantity of future movements from the given movement by quantity
     * (or decrease if it's a negative quantity). The given movement is not affected, just
     * movements after the given one, i.e, with date after the movement or same date but
     * with bigger record date
     * @param mov the movement to get the source, unit, medicine, date and batches
     * @param increase the quantity to be increased (or decreased, if a negative number)
     */
    private void updateAvailableQuantity(Movement mov, boolean increase) {
        int signal = (increase? 1: -1) * mov.getOper();

        int quantity = signal * mov.getQuantity();

        System.out.println("Update Mov: medicine=(" + mov.getMedicine().getId() + ") " + mov.getMedicine().toString() + " += " + quantity);
        entityManager.createQuery("update Movement set availableQuantity = availableQuantity + :qtd, " +
                "totalPriceInventory = totalPriceInventory + :price " +
                "where tbunit.id = :unitid and source.id = :sourceid and medicine.id = :medid " +
                "and (date > :dt or (date = :dt and recordDate > :recordDate))")
                .setParameter("unitid", mov.getTbunit().getId())
                .setParameter("sourceid", mov.getSource().getId())
                .setParameter("medid", mov.getMedicine().getId())
                .setParameter("dt", mov.getDate())
                .setParameter("recordDate", mov.getRecordDate())
                .setParameter("qtd", quantity)
                .setParameter("price", mov.getTotalPrice() * signal)
                .executeUpdate();
        updateMedicineQuantity(mov.getTbunit(), mov.getSource(), mov.getMedicine(), quantity,
                mov.getTotalPrice() * signal, mov.getDate());

        // update batches
        for (BatchMovement bm: mov.getBatches()) {
            int batchQuantity = signal * bm.getQuantity();
//            entityManager.remove(bm);
            System.out.println("   # batch=(" + bm.getBatch().getId() + ") " + bm.getBatch().getBatchNumber() + " += " + batchQuantity);
            entityManager.createQuery("update BatchMovement set availableQuantity = availableQuantity + :qtd " +
                    "where movement.id in (select id from Movement where tbunit.id = :unitid and source.id = :sourceid " +
                    "and medicine.id = :medid and date > :dt or (date = :dt and recordDate > :recordDate)) " +
                    "and batch.id = :batchid ")
                    .setParameter("unitid", mov.getTbunit().getId())
                    .setParameter("sourceid", mov.getSource().getId())
                    .setParameter("batchid", bm.getBatch().getId())
                    .setParameter("medid", mov.getMedicine().getId())
                    .setParameter("dt", mov.getDate())
                    .setParameter("recordDate", mov.getRecordDate())
                    .setParameter("qtd", batchQuantity)
                    .executeUpdate();
            updateBatchQuantity(mov.getTbunit(), mov.getSource(), bm.getBatch(), batchQuantity);
        }
    }

    /**
     * Update the available quantity of medicine in a specific health unit for a given medicine source
     * @param unit instance of Unit representing the health facility
     * @param source instance of Source representing the medicine source
     * @param med instance of the {@link org.msh.tb.entities.Medicine} representing the medicine
     * @param quantity the quantity to be increased (or decreased, if a negative number)
     * @param movDate date of the reference movement that is updating the medicine quantity
     * @param price the price to increase or decrease
     */
    private void updateMedicineQuantity(Tbunit unit, Source source, Medicine med, int quantity, float price, Date movDate) {
        System.out.println(quantity);
        // find the entity about quantity available
        Number count = (Number)entityManager.createQuery("select count(id) from StockPosition where tbunit.id = :unitid " +
                "and source.id = :sourceid and medicine.id = :medid")
                .setParameter("unitid", unit.getId())
                .setParameter("sourceid", source.getId())
                .setParameter("medid", med.getId())
                .getSingleResult();

        boolean existStockPos = count.intValue() > 0;
//        StockPosition sp = lst.size() > 0? lst.get(0): null;

        // no stock position information?
        if (!existStockPos) {
            // so create one
            StockPosition sp = new StockPosition();
            sp.setTbunit(unit);
            sp.setSource(source);
            sp.setMedicine(med);
            sp.setQuantity(quantity);
            sp.setTotalPrice(price);
            sp.setLastMovement(movDate);
            entityManager.persist(sp);
        }
        else {
            // update stock position record
            entityManager.createQuery("update StockPosition set quantity = quantity + :qtd, " +
                    "totalPrice = totalPrice + :price, " +
                    "lastMovement = :movdate " +
                    "where tbunit.id=:unitid and source.id=:sourceid and medicine.id=:medid")
                    .setParameter("qtd", quantity)
                    .setParameter("movdate", movDate)
                    .setParameter("price", price)
                    .setParameter("unitid", unit.getId())
                    .setParameter("sourceid", source.getId())
                    .setParameter("medid", med.getId())
                    .executeUpdate();
/*
            sp.setQuantity( sp.getQuantity() + quantity );
            sp.setTotalPrice( sp.getTotalPrice() + price );
            if (sp.getLastMovement().before(movDate)) {
                sp.setLastMovement(movDate);
            }
*/
        }
    }

    /**
     * Update available quantity of a batch for a given health unit and medicine source
     * @param unit instance of Unit representing the health facility
     * @param source instance of Source representing the medicine source
     * @param batch instance of {#link Batch} representing the batch
     * @param quantity the quantity to be increased (or decreased, if a negative number)
     */
    private void updateBatchQuantity(Tbunit unit, Source source, Batch batch, int quantity) {
        Number count = (Number)entityManager.createQuery("select count(id) from BatchQuantity where batch.id = :batchid " +
                "and tbunit.id = :unitid and source.id = :sourceid")
                .setParameter("batchid", batch.getId())
                .setParameter("sourceid", source.getId())
                .setParameter("unitid", unit.getId())
                .getSingleResult();

        boolean recordExists = count.intValue() > 0;

        if (!recordExists) {
            BatchQuantity bq = new BatchQuantity();
            bq.setTbunit(unit);
            bq.setSource(source);
            bq.setBatch(batch);
            bq.setQuantity(quantity);
            entityManager.persist(bq);
        }
        else {
            // update batch quantity value
            entityManager.createQuery("update BatchQuantity set quantity = quantity + :qtd " +
                    "where batch.id = :batchid " +
                    "and tbunit.id = :unitid and source.id = :sourceid")
                    .setParameter("qtd", quantity)
                    .setParameter("batchid", batch.getId())
                    .setParameter("sourceid", source.getId())
                    .setParameter("unitid", unit.getId())
                    .executeUpdate();
        }
/*
        List<BatchQuantity> lst = entityManager.createQuery("from BatchQuantity where batch.id = :batchid " +
                "and tbunit.id = :unitid and source.id = :sourceid")
                .setParameter("batchid", batch.getId())
                .setParameter("sourceid", source.getId())
                .setParameter("unitid", unit.getId())
                .getResultList();

        BatchQuantity bq = lst.size() > 0? lst.get(0) : null;
        if (bq == null) {
            bq = new BatchQuantity();
            bq.setTbunit(unit);
            bq.setSource(source);
            bq.setBatch(batch);
            bq.setQuantity(quantity);
            entityManager.persist(bq);
        }
        else {
            bq.setQuantity( bq.getQuantity() + quantity );

            if (bq.getQuantity() == 0) {
                entityManager.remove(bq);
            }
            else {
                entityManager.persist(bq);
            }
        }
*/
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

        Movement m = prepareNewMovement(date, unit, source, medicine, MovementType.ADJUSTMENT, batches, adjustReason.getComplement());

        if(m!=null)
            m.setAdjustmentType(adjustReason.getValue());

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
         * @return the date
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