package org.msh.tb.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.Container;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.MedicineManStartHome;
import org.msh.tb.medicines.MedicineManStartHome.BatchInfo;
import org.msh.tb.medicines.MedicineManStartHome.MedicineInfo;
import org.msh.tb.medicines.MedicineManStartHome.SourceInfo;
import org.msh.tb.medicines.movs.MovementException;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.utils.date.DateUtils;

/**
 * Execute test about medicine movements under several different situations
 * @author Ricardo Memoria
 *
 */
@Name("movementHomeTest")
public class MovementHomeTest {

	@In(create=true) MovementHome movementHome;
	@In(create=true) MedicineManStartHome medicineManStartHome;
	@In(create=true) UserSession userSession;
	@In(create=true) FacesMessages facesMessages;
	@In EntityManager entityManager;

	private Medicine medicine;
	private Source source;
	private Tbunit unit;
	
	private Batch b1;
	private Batch b2;


	/**
	 * Execute test
	 */
	@Transactional
	public void execute() {
		try {
			executeInternally();
			facesMessages.add("Test movements successfully generated...");		
		} catch (MovementException e) {
			facesMessages.add(e.getMessage());
		}
	}

	
	protected void executeInternally() {
		// delete all medicine movements of the selected unit
		medicineManStartHome.cancelMedicineManagement();

		// initialize variables
		unit = userSession.getTbunit();
		unit.getHealthSystem().getName();
		unit.getAdminUnit().getParents();

		// STOCK INITIALIZATION
		initializeManagement();

		// RECEIVING
		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		batches.put(b1, 700);
		batches.put(b2, 500);
		movementHome.initMovementRecording();
		prepareMovement(DateUtils.newDate(2011, 1, 1), MovementType.DRUGRECEIVING, batches);
		movementHome.savePreparedMovements();
		entityManager.flush();

		// DISPENSING
		batches.clear();
		batches.put(b1, 900);
		batches.put(b2, 800);
		movementHome.initMovementRecording();
		Movement movDisp = prepareMovement(DateUtils.newDate(2011, 1, 1), MovementType.DISPENSING, batches);
		movementHome.savePreparedMovements();
		entityManager.flush();

		// ORDER RECEIVING
		batches.clear();
		batches.put(b1, 200);
		batches.put(b2, 200);
		movementHome.initMovementRecording();
		prepareMovement(DateUtils.newDate(2011, 1, 1), MovementType.ORDERRECEIVING, batches);
		movementHome.savePreparedMovements();
		entityManager.flush();
		
		// change RECEIVING MOVEMENT
/*		batches.clear();
		batches.put(b1, 700);
		batches.put(b2, 500);
		movementHome.initMovementRecording();
		movementHome.prepareMovementsToRemove(movRec);
		prepareMovement(DateUtils.newDate(2011, 1, 1), MovementType.DRUGRECEIVING, batches);
		movementHome.savePreparedMovements();
		entityManager.flush();
*/		
		// change DISPENSING MOVEMENT
		batches.clear();
		batches.put(b1, 900);
		batches.put(b2, 800);
		movementHome.initMovementRecording();
		movementHome.prepareMovementsToRemove(movDisp);
		prepareMovement(DateUtils.newDate(2011, 1, 5), MovementType.DISPENSING, batches);
		movementHome.savePreparedMovements();
		entityManager.flush();

//		System.out.println("receiving = " + movRec.getQuantity());
//		System.out.println("dispensing = " + movDisp.getQuantity());
	}
	
	/**
	 * Include TB Unit in the medicine module management
	 */
	protected void initializeManagement() {
		medicineManStartHome.initialize();
		List<SourceInfo> lst = medicineManStartHome.getSourcesInfo();
		SourceInfo info = lst.get(0);
		source = info.getSource();
		
		MedicineInfo medInfo = info.getItems().get(0);
		medicine = medInfo.getMedicine();

		createBatches();
		
		medicineManStartHome.setStartDate(DateUtils.newDate(2010, 12, 1));
		
		medicineManStartHome.startNewBatch(medInfo);
		BatchInfo bq = medicineManStartHome.getBatchInfo();
		bq.setBatch(b1);
		bq.setQuantity(b1.getQuantityReceived());
		medicineManStartHome.finishBatchEditing();

		medicineManStartHome.startNewBatch(medInfo);
		bq = medicineManStartHome.getBatchInfo();
		bq.setBatch(b2);
		bq.setQuantity(b2.getQuantityReceived());
		medicineManStartHome.finishBatchEditing();
		
		medicineManStartHome.startMedicineManagement();
		
		unit.getHealthSystem();
		unit.getAdminUnit().getParents();
	}


	/**
	 * Prepare a new movement to be created
	 * @param dt
	 * @param movType
	 * @param batches
	 * @return
	 */
	protected Movement prepareMovement(Date dt, MovementType movType, Map<Batch, Integer> batches) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("*** PREPARING NEW MOVEMENT ***");
		System.out.println("  Type = " + Messages.instance().get( movType.getKey() ) + ", Date = " + DateUtils.formatDate(dt, "dd-MMM-yyyy"));
		System.out.println("  Batches");
		for (Batch bq: batches.keySet()) {
			System.out.println("  batch=" + bq.getBatchNumber() + ", " + batches.get(bq));
		}
		
		return movementHome.prepareNewMovement(dt, unit, source, medicine, movType, batches, null);
	}


	/**
	 * Create batches for the test
	 */
	protected void createBatches() {
		b1 = new Batch();
		b1.setBatchNumber("1234");
		b1.setBrandName("MED-1");
		b1.setContainer(Container.BOX);
		b1.setExpiryDate(DateUtils.newDate(2012, 6, 1));
		b1.setManufacturer("MSH LAB");
		b1.setMedicine(medicine);
		b1.setQuantityReceived(200);
		b1.setQuantityContainer(200);
		b1.setTotalPrice(1000F);
		b1.setUnitPrice( b1.getTotalPrice() / b1.getQuantityReceived());
//		entityManager.persist(b1);
		
		b2 = new Batch();
		b2.setBatchNumber("ABCD");
		b2.setBrandName("MED-2");
		b2.setContainer(Container.BOX);
		b2.setExpiryDate(DateUtils.newDate(2012, 1, 1));
		b2.setManufacturer("RMEMORIA LAB");
		b2.setMedicine(medicine);
		b2.setQuantityReceived(300);
		b2.setQuantityContainer(300);
		b2.setTotalPrice(1200F);
		b2.setUnitPrice( b2.getTotalPrice() / b2.getQuantityReceived());
//		entityManager.persist(b2);
		
/*		BatchQuantity qtd = new BatchQuantity();
		qtd.setBatch(b1);
		qtd.setQuantity(b1.getQuantityReceived());
		qtd.setSource(source);
		qtd.setTbunit(unit);
		entityManager.persist(qtd);
		
		qtd = new BatchQuantity();
		qtd.setBatch(b2);
		qtd.setQuantity(b2.getQuantityReceived());
		qtd.setSource(source);
		qtd.setTbunit(unit);
		entityManager.persist(qtd);
		
*/
//		entityManager.flush();
	}
	
}
