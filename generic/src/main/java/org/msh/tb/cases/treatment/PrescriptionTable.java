package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Generate a data model to be used by UI to display the prescription table of a case
 * @author Ricardo Memoria
 *
 */
@Name("prescriptionTable")
@Scope(ScopeType.CONVERSATION)
public class PrescriptionTable {

	@In(required=true) CaseHome caseHome;

	/**
	 * Store the current period of treatment, considering transferring of health unit
	 */
	private Period period;

	/**
	 * Treatment period of the intensive phase
	 */
	private Period intensivePhasePeriod;
	
	/**
	 * Treatment period of the continuous phase
	 */
	private Period continuousPhasePeriod;
	
	/**
	 * List of medicines prescribed to the patient during the treatment
	 */
	private List<MedicineInfo> medicines;
	
	/**
	 * Store the number of days of treatment 
	 */
	private int numDaysTreatment;

	
	/**
	 * Indicate if the period will be displayed for editing, in this case, just the period
	 * of the current treatment health unit will be displayed
	 */
	private boolean editing;

	private TbCase tbcase;
	
	private List<TreatmentPeriod> healthUnits;
	private List<TreatmentPeriod> phases;


	/**
	 * Calculate the treatment period
	 */
	protected boolean calculateTreatmentPeriod() {
		healthUnits = new ArrayList<TreatmentPeriod>();

		// is just for displaying
		if (!isEditing()) {
			period = tbcase.getTreatmentPeriod();
			intensivePhasePeriod = tbcase.getIntensivePhasePeriod();
			continuousPhasePeriod = tbcase.getContinuousPhasePeriod();
			
			if ((period == null) || (period.isEmpty()))
				return false;
			
			for (TreatmentHealthUnit hu: tbcase.getHealthUnits())
				healthUnits.add( new TreatmentPeriod(this, hu, hu.getPeriod()) );
			
			numDaysTreatment = (period != null ? period.getDays() : 0);
			updateListValues(healthUnits);
	
			return true;
		}

		// get the treatment health unit being edited
		TreatmentHealthUnit item = null;
		for (TreatmentHealthUnit hu: tbcase.getHealthUnits()) {
			if (!hu.isTransferring()) {
				if ((item == null) || (item.getPeriod().isBefore( hu.getPeriod() ))) {
					item = hu;
				}
			}
		}
		
		if (item == null)
			return false;

		period = item.getPeriod();
		healthUnits.add( new TreatmentPeriod(this, item, item.getPeriod()) );
		
		numDaysTreatment = period.getDays();
		updateListValues(healthUnits);
		
		if (tbcase.getIntensivePhasePeriod() != null) {
			intensivePhasePeriod = new Period(period);
			intensivePhasePeriod.intersect( tbcase.getIntensivePhasePeriod() );
		}
		
		if (tbcase.getContinuousPhasePeriod() != null) {
			continuousPhasePeriod = new Period(period);
			continuousPhasePeriod.intersect( tbcase.getContinuousPhasePeriod() );
		}
		return true;
	}


	/**
	 * Update table values
	 */
	protected void createTable() {
		medicines = new ArrayList<MedicineInfo>();
		tbcase = caseHome.getInstance();

		// calculate the treatment period
		if (!calculateTreatmentPeriod())
			return;

		for (PrescribedMedicine pm: tbcase.getSortedPrescribedMedicines()) {
			MedicineInfo medInfo = findMedicine(pm.getMedicine());
			if (pm.getPeriod().isIntersected(period))
				medInfo.addMedicinePrescribed(pm);
		}
		
		for (MedicineInfo medInfo: getMedicines()) {
			updateListValues(medInfo.getPrescriptions());
		}
		
		createPhases();
	}


	/**
	 * Create intensive and continuous phase of the treatment
	 */
	public void createPhases() {
		phases = new ArrayList<TreatmentPeriod>();
		
		if (intensivePhasePeriod != null)
			phases.add( new TreatmentPeriod(this, RegimenPhase.INTENSIVE, intensivePhasePeriod) );
		
		if (continuousPhasePeriod != null)
			phases.add( new TreatmentPeriod(this, RegimenPhase.CONTINUOUS, continuousPhasePeriod) );
		
		updateListValues(phases);
	}
	
	
	/**
	 * Check if the case was transferred from a health unit to another
	 * @return true if was transferred
	 */
	public boolean isTransferredCase() {
		TbCase tbcase = caseHome.getInstance();
		int num = tbcase.getHealthUnits().size();
		return (tbcase.isOpen()) && (num > 1) && (!tbcase.getHealthUnits().get(num - 1).isTransferring());
	}

	
	/**
	 * Return the last treatment health unit of the case
	 * @return
	 */
	public TreatmentHealthUnit getLastHealthUnit() {
		List<TreatmentHealthUnit> lst = caseHome.getInstance().getHealthUnits();
		return (lst.size() > 0? lst.get(lst.size() - 1) : null);
	}
		
	
	/**
	 * Force the table to be reconstructed next time it'll be used
	 */
	public void refresh() {
		period = null;
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
	 * Calculate position of the bar based on the give date
	 * @param dt
	 * @return
	 */
	private int calcBarPosition(Date dt) {
		if (dt.after(period.getEndDate()))
			return 100;
		
		int days = DateUtils.daysBetween(period.getIniDate(), dt);
		
		int pos = Math.round((days / (float)numDaysTreatment * 100f) - 0.5f);
		
		return pos;
	}


	/**
	 * Update values of a list of treatment periods
	 * @param lst
	 */
	protected void updateListValues(List<TreatmentPeriod> lst) {
		sortPeriodList(lst);

		int lastpos = 0;
		
		for (TreatmentPeriod p: lst) {
			int left = calcBarPosition(p.getIniDate());
			int right = calcBarPosition( DateUtils.incDays(p.getEndDate(), 1) );
			
			int width = right - left;
			
			left -= lastpos;
			lastpos = right;

			p.setLeft(left);
			p.setWidth(width);
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


	/**
	 * @return the intensivePhasePeriod
	 */
	public Period getIntensivePhasePeriod() {
		return intensivePhasePeriod;
	}


	/**
	 * @return the continuousPhasePeriod
	 */
	public Period getContinuousPhasePeriod() {
		return continuousPhasePeriod;
	}


	/**
	 * @return the editing
	 */
	public boolean isEditing() {
		return editing;
	}


	/**
	 * @param editing the editing to set
	 */
	public void setEditing(boolean editing) {
		this.editing = editing;
	}
	
	
	/**
	 * Return true if prescription table is valid, i.e, if the treatment period is ok
	 * @return
	 */
	public boolean isValid() {
		if (period == null)
			createTable();
		return ((period != null) && (!period.isEmpty()) && (healthUnits.size() > 0));
	}
}
