package org.msh.tb.ua;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.application.App;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.MedicineReceivingHome;
import org.msh.tb.medicines.SourceMedicineTree;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.ua.entities.MedicineReceivingUA;



@Name("medicineReceivingUAHome")
@LogInfo(roleName="RECEIV")
public class MedicineReceivingUAHome extends EntityHomeEx<MedicineReceivingUA> {
	private static final long serialVersionUID = 28538032481597899L;

	@In(required=false) UserSession userSession;
	@In(create=true) MovementHome movementHome;
	@In(create=true) FacesMessages facesMessages;
	
	@Factory("medicineReceivingUA")
	public MedicineReceivingUA getMedicineReceiving() {
		/*MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
		if (getInstance()!=null)
		if (getInstance().getId()==null && mrh.getInstance()!=null){
			MedicineReceivingUA ins = (MedicineReceivingUA)App.getEntityManager().find(MedicineReceivingUA.class, mrh.getInstance().getId());
			return ins;
		}*/
		MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
		MedicineReceivingUA rec = getInstance();
		//rec.setId(mrh.getInstance().getId());
		rec.clone(mrh.getInstance());
		return rec;
		//return getInstance();
		/*
		if (mrh.getInstance().getId() == null) 
			return (MedicineReceivingUA)super.getInstance();
		MedicineReceivingUA ins = (MedicineReceivingUA)App.getEntityManager().find(MedicineReceivingUA.class, mrh.getInstance().getId());
		if (ins == null){
			ins = (MedicineReceivingUA)super.getInstance();
		}
		return ins;*/
	}

	/**
	 * Valida os dados do recebimento de medicamento
	 * @return
	 */
	/*public boolean validateDrugReceiving() {
		Tbunit unit = userSession.getTbunit();

		if (getInstance().getReceivingDate().before( unit.getMedManStartDate()) ) {
			facesMessages.addFromResourceBundle("meds.movs.datebefore", unit.getMedManStartDate());
			return false;
		}
		MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
		SourceNode sourceNode = mrh.getRoot();
		
		if (sourceNode.getMedicines().size() == 0) {
			facesMessages.addFromResourceBundle("meds.receiving.nobatch");
			return false;
		}
		
		return true;
	}*/

	@Override
	public String persist() {		
		MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
		if (!mrh.validateDrugReceiving())
			return "error";
		
		if (mrh.getInstance().getTbunit() == null)
			mrh.getInstance().setTbunit(userSession.getTbunit());
		
		MedicineReceivingUA rec = getInstance();
		rec.clone(mrh.getInstance());

		// if movements can't be saved (because it may be an editing and the quantity was changed to a 
		// quantity that makes the stock negative) the system will just return an error message
		movementHome.initMovementRecording();
		if (!mrh.prepareMovements())
			return "error";
		
		float totalPrice = 0;
		for (Batch b: mrh.getSourceTree().getItems())
				totalPrice += b.getTotalPrice();
		rec.setTotalPrice(totalPrice);

		// register log
		getLogDetailWriter().addTableRow("Source", rec.getSource());
		getLogDetailWriter().addTableRow(".receivingDate", rec.getReceivingDate());
		getLogDetailWriter().addTableRow("global.totalPrice", rec.getTotalPrice());

		if (isManaged())
			getLogService().recordEntityState(getInstance(), Operation.EDIT);

		// save all batches
		mrh.getSourceTree().traverse(new SourceMedicineTree.ItemTraversing<Batch>() {
			public void traverse(Source source, Medicine medicine, Batch item) {
				getEntityManager().persist(item);
			}
		});

		// save all movements and update stock position
		movementHome.savePreparedMovements();
		
		for (Batch batch: mrh.getRemBatches()) {
			getEntityManager().remove(batch);
		}

		// save receiving
		return super.persist();
	}

	/*public boolean prepareMovements() {
		MedicineReceiving rec = getInstance();
		MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
		//  prepare to remove movements of medicines removed by the user
		for (Movement mov: mrh.getRemMovs()) {
			movementHome.prepareMovementsToRemove(mov);
		}
		
		SourceNode node = mrh.getRoot();
		rec.getMovements().clear();

		// update current movements
		for (Object obj: node.getMedicines()) {
			MedicineNode medNode = (MedicineNode)obj;
			MedicineInfo medInfo = (MedicineInfo)medNode.getItem();

			Movement mov = medInfo.getMovement();

			// remove movements previously saved (if it's an editing of an existing receiving) 
			if (mov != null)
				movementHome.prepareMovementsToRemove(mov);

			// mount batch list
			Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
			for (Object aux: medNode.getBatches()) {
				Batch b = (Batch)aux;
				batches.put(b, b.getQuantityReceived());
			}
			
			// prepare movement to be saved
			mov = movementHome.prepareNewMovement(rec.getReceivingDate(),
					rec.getTbunit(), 
					rec.getSource(), 
					medNode.getMedicine(), 
					MovementType.DRUGRECEIVING, 
					batches, null);

			if (mov == null) {
				facesMessages.add(movementHome.getErrorMessage());
				return false;
			}

			// link the new movement to the item
			rec.getMovements().add(mov);
		}

		return true;
	}*/

	@Override
	public Object getId() {
		Object id = super.getId();
		if (id == null){
			MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
			id = mrh.getId();
		}
		return id;
	}

	/*@Override
	public void setId(Object id) {
		MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
		mrh.setId(id);
		super.setId(id);
	}*/
	
	/*@Override
	public void setInstance(MedicineReceivingUA instance) {
		super.setInstance(instance);
	}*/
	
	/*public String getConsignmentNumber() {
		String num = getInstance().getConsignmentNumber();
		if (num == null){
			MedicineReceivingHome mrh = (MedicineReceivingHome)App.getComponent("medicineReceivingHome");
			if (mrh.getInstance().getId() != null){
				MedicineReceivingUA ua = (MedicineReceivingUA)App.getEntityManager().find(MedicineReceivingUA.class, mrh.getInstance().getId());
				num = ua.getConsignmentNumber();
			}
		}
		return num;
	}*/
}
