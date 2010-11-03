package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.MedicineRegimen;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.tb.RegimensQuery;
import org.msh.tb.cases.CaseHome;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

@Name("caseRegimenHome")
@Scope(ScopeType.CONVERSATION)
public class CaseRegimenHome {

	@In(required=true) CaseHome caseHome;
	@In EntityManager entityManager;
	@In(create=true) RegimensQuery regimens;
	@In(create=true) PrescribedMedicineHome prescribedMedicineHome;

	private Regimen regimen;
	
	/**
	 * Return the regimens available according to the case classification 
	 */
	private List<Regimen> availableRegimens;
	
	/**
	 * If there is already a regimen under treatment, indicates if the previous regimen will be preserved (period outside the new regimen period) 
	 */
	private boolean erasePreviousRegimen;
	
	/**
	 * If true, the default dose unit defined in the regimen will be used when creating the prescribed medicines of the case
	 */
	private boolean useDefaultDoseUnit;
	
	/**
	 * Store information about intensive and continuous phases of the treatment
	 */
	private List<TreatmentPhase> phases;
	
	
	/**
	 * Apply a regimen to a case. If the case has already a regimen specified, the property preserveOuterPeriods will be
	 * used to indicate if any outer period of treatment will be preserved or erased
	 * @param iniDate initial date of the regimen to be applied to the case
	 * @param endDate final date of the regimen. If not informed, the end of the regimen will be considered
	 * @param useDefaultDoses if true, the default doses specified in the regimen will be used
	 */
	public void applyRegimen(Date iniDate, Date endDate, boolean useDefaultDoses) {
		if (regimen == null)
			throw new RuntimeException("Regimen was not defined");
		
		if (erasePreviousRegimen)
			erasePrescribedMedicines();
		
		int monthsIntPhase = regimen.getMonthsPhase(RegimenPhase.INTENSIVE);
		
		Period p = new Period();
		p.setIniDate(iniDate);
		p.setEndDate(endDate);
		
		for (MedicineRegimen mr: regimen.getMedicines()) {
			int num = (RegimenPhase.INTENSIVE.equals(mr.getPhase()) ? 0: monthsIntPhase); 
			Date dt = DateUtils.incMonths(iniDate, num);

			// check if it's inside the period
			if ((endDate == null) || ((endDate != null) && (dt.before(endDate)))) {
				PrescribedMedicine pm = findPrescribedMedicine(mr.getMedicine(), mr.getPhase());

				if (pm == null)
					pm = newPrescribedMedicineFromRegimen(mr);

				pm.getPeriod().setIniDate(dt);
				
				dt = DateUtils.incMonths(dt, mr.getMonthsTreatment());
				dt = DateUtils.incDays(dt, -1);
				
				if (endDate != null)
					dt = (endDate.before(dt) ? endDate: dt);
				
				pm.getPeriod().setEndDate( dt );
				prescribedMedicineHome.addPrescribedMedicine(pm);
				
				if ((p.getEndDate() == null) || (p.getEndDate().before(dt)))
					p.setEndDate(dt);
			}
		}
				
		TbCase tbcase = caseHome.getInstance();
		tbcase.setTreatmentPeriod(p);
		tbcase.setIniContinuousPhase( DateUtils.incMonths(p.getIniDate(), monthsIntPhase ));
	}

	
	/**
	 * Refresh the list of phases and its prescribed medicines
	 */
	public void refresh() {
		phases = null;
	}


	/**
	 * Return the list of available regimens depending on the case classification
	 * @return
	 */
	public List<Regimen> getAvailableRegimens() {
		if (availableRegimens == null) {
			availableRegimens = new ArrayList<Regimen>();
			
			CaseClassification classif = caseHome.getInstance().getClassification();
			if (classif != null) {
				for (Regimen reg: regimens.getResultList())
					if (classif.equals(reg.getCaseClassification()))
						availableRegimens.add(reg);
			}
		}
		return availableRegimens;
	}

	
	/**
	 * Erase all prescribed medicines
	 */
	protected void erasePrescribedMedicines() {
		TbCase tbcase = caseHome.getInstance();
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if (entityManager.contains(pm))
				entityManager.remove(pm);
		}
		tbcase.getPrescribedMedicines().clear();
	}


	/**
	 * Search for a prescribed medicine of the regimen
	 * @param medicine
	 * @param phase
	 * @return
	 */
	protected PrescribedMedicine findPrescribedMedicine(Medicine medicine, RegimenPhase phase) {
		if (phases == null)
			return null;
		for (TreatmentPhase tp: phases) {
			if (tp.getPhase().equals(phase)) {
				for (PrescribedMedicine pm: tp.getMedicines()) {
					if (pm.getMedicine().equals(medicine))
						return pm;
				}
			}
		}
		return null;
	}


	/**
	 * Create the phases of a regimen for editing
	 */
	protected void createPhases() {
		if (regimen == null) {
			phases = null;
			return;
		}
		
		phases = new ArrayList<TreatmentPhase>();

		TreatmentPhase p = new TreatmentPhase();
		p.setMedicines( createPrescribedMedicineList(regimen.getIntensivePhaseMedicines()) );
		p.setPhase(RegimenPhase.INTENSIVE);
		p.setMonths(regimen.getMonthsPhase(RegimenPhase.INTENSIVE));
		phases.add(p);
		
		p = new TreatmentPhase();
		p.setMedicines( createPrescribedMedicineList(regimen.getContinuousPhaseMedicines()) );
		p.setPhase(RegimenPhase.CONTINUOUS);
		p.setMonths(regimen.getMonthsPhase(RegimenPhase.CONTINUOUS));
		phases.add(p);
	}


	/**
	 * Create list of prescribed medicines according to the list of {@link MedicineRegimen} objects
	 * @param lst
	 * @return
	 */
	protected List<PrescribedMedicine> createPrescribedMedicineList(List<MedicineRegimen> lst) {
		List<PrescribedMedicine> meds = new ArrayList<PrescribedMedicine>();
		for (MedicineRegimen mr: lst) {
			PrescribedMedicine pm = new PrescribedMedicine();
			pm.setMedicine(mr.getMedicine());
			pm.setSource(mr.getDefaultSource());
			pm.setFrequency(mr.getDefaultFrequency());
			if (useDefaultDoseUnit)
				pm.setDoseUnit(mr.getDefaultDoseUnit());

			meds.add(pm);
		}
		return meds;
	}

	
	/**
	 * Create a new prescribed medicine from a medicine regimen
	 * @param mr
	 * @return
	 */
	protected PrescribedMedicine newPrescribedMedicineFromRegimen(MedicineRegimen mr) {
		PrescribedMedicine pm = new PrescribedMedicine();
		pm.setMedicine(mr.getMedicine());
		pm.setSource(mr.getDefaultSource());
		pm.setFrequency(mr.getDefaultFrequency());
		if (useDefaultDoseUnit)
			pm.setDoseUnit(mr.getDefaultDoseUnit());
		return pm;
	}
	
	
	/**
	 * Return the phases of the treatment regimen
	 * @return
	 */
	public List<TreatmentPhase> getPhases() {
		if (phases == null)
			createPhases();
		return phases;
	}


	
	/**
	 * Store temporary information about a treatment phase
	 * @author Ricardo Memoria
	 *
	 */
	public class TreatmentPhase {
		private List<PrescribedMedicine> medicines;
		private int months;
		private RegimenPhase phase;
		
		public List<PrescribedMedicine> getMedicines() {
			return medicines;
		}
		public void setMedicines(List<PrescribedMedicine> medicines) {
			this.medicines = medicines;
		}
		public int getMonths() {
			return months;
		}
		public void setMonths(int months) {
			this.months = months;
		}
		/**
		 * @return the phase
		 */
		public RegimenPhase getPhase() {
			return phase;
		}
		/**
		 * @param phase the phase to set
		 */
		public void setPhase(RegimenPhase phase) {
			this.phase = phase;
		}
	}



	/**
	 * @return the regimen
	 */
	public Regimen getRegimen() {
		return regimen;
	}


	/**
	 * @param regimen the regimen to set
	 */
	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}


	/**
	 * @return the useDefaultDoseUnit
	 */
	public boolean isUseDefaultDoseUnit() {
		return useDefaultDoseUnit;
	}


	/**
	 * @param useDefaultDoseUnit the useDefaultDoseUnit to set
	 */
	public void setUseDefaultDoseUnit(boolean useDefaultDoseUnit) {
		this.useDefaultDoseUnit = useDefaultDoseUnit;
	}


	/**
	 * @return the erasePreviousRegimen
	 */
	public boolean isErasePreviousRegimen() {
		return erasePreviousRegimen;
	}


	/**
	 * @param erasePreviousRegimen the erasePreviousRegimen to set
	 */
	public void setErasePreviousRegimen(boolean erasePreviousRegimen) {
		this.erasePreviousRegimen = erasePreviousRegimen;
	}
}
