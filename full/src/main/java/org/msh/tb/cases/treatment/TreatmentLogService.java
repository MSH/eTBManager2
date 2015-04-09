package org.msh.tb.cases.treatment;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.msh.etbm.transactionlog.ActionTX;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.utils.date.LocaleDateConverter;
import org.msh.utils.date.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Register in log the changes in the treatment
 * @author Ricardo Memoria
 *
 */
@Name("treatmentLogService")
@Scope(ScopeType.CONVERSATION)
public class TreatmentLogService {

//	@In(create=true) TransactionLogService transactionLogService;
	@In CaseHome caseHome;

	private Period prevPeriod;
	private Date prevIniContPhase;
	private boolean regimenChanged = false;
	
	private List<PrescribedMedicine> newMedicines;
	private List<PrescribedMedicine> editedMedicines;
	private List<RemovedPediod> removedMedicines;
	
	@Observer("treatment-init-editing")
	public void logInitEditing() {
		TbCase tbcase = caseHome.getInstance();

		prevPeriod = tbcase.getTreatmentPeriod();
		prevIniContPhase = tbcase.getIniContinuousPhase();
	}

	
	/**
	 * Save the changes of the treatment in the transaction log system
	 */
	@Observer("treatment-persist")
	public void logTreatmentPersist() {
//		TransactionLogService log = transactionLogService;

		ActionTX atx = ActionTX.begin("CASE_TREAT");

		TbCase tbcase = caseHome.getInstance();

		// log changes in the treatment period
		Period p = tbcase.getTreatmentPeriod();
		boolean changed = false;

		if (!p.equals(prevPeriod)) {
			atx.addRow("TbCase.iniTreatmentDate", (prevPeriod != null? prevPeriod.getIniDate(): null), p.getIniDate());
			atx.addRow("TbCase.endTreatmentDate", (prevPeriod != null? prevPeriod.getEndDate(): null), p.getEndDate());
			changed = true;
		}
		
		if ((prevIniContPhase == null) || ((prevIniContPhase != null) && (!prevIniContPhase.equals(tbcase.getIniContinuousPhase())))) {
			atx.addRow("RegimenPhase.CONTINUOUS", prevIniContPhase, tbcase.getIniContinuousPhase());
			changed = true;
		}

		// log changes in the regimen
		if (regimenChanged) {
			if (tbcase.getRegimen() == null)
				 atx.getDetailWriter().addMessage("regimens.individualized");
			else atx.addRow("Regimen", tbcase.getRegimen());
			changed = true;
		}

		// log changes of new medicines included
		if (newMedicines != null) {
			for (PrescribedMedicine pm: newMedicines) {
				String s = getDisplayTextPrescribedMed(pm);
				atx.addRow("admin.meds.new", s);
			}
			changed = true;
		}
		
		if (editedMedicines != null) {
			for (PrescribedMedicine pm: editedMedicines)
				atx.addRow("form.edit", getDisplayTextPrescribedMed(pm));
			changed = true;
		}
		
		if (removedMedicines != null) {
			for (RemovedPediod rp: removedMedicines) {
				String s = rp.getMedicine().getAbbrevName() +
					" (" + LocaleDateConverter.getDisplayDate(rp.getPeriod().getIniDate(), false) +
					"..." + LocaleDateConverter.getDisplayDate(rp.getPeriod().getEndDate(), false) + ")";

				atx.addRow("cases.treat.deletedperiod", s);
			}
			changed = true;
		}

		if (changed) {
			atx.setEntityClass( TbCase.class.getSimpleName() )
					.setEntityId( tbcase.getId() )
					.setEntity( tbcase )
					.setRoleAction( RoleAction.EDIT )
					.setDescription( tbcase.toString() )
					.end();
		}
//			log.save("CASE_TREAT", RoleAction.EDIT, tbcase.toString(), tbcase.getId(), TbCase.class.getSimpleName(), tbcase);
	}

	
	/**
	 * Retrieve the new medicine included
	 * @param pm
	 */
	@Observer("treatment-new-medicine")
	public void logNewMedicine(PrescribedMedicine pm) {
		if (newMedicines == null)
			newMedicines = new ArrayList<PrescribedMedicine>();
		
		newMedicines.add(pm);
	}


	/**
	 * Retrieve medicines edited
	 * @param pm
	 */
	@Observer("treatment-edit-medicine")
	public void logMedicineEditing(PrescribedMedicine pm) {
		if ((newMedicines != null) && (newMedicines.contains(pm)))
			return;

		if (editedMedicines == null)
			editedMedicines = new ArrayList<PrescribedMedicine>();

		editedMedicines.add(pm);
	}


	/**
	 * Retrieve period changed
	 * @param med
	 * @param period
	 */
	@Observer("treatment-remove-period")
	public void logRemoveMedicinePeriod(Medicine med, Period period) {
		if (removedMedicines == null)
			removedMedicines = new ArrayList<RemovedPediod>();
		
		RemovedPediod rp = new RemovedPediod(period, med);
		removedMedicines.add(rp);
	}
	

	/**
	 * Retrieve the changes in the regimen and save them in the transaction log system when the changes are committed 
	 */
	@Observer("treatment-regimen-changed")
	public void logRegimenChanged() {
		removedMedicines = null;
		editedMedicines = null;
		newMedicines = null;
		
		regimenChanged = true;
	}


	/**
	 * Register in log that treatment was undone in the system
	 */
	@Observer("treatment-undone")
	public void logTreatmentUndone() {
		TbCase tbcase = caseHome.getInstance();

		ActionTX atx = ActionTX.begin("TREATMENT_UNDO", tbcase, RoleAction.EXEC);

		if (tbcase.getRegimen() == null)
			 atx.getDetailWriter().addMessage("regimens.individualized");
		else atx.addRow("Regimen", tbcase.getRegimen());

		Period p = tbcase.getTreatmentPeriod();
		if (p != null) {
			atx.addRow("TbCase.iniTreatmentDate", p.getIniDate());
			atx.addRow("TbCase.endTreatmentDate", p.getEndDate());
		}
		atx.setEntityClass( TbCase.class.getSimpleName() )
				.end();
//		transactionLogService.saveExecuteTransaction("TREATMENT_UNDO", tbcase.toString(), tbcase.getId(), TbCase.class.getSimpleName(), tbcase);
	}

	
	/**
	 * Save in log the beginning of the treatment
	 */
	@Observer("treatment-started")
	public void logTreatmentStart() {
		TbCase tbcase = caseHome.getInstance();

		ActionTX atx = ActionTX.begin("CASE_STARTTREAT", tbcase, RoleAction.EXEC);

		if (tbcase.getRegimen() == null)
			 atx.getDetailWriter().addMessage("regimens.individualized");
		else atx.addRow("Regimen", tbcase.getRegimen());

		Period p = tbcase.getTreatmentPeriod();
		if (p != null) {
			atx.addRow("TbCase.iniTreatmentDate", p.getIniDate());
			atx.addRow("TbCase.endTreatmentDate", p.getEndDate());
		}

		atx.setEntityClass( TbCase.class.getSimpleName() )
				.end();
//		transactionLogService.saveExecuteTransaction("CASE_STARTTREAT", tbcase.getPatient().getFullName(), tbcase.getId(), TbCase.class.getSimpleName(), tbcase);
	}


	/**
	 * Return the display text of the prescribed medicine
	 * @param pm
	 * @return
	 */
	public String getDisplayTextPrescribedMed(PrescribedMedicine pm) {
		int months = pm.getPeriod().getMonths();
		return Integer.toString(months) + pm.getMedicine().getAbbrevName() + Integer.toString(pm.getDoseUnit()) 
			+ " " + Integer.toString(pm.getFrequency()) + "/7 " + pm.getSource().getAbbrevName().toString(); 
	}


	/**
	 * Information about a period removed
	 * @author Ricardo Memoria
	 *
	 */
	public class RemovedPediod {
		private Period period;
		private Medicine medicine;

		public RemovedPediod(Period period, Medicine medicine) {
			super();
			this.period = period;
			this.medicine = medicine;
		}
		/**
		 * @return the period
		 */
		public Period getPeriod() {
			return period;
		}
		/**
		 * @return the medicine
		 */
		public Medicine getMedicine() {
			return medicine;
		}
	}
}
