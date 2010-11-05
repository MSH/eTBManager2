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


	
	/**
	 * Add a prescribed medicine to the case
	 * @param pm
	 */
	public void addPrescribedMedicine(PrescribedMedicine pm) {
		removePeriod(pm.getPeriod(), pm.getMedicine());
		TbCase tbcase = caseHome.getInstance();
		tbcase.getPrescribedMedicines().add(pm);
		pm.setTbcase(tbcase);
	}
	
	/**
	 * Remove a prescribed medicine from the treatment
	 * @param pm
	 */
	public void deleteMedicine(PrescribedMedicine pm) {
		TbCase tbcase = caseHome.getInstance();
		tbcase.getPrescribedMedicines().remove(pm);

		if (entityManager.contains(pm))
			entityManager.remove(pm);
	}


	/**
	 * Update dates of the medicine prescribed to prevent that the prescription doesn't overlap other periods with the same medicine
	 * @param pm
	 */
	protected void removePeriod(Period period, Medicine medicine) {
		TbCase tbcase = caseHome.getInstance();
		
		List<PrescribedMedicine> lst = createPrescribedMedicineList(medicine);
		for (PrescribedMedicine aux: lst) {
			// prescription is inside new/edited one?
			if (period.contains(aux.getPeriod())) {
				tbcase.getPrescribedMedicines().remove(aux);
				if (entityManager.contains(aux))
					entityManager.remove(aux);
			}
			else
			// prescription is containing the whole new/edit prescription
			if (aux.getPeriod().contains(period)) {	
				PrescribedMedicine aux2 = clonePrescribedMedicine(aux);
				aux.getPeriod().setEndDate( DateUtils.incDays(period.getIniDate(), -1) );
				aux2.getPeriod().setIniDate( DateUtils.incDays(period.getEndDate(), 1) );
				tbcase.getPrescribedMedicines().add(aux2);
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
		
		if ((tbcase.getTreatmentPeriod() != null) && (!tbcase.getTreatmentPeriod().isEmpty()) && (tbcase.getTreatmentPeriod().getEndDate().before(period.getEndDate())))
			tbcase.getTreatmentPeriod().setEndDate(period.getEndDate());
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
	 * Adjust medicines of the phase according to the new period
	 * @param phase
	 * @param newperiod
	 */
	public void adjustPhasePeriod(RegimenPhase phase, Period newperiod) {
		TbCase tbcase = caseHome.getInstance();
		
		Period p = (RegimenPhase.INTENSIVE.equals(phase) ? tbcase.getIntensivePhasePeriod(): tbcase.getContinuousPhasePeriod());
		
		if (p == null)
			throw new RuntimeException("Null period for case phase " + phase);
		
		// just for security, split the period between intensive and continuous phases
		if (RegimenPhase.INTENSIVE.equals(phase))
			 splitPeriod( DateUtils.incDays(p.getEndDate(), 1));
		else splitPeriod( p.getIniDate() );
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if (pm.getPeriod().isInside(p)) {
				// calc number of days period is ahead phase
				int days = DateUtils.daysBetween(p.getIniDate(), pm.getPeriod().getIniDate());

				// calc new initial date
				Date dt = DateUtils.incDays(newperiod.getIniDate(), days);
				
				// move period
				pm.getPeriod().movePeriod( dt );

				// cut period if end date is after
				pm.getPeriod().intersect(newperiod);
			}
		}
	}


	/**
	 * Create a clone of the prescribed medicine object
	 * @param pm
	 * @return
	 */
	private PrescribedMedicine clonePrescribedMedicine(PrescribedMedicine pm) {
		PrescribedMedicine aux = new PrescribedMedicine();
		aux.setDoseUnit(pm.getDoseUnit());
		aux.getPeriod().setEndDate(pm.getPeriod().getEndDate());
		aux.getPeriod().setIniDate(pm.getPeriod().getIniDate());
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

}
