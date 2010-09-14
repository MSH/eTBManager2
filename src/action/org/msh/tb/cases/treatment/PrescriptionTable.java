package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.CaseRegimen;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.tb.cases.CaseHome;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Generate a data model to be used by UI to display the prescription table of a case
 * @author Ricardo Memoria
 *
 */
@Name("prescriptionTable")
public class PrescriptionTable {

	@In(required=true) CaseHome caseHome;

	/**
	 * Store the current period of treatment, considering transfering of health unit
	 */
	private Period period;
	
	/**
	 * List of medicines prescribed to the patient during the treatment
	 */
	private List<MedicineInfo> medicines;
	
	/**
	 * Store the number of days of treatment 
	 */
	private int numDaysTreatment;

	private TbCase tbcase;
	
	private List<TreatmentPeriod> regimens;
	private List<TreatmentPeriod> healthUnits;
	private List<TreatmentPeriod> phases;


	/**
	 * Update table values
	 */
	protected void createTable() {
		medicines = new ArrayList<MedicineInfo>();
		tbcase = caseHome.getInstance();
		period = new Period();

		// calculate the treatment period
		for (TreatmentHealthUnit unit: getTbcase().getHealthUnits()) {
			if (!unit.isTransferring()) {
				Period p = unit.getPeriod();
				if ((period.getIniDate() == null) || (period.getIniDate().after(p.getIniDate())))
					period.setIniDate(p.getIniDate());
				if ((period.getEndDate() == null) || (period.getEndDate().before(p.getEndDate())))
					period.setEndDate(p.getEndDate());
			}			
		}
		
		if ((period.isEmpty()) && (tbcase.getTreatmentPeriod() != null)) 
			period.set(tbcase.getTreatmentPeriod());
		numDaysTreatment = period.getDays();
		
		healthUnits = new ArrayList<TreatmentPeriod>();
		for (TreatmentHealthUnit unit: getTbcase().getHealthUnits()) {
			if (!unit.isTransferring()) {
				TreatmentPeriod p = new TreatmentPeriod(this, unit, unit.getPeriod());
				healthUnits.add(p);
			}
		}
		updateListValues(healthUnits);
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			MedicineInfo medInfo = findMedicine(pm.getMedicine());
			if (pm.getPeriod().isIntersected(period))
				medInfo.addMedicinePrescribed(pm);
		}

		regimens = new ArrayList<TreatmentPeriod>();
		for (CaseRegimen r: getTbcase().getRegimens()) {
			// is regimen in the treatment period ?
			if (r.getPeriod().isIntersected(period)) {
				TreatmentPeriod p = new TreatmentPeriod(this, r, r.getPeriod());
				regimens.add(p);
			}
		}
		updateListValues(regimens);
		
		createPhases();
		
		for (MedicineInfo medInfo: getMedicines()) {
			updateListValues(medInfo.getPrescriptions());
		}
	}

	
	/**
	 * Create phases to be displayed in the table
	 */
	protected void createPhases() {
		phases = new ArrayList<TreatmentPeriod>();
		TreatmentPeriod treatPeriod = null;

		for (CaseRegimen r: getTbcase().getRegimens()) {
			RegimenPhase phase;
			Period period;
			if (r.isHasIntensivePhase()) {
				phase = RegimenPhase.INTENSIVE;
				period = r.getPeriodIntPhase();
				treatPeriod = addPhasePeriod(treatPeriod, phase, period);
			}
			
			if (r.isHasContinuousPhase()) {
				phase = RegimenPhase.CONTINUOUS;
				period = r.getPeriodContPhase();
				treatPeriod = addPhasePeriod(treatPeriod, phase, period);
			}
		}
		updateListValues(phases);
	}
	
	
	/**
	 * Add a new period to the list of phases 
	 * @param prevPeriod
	 * @param phase
	 * @param period
	 * @return
	 */
	private TreatmentPeriod addPhasePeriod(TreatmentPeriod prevPeriod, RegimenPhase phase, Period period) {
		if (prevPeriod == null) {
			TreatmentPeriod treatPeriod = new TreatmentPeriod(this, phase, period);
			phases.add(treatPeriod);
			return treatPeriod;
		}
		
		RegimenPhase prevPhase = (RegimenPhase)prevPeriod.getItem();
		
		// previous phase is equals the current phase ?
		if (prevPhase.equals(phase)) {
			prevPeriod.expandEndDate(period.getEndDate());
			return prevPeriod;
		}

		TreatmentPeriod treatPeriod = new TreatmentPeriod(this, phase, period);
		phases.add(treatPeriod);

		return treatPeriod;
	}
	
	
	/**
	 * Force the table to be reconstructed next time it'll be used
	 */
	public void refresh() {
		period = null;
		regimens = null;
		healthUnits = null;
		medicines = null;
	}


	/**
	 * Return TB case
	 * @return
	 */
	public TbCase getTbcase() {
		if (tbcase == null)
			tbcase = caseHome.getInstance();
		return tbcase;
	}


	/**
	 * Update values of a list of treatment periods
	 * @param lst
	 */
	protected void updateListValues(List<TreatmentPeriod> lst) {
		int prevLeft = 0;
		sortPeriodList(lst);
		float numDaysTreat = getNumDaysTreatment();

		Date dtAnt = null;

		for (TreatmentPeriod p: lst) {
			float daysPeriod = p.getPeriod().getDays();
			int width = Math.round(daysPeriod / numDaysTreat * 100);

			int daysLeft = DateUtils.daysBetween(p.getIniDate(), period.getIniDate());
			int left = Math.round(daysLeft / numDaysTreat * 100);
			
			p.setWidth(width);
			
			if ((dtAnt != null) && (DateUtils.daysBetween(dtAnt, p.getPeriod().getIniDate()) == 1)) {
				// make small adjustment in the round operation 
				p.setLeft(0);
				if (left - prevLeft > 0)
					p.setWidth(width + 1);
			}
			else p.setLeft(left - prevLeft);
			
			prevLeft = left + width;
			dtAnt = p.getPeriod().getEndDate();
		}
	}


	/**
	 * Sort the periods of a list
	 * @param lst
	 */
	protected void sortPeriodList(List<TreatmentPeriod> lst) {
		Collections.sort(lst, new Comparator<TreatmentPeriod>() {
			public int compare(TreatmentPeriod p1, TreatmentPeriod p2) {
				return p1.getPeriod().getIniDate().compareTo(p2.getPeriod().getIniDate());
			}
		});
	}
	
	/**
	 * Return the number of days of treatment
	 * @return
	 */
	public int getNumDaysTreatment() {
		return numDaysTreatment;
	}


	/**
	 * Search for information about a medicine
	 * @param med
	 * @return
	 */
	protected MedicineInfo findMedicine(Medicine med) {
		for (MedicineInfo medInfo: medicines) {
			if (medInfo.getMedicine().equals(med))
				return medInfo;
		}
		
		MedicineInfo medInfo = new MedicineInfo();
		medInfo.setTable(this);
		medInfo.setMedicine(med);
		medicines.add(medInfo);
		return medInfo;
	}
	
	public List<MedicineInfo> getMedicines() {
		if (medicines == null)
			createTable();
		return medicines;
	}


	/**
	 * Store information about a medicine prescribed to the patient
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineInfo {
		private Medicine medicine;
		private List<TreatmentPeriod> prescriptions = new ArrayList<TreatmentPeriod>();
		private PrescriptionTable table;

		/**
		 * Add a prescribed medicine period to the list of prescribed periods of the medicine
		 * @param pm
		 * @return
		 */
		public TreatmentPeriod addMedicinePrescribed(PrescribedMedicine pm) {
			TreatmentPeriod info = new TreatmentPeriod(table, pm, pm.getPeriod());
			prescriptions.add(info);
			return info;
		}
		
		public Medicine getMedicine() {
			return medicine;
		}
		public void setMedicine(Medicine medicine) {
			this.medicine = medicine;
		}
		public List<TreatmentPeriod> getPrescriptions() {
			return prescriptions;
		}
		public PrescriptionTable getTable() {
			return table;
		}
		public void setTable(PrescriptionTable table) {
			this.table = table;
		}
	}



	/**
	 * Return the list of regimen periods
	 * @return
	 */
	public List<TreatmentPeriod> getRegimens() {
		if (regimens == null) {
			createTable();
		}
		return regimens;
	}


	/**
	 * return list of health unit periods
	 * @return
	 */
	public List<TreatmentPeriod> getHealthUnits() {
		if (healthUnits == null) {
			createTable();
		}
		return healthUnits;
	}


	public Period getPeriod() {
		if (period == null)
			createTable();
		return period;
	}
	
	
	/**
	 * Return the list of phases
	 * @return
	 */
	public List<TreatmentPeriod> getPhases() {
		if (phases == null)
			createTable();
		return phases;
	}
}
