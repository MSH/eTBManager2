package org.msh.tb.cases.treatment;

 import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.tb.cases.CaseHome;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Handle medicine prescribed to a patient
 * @author Ricardo Memoria
 *
 */
@Name("prescribedMedicineHome")
public class PrescribedMedicineHome {
	private static final long serialVersionUID = 4665192156115818462L;

	@In(required=true) CaseHome caseHome;
	@In(create=true) CaseRegimenHome caseRegimenHome;
	@In EntityManager entityManager;


	private List<PrescribedMedicine> deletedMedicines;
	
	/**
	 * Add a prescribed medicine to the case
	 * @param pm
	 */
	public void addPrescribedMedicine(PrescribedMedicine pm) {
		removePeriod(pm.getPeriod(), pm.getMedicine(), null);
		TbCase tbcase = caseHome.getInstance();
		tbcase.getPrescribedMedicines().add(pm);
		pm.setTbcase(tbcase);
		
		if (tbcase.getIniContinuousPhase() != null)
			splitPeriod(tbcase.getIniContinuousPhase());
	}
	

	/**
	 * Update dates of the medicine prescribed to prevent that the prescription doesn't overlap other periods with the same medicine
	 * @param period Period to be removed from the prescription
	 * @param medicine Medicine to be removed. If null, all medicines of the period will be removed
	 * @param pmException PrescribedMedicine that will be not deleted from the entityManager
	 */
	protected boolean removePeriod(Period period, Medicine medicine, PrescribedMedicine pmException) {
		TbCase tbcase = caseHome.getInstance();
		boolean removed = false;
		
		initializeMedicinesToBeDeleted();
		
		List<PrescribedMedicine> lst = createPrescribedMedicineList(medicine);
		for (PrescribedMedicine aux: lst) {
			// prescription is inside new/edited one?
			if (period.contains(aux.getPeriod())) {
				tbcase.getPrescribedMedicines().remove(aux);
				if ((entityManager.contains(aux)) && (aux != pmException))
					entityManager.remove(aux);
				removed = true;
			}
			else
			// prescription is containing the whole new/edit prescription
			if (aux.getPeriod().contains(period)) {	
				PrescribedMedicine aux2 = clonePrescribedMedicine(aux);
				aux.getPeriod().setEndDate( DateUtils.incDays(period.getIniDate(), -1) );
				if (aux.getPeriod().getDays() <= 1)
					addMedicineToBeDeleted(aux);
				
				aux2.getPeriod().setIniDate( DateUtils.incDays(period.getEndDate(), 1) );
				if (aux2.getPeriod().getDays() > 1)
					tbcase.getPrescribedMedicines().add(aux2);
				removed = true;
			}
			else 
			if (period.isDateInside(aux.getPeriod().getIniDate())) {
				aux.getPeriod().cutIni( DateUtils.incDays(period.getEndDate(), 1) );
			}
			else
			if (period.isDateInside(aux.getPeriod().getEndDate())) {
				aux.getPeriod().cutEnd( DateUtils.incDays(period.getIniDate(), -1) );
			}
		}

		commitDeleteMedicines();
//		if ((tbcase.getTreatmentPeriod() != null) && (!tbcase.getTreatmentPeriod().isEmpty()) && (tbcase.getTreatmentPeriod().getEndDate().before(period.getEndDate())))
//			tbcase.getTreatmentPeriod().setEndDate(period.getEndDate());
		return removed;
	}

	
	/**
	 * Split the prescribed medicines periods in two based on the date parameters 
	 * @param dt
	 */
	public void splitPeriod(Date dt) {
		TbCase tbcase = caseHome.getInstance();

		List<PrescribedMedicine> lstnew = new ArrayList<PrescribedMedicine>();
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if ((pm.getPeriod().isDateInside(dt)) && (!pm.getPeriod().getIniDate().equals(dt))) {
				PrescribedMedicine aux = clonePrescribedMedicine(pm);
				
				pm.getPeriod().setEndDate( DateUtils.incDays(dt, -1) );
				aux.getPeriod().setIniDate( dt );
				
				// add to a temporary list not to interfere with the main loop
				lstnew.add(aux);
			}
		}
		
		for (PrescribedMedicine pm: lstnew)
			tbcase.getPrescribedMedicines().add(pm);
	}


	/**
	 * Join two periods that were split in a date dt
	 * @param dt date to join two adjacent periods
	 */
	public void joinPeriods(Date dt) {
		TbCase tbcase = caseHome.getInstance();
		
		initializeMedicinesToBeDeleted();
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if (pm.getPeriod().getIniDate().equals(dt)) {
				PrescribedMedicine aux = findCompactibleLeftAdjacentPeriod(pm);
				if (aux != null) {
					pm.getPeriod().setIniDate(aux.getPeriod().getIniDate());
					addMedicineToBeDeleted(aux);
				}
			}
		}
		commitDeleteMedicines();
	}


	/**
	 * Find a compatible left adjacent prescribed medicine of the prescribed medicine pm 
	 * @param pm
	 * @return
	 */
	private PrescribedMedicine findCompactibleLeftAdjacentPeriod(PrescribedMedicine pm) {
		TbCase tbcase = caseHome.getInstance();
		Date dt = DateUtils.incDays( pm.getPeriod().getIniDate(), -1);
		for (PrescribedMedicine aux: tbcase.getPrescribedMedicines()) {
			if ((aux.getPeriod().getEndDate().equals(dt)) && (aux.getMedicine().equals(pm.getMedicine())) &&
			   (aux.getSource().equals(pm.getSource())) &&
			   (aux.getDoseUnit() == pm.getDoseUnit()) && (aux.getFrequency() == pm.getFrequency()))
			{
				return aux;
			}
		}
		return null;
	}
	
	
	/**
	 * Adjust medicines of the phase according to the new period
	 * @param phase
	 * @param newperiod
	 */
	public void adjustPhasePeriod(RegimenPhase phase, Period newperiod) {
		TbCase tbcase = caseHome.getInstance();
		
		Period p = (RegimenPhase.INTENSIVE.equals(phase) ? tbcase.getIntensivePhasePeriod(): tbcase.getContinuousPhasePeriod());
		
		if (p == null)
			throw new RuntimeException("Null period for case phase " + phase);

		initializeMedicinesToBeDeleted();
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if (pm.getPeriod().isInside(p)) {
				// indicate if end date of medicine is at end date of period
				boolean fixedEndDate = pm.getPeriod().getEndDate().equals(p.getEndDate());
				
				// calc number of days period is ahead phase
				int days = DateUtils.daysBetween(p.getIniDate(), pm.getPeriod().getIniDate());

				// calc new initial date
				Date dt = DateUtils.incDays(newperiod.getIniDate(), days);
				
				// move period
				pm.getPeriod().movePeriod( dt );
				
				if (fixedEndDate)
					pm.getPeriod().setEndDate( newperiod.getEndDate() );

				// cut period if end date is after
				pm.getPeriod().intersect(newperiod);
				
				if (pm.getPeriod().getDays() <= 1)
					addMedicineToBeDeleted(pm);
			}
		}

		// remove periods that are out of the new adjustment
		commitDeleteMedicines();
	}


	/**
	 * Create a clone of the prescribed medicine object
	 * @param pm
	 * @return
	 */
	public PrescribedMedicine clonePrescribedMedicine(PrescribedMedicine pm) {
		PrescribedMedicine aux = new PrescribedMedicine();
		aux.setDoseUnit(pm.getDoseUnit());
		aux.setPeriod( new Period(pm.getPeriod()) );
		aux.setFrequency(pm.getFrequency());
		aux.setMedicine(pm.getMedicine());
		aux.setSource(pm.getSource());
		aux.setTbcase(pm.getTbcase());
		return aux;
	}

	
	/**
	 * Generate a list of prescribed medicines of a specific medicine for the current case
	 * @param med
	 * @return
	 */
	protected List<PrescribedMedicine> createPrescribedMedicineList(Medicine med) {
		List<PrescribedMedicine> lst = new ArrayList<PrescribedMedicine>();
		TbCase tbcase = caseHome.getInstance();
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if ((med == null) || (pm.getMedicine().equals(med)))
				lst.add(pm);
		}
		return lst;
	}

	
	/**
	 * Add objects {@link PrescribedMedicine} to be deleted
	 * @param pm
	 */
	private void addMedicineToBeDeleted(PrescribedMedicine pm) {
		if (deletedMedicines == null)
			deletedMedicines = new ArrayList<PrescribedMedicine>();
		deletedMedicines.add(pm);
	}

	
	private void initializeMedicinesToBeDeleted() {
		deletedMedicines = null;
	}

	
	/**
	 * Delete objects PrescribedMedicines in the temporary list
	 * @param pm
	 */
	private void commitDeleteMedicines() {
		if (deletedMedicines == null)
			return;

		TbCase tbcase = caseHome.getInstance();
		
		for (PrescribedMedicine pm: deletedMedicines) {
			tbcase.getPrescribedMedicines().remove(pm);
			if (entityManager.contains(pm))
				entityManager.remove(pm);
		}
		
		deletedMedicines = null;
	}
}
