package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.CaseRegimen;
import org.msh.mdrtb.entities.MedicineRegimen;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Handle beginning of standard treatment for a case
 * @author Ricardo Memoria
 *
 */
@Name("startTreatmentHome")
@Scope(ScopeType.CONVERSATION)
public class StartTreatmentHome {

	@In(required=true) CaseHome caseHome;
	@In EntityManager entityManager;
	
	private Regimen regimen;
	private TBUnitSelection tbunitselection;
	private Date iniTreatmentDate;
	
	private boolean saveChages = true;
	
	/**
	 * End of the treatment. This field is not required and is used if the case has already an outcome date
	 */
	private Date endTreatmentDate;
	
	/**
	 * Indicate, when creating a standard script, if the default dose unit defined in the regimen will be applied to the case regimen 
	 */
	private boolean useDefaultDoseUnit;
	
	private List<Phase> phases;
	private List<Regimen> regimens;

	
	/**
	 * Start a standard regimen for a case
	 * @return "treatment-started" if successfully started, otherwise return "error"
	 */
	public String startStandardRegimen() { 
		if ((regimen == null) || (getTbunitselection().getTbunit() == null) || (iniTreatmentDate == null))
			return "error";

		if (phases == null)
			createPhases();
		
		TbCase tbcase = caseHome.getInstance();
		
		if (!CaseState.WAITING_TREATMENT.equals(tbcase.getState())) {
			return "error";
		}
		
		// calculate the end of the treatment date
		int numMonthsInt = regimen.getMonthsPhase(RegimenPhase.INTENSIVE); 
		int numMonths = numMonthsInt + regimen.getMonthsPhase(RegimenPhase.CONTINUOUS);
		Date endDate = DateUtils.incDays( DateUtils.incMonths(iniTreatmentDate, numMonths), -1);
		if ((endTreatmentDate != null) && (endTreatmentDate.before(endDate)))
			endDate = endTreatmentDate;
		Date iniContPhase = DateUtils.incMonths(iniTreatmentDate, numMonthsInt);

		// initialize case data
		tbcase.setTreatmentPeriod(new Period(iniTreatmentDate, endDate));
		tbcase.setState(CaseState.ONTREATMENT);

		// initialize treatment health unit
		TreatmentHealthUnit hu = new TreatmentHealthUnit();
		hu.setPeriod(new Period(iniTreatmentDate, endDate));
		hu.setTbCase(tbcase);
		hu.setTbunit(tbunitselection.getTbunit());
		hu.setTransferring(false);
		tbcase.getHealthUnits().clear();
		tbcase.getHealthUnits().add(hu);
		
		// initialize case regimen
		CaseRegimen cr = new CaseRegimen();
		cr.setPeriod(new Period(iniTreatmentDate, endDate));
		cr.setIniContPhase(iniContPhase);
		cr.setRegimen(regimen);
		cr.setTbCase(tbcase);
		tbcase.getRegimens().clear();
		tbcase.getRegimens().add(cr);

		// initialize list of prescribed medicines
		Date dt = iniTreatmentDate;
		Period p = new Period(iniTreatmentDate, endDate);
		for (Phase phase: phases) {
			for (MedicineInfo mi: phase.getMedicines()) {
				PrescribedMedicine pm = mi.getPrescribedMedicine();
				pm.setPeriod(new Period(dt, DateUtils.incDays( DateUtils.incMonths(dt, mi.getMonths()), -1)));

				if (pm.getPeriod().intersect(p)) {
					tbcase.getPrescribedMedicines().add(pm);
					pm.setTbcase(tbcase);
				}
			}
			dt = DateUtils.incMonths(dt, phase.getMonths());
		}

		if (saveChages)
			caseHome.persist();
		
		return "treatment-started";
	}


	/**
	 * Create list of prescribed medicines for the phases of treatment 
	 * according to the regimen selected
	 */
	protected void createPhases() {
		if (regimen == null) {
			phases = null;
			return;
		}
		
		phases = new ArrayList<Phase>();

		Phase p = new Phase();
		p.setMedicines( createPrescribedMedicineList(regimen.getIntensivePhaseMedicines()) );
		p.setMonths(regimen.getMonthsPhase(RegimenPhase.INTENSIVE));
		phases.add(p);
		
		p = new Phase();
		p.setMedicines( createPrescribedMedicineList(regimen.getContinuousPhaseMedicines()) );
		p.setMonths(regimen.getMonthsPhase(RegimenPhase.CONTINUOUS));
		phases.add(p);
	}


	/**
	 * Create list of prescribed medicines according to the list of {@link MedicineRegimen} objects
	 * @param lst
	 * @return
	 */
	protected List<MedicineInfo> createPrescribedMedicineList(List<MedicineRegimen> lst) {
		List<MedicineInfo> meds = new ArrayList<MedicineInfo>();
		for (MedicineRegimen mr: lst) {
			PrescribedMedicine pm = new PrescribedMedicine();
			pm.setMedicine(mr.getMedicine());
			pm.setSource(mr.getDefaultSource());
			pm.setFrequency(mr.getDefaultFrequency());
			if (useDefaultDoseUnit)
				pm.setDoseUnit(mr.getDefaultDoseUnit());
			
			MedicineInfo mi = new MedicineInfo();
			mi.setMonths(mr.getMonthsTreatment());
			mi.setPrescribedMedicine(pm);
			meds.add(mi);
		}
		return meds;
	}


	/**
	 * Return a list of lists of prescribed medicines for intensive and continuous phase
	 * @return
	 */
	public List<Phase> getPhases() {
		if (phases == null)
			createPhases();
		return phases;
	}

	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}

	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null)
			tbunitselection = new TBUnitSelection(true, TBUnitFilter.HEALTH_UNITS);
		return tbunitselection;
	}

	public Date getIniTreatmentDate() {
		return iniTreatmentDate;
	}

	public void setIniTreatmentDate(Date iniTreatmentDate) {
		this.iniTreatmentDate = iniTreatmentDate;
	}


	/**
	 * Update the list of phases and its medicines
	 */
	public void updatePhases() {
		phases = null;
	}

	/**
	 * Return list of regimens available to the case
	 * @return
	 */
	public List<Regimen> getRegimens() {
		if (regimens == null) {
			CaseClassification cl = caseHome.getInstance().getClassification();
			if (cl == null)
				return null;
			
			String cond;
			if (cl == CaseClassification.MDRTB_DOCUMENTED)
				cond = "r.mdrTreatment = :p1";
			else cond = "r.tbTreatment = :p1";
			regimens = entityManager.createQuery("from Regimen r where r.workspace.id = #{defaultWorkspace.id} and " + cond)
						.setParameter("p1", true)
						.getResultList();
		}
		return regimens;
	}



	public Regimen getRegimen() {
		return regimen;
	}
	

	public class MedicineInfo {
		private PrescribedMedicine prescribedMedicine;
		private int months;
		public PrescribedMedicine getPrescribedMedicine() {
			return prescribedMedicine;
		}
		public void setPrescribedMedicine(PrescribedMedicine prescribedMedicine) {
			this.prescribedMedicine = prescribedMedicine;
		}
		public int getMonths() {
			return months;
		}
		public void setMonths(int months) {
			this.months = months;
		}
	}
	
	public class Phase {
		private List<MedicineInfo> medicines;
		private int months;
		
		public List<MedicineInfo> getMedicines() {
			return medicines;
		}
		public void setMedicines(List<MedicineInfo> medicines) {
			this.medicines = medicines;
		}
		public int getMonths() {
			return months;
		}
		public void setMonths(int months) {
			this.months = months;
		}
	}

	public boolean isUseDefaultDoseUnit() {
		return useDefaultDoseUnit;
	}


	public void setUseDefaultDoseUnit(boolean useDefaultDoseUnit) {
		this.useDefaultDoseUnit = useDefaultDoseUnit;
	}


	public Date getEndTreatmentDate() {
		return endTreatmentDate;
	}


	public void setEndTreatmentDate(Date endTreatmentDate) {
		this.endTreatmentDate = endTreatmentDate;
	}


	public boolean isSaveChages() {
		return saveChages;
	}


	public void setSaveChages(boolean saveChages) {
		this.saveChages = saveChages;
	}

}
