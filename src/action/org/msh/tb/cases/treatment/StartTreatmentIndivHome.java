package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.SourcesQuery;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.medicines.MedicineSelection;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

@Name("startTreatmentIndivHome")
@Scope(ScopeType.CONVERSATION)
public class StartTreatmentIndivHome extends StartTreatmentHome {

	@In(create=true) MedicineSelection medicineSelection;

	private List<PrescribedMedicine> medicinesIntPhase = new ArrayList<PrescribedMedicine>();
	private List<PrescribedMedicine> medicinesContPhase = new ArrayList<PrescribedMedicine>();
	private RegimenPhase phase;
	

	/**
	 * Start an individualized treatment
	 * @return "treatment-started" if successfully started
	 */
	public String startIndividualizedRegimen() {
		TbCase tbcase = caseHome.getInstance();

		int monthsIntPhase = 0;
		int monthsContPhase = 0;
		for (PrescribedMedicine pm: medicinesIntPhase) {
			if (pm.getMonths() > monthsIntPhase)
				monthsIntPhase += pm.getMonths();
		}

		for (PrescribedMedicine pm: medicinesContPhase) {
			if (pm.getMonths() > monthsContPhase)
				monthsContPhase += pm.getMonths();
		}
		
		if (monthsContPhase == 0) {
			FacesMessages.instance().addFromResourceBundle("edtrec.nomedicine");
			return "error";
		}
		
		if (monthsIntPhase == 0) {
			FacesMessages.instance().addFromResourceBundle("edtrec.nomedicine");
			return "error";
		}
		
		Date iniTreatmentDate = getIniTreatmentDate();
		Date endTreatmentDate = DateUtils.incMonths(iniTreatmentDate, monthsIntPhase + monthsContPhase);
		endTreatmentDate = DateUtils.incDays(endTreatmentDate, -1);
		
		Period treatPeriod = new Period(iniTreatmentDate, endTreatmentDate);

		// update case
		tbcase.setTreatmentPeriod(treatPeriod);
		tbcase.setState(CaseState.ONTREATMENT);
		tbcase.setRegimen(null);
		tbcase.setIniContinuousPhase( DateUtils.incMonths(iniTreatmentDate, monthsIntPhase) );
		
		// create treatment health unit
		TreatmentHealthUnit hu = new TreatmentHealthUnit();
		hu.setPeriod(new Period(treatPeriod));
		hu.setTbCase(tbcase);
		hu.setTbunit(getTbunitselection().getTbunit());
		hu.setTransferring(false);
		
		tbcase.getHealthUnits().clear();
		tbcase.getHealthUnits().add(hu);
	
		SourcesQuery sources = (SourcesQuery)Component.getInstance("sources");

		// include medicines of the intensive phase
		for (PrescribedMedicine pm: medicinesIntPhase) {
			pm.getPeriod().movePeriod(iniTreatmentDate);
			pm.setTbcase(tbcase);
			if (pm.getSource() == null)
				pm.setSource(sources.getResultList().get(0));
			tbcase.getPrescribedMedicines().add(pm);
		}

		// include medicines of the continuous phase
		for (PrescribedMedicine pm: medicinesContPhase) {
			pm.getPeriod().movePeriod(tbcase.getIniContinuousPhase());
			pm.setTbcase(tbcase);
			if (pm.getSource() == null)
				pm.setSource(sources.getResultList().get(0));
			tbcase.getPrescribedMedicines().add(pm);
		}
		
		if (isSaveChages())
			caseHome.persist();

		return "treatment-started";
		
	}


	/**
	 * Initialize medicine selection dialog box
	 * @param phase
	 */
	public void initMedicineSelection(RegimenPhase phase) {
		List<PrescribedMedicine> meds;
		if (RegimenPhase.CONTINUOUS.equals(phase))
			meds = medicinesContPhase;
		else meds = medicinesIntPhase;

		medicineSelection.clear();
		medicineSelection.applyFilter(meds, "medicine");
		this.phase = phase;
	}

	
	/**
	 * Add new medicines
	 */
	public void addMedicines() {
		List<PrescribedMedicine> meds;
		if (RegimenPhase.CONTINUOUS.equals(phase))
			 meds = medicinesContPhase;
		else meds = medicinesIntPhase;
		
		List<Medicine> lst = medicineSelection.getSelectedMedicines();
		for (Medicine med: lst) {
			PrescribedMedicine pm = new PrescribedMedicine();
			pm.setMedicine(med);
			pm.setIniMonth(1);
			pm.setFrequency(7);
			meds.add(pm);
		}
	}

	
	public void removePrescribedMedicine(PrescribedMedicine pm) {
		if (pm == null)
			return;
		
		medicinesContPhase.remove(pm);
		medicinesIntPhase.remove(pm);
	}


	public List<PrescribedMedicine> getMedicinesIntPhase() {
		return medicinesIntPhase;
	}


	public List<PrescribedMedicine> getMedicinesContPhase() {
		return medicinesContPhase;
	}


	public RegimenPhase getPhase() {
		return phase;
	}
}
