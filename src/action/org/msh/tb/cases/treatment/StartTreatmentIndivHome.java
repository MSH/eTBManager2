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
import org.msh.mdrtb.entities.CaseRegimen;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.tb.SourcesQuery;
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
		
		// create treatment health unit
		TreatmentHealthUnit hu = new TreatmentHealthUnit();
		hu.setPeriod(new Period(treatPeriod));
		hu.setTbCase(tbcase);
		hu.setTbunit(getTbunitselection().getTbunit());
		hu.setTransferring(false);
		
		tbcase.getHealthUnits().clear();
		tbcase.getHealthUnits().add(hu);
		
		// create regimens
		CaseRegimen cr = new CaseRegimen();
		cr.setPeriod(new Period(treatPeriod));
		cr.setIniContPhase( DateUtils.incMonths(iniTreatmentDate, monthsIntPhase));
		cr.setTbCase(tbcase);
		
		tbcase.getRegimens().clear();
		tbcase.getRegimens().add(cr);

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
			pm.getPeriod().movePeriod(cr.getIniContPhase());
			pm.setTbcase(tbcase);
			if (pm.getSource() == null)
				pm.setSource(sources.getResultList().get(0));
			tbcase.getPrescribedMedicines().add(pm);
		}
		
		if (isSaveChages())
			caseHome.persist();

		return "treatment-started";
		
/*		for (CaseRegimen cr: tbcase.getRegimens()) {
			cr.getPeriod().movePeriod(iniTreatmentDate);
			
			// initialize the treatment period
			if ((treatPeriod.getIniDate() == null) || (treatPeriod.getIniDate().before(cr.getPeriod().getIniDate())))
				treatPeriod.setIniDate(cr.getPeriod().getIniDate());
			
			if ((treatPeriod.getEndDate() == null) || (treatPeriod.getEndDate().after(cr.getPeriod().getEndDate())))
				treatPeriod.setEndDate(cr.getPeriod().getEndDate());
		}
		treatPeriod.movePeriod(iniTreatmentDate);

		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			Date dt = DateUtils.incMonths(iniTreatmentDate, pm.getIniMonth());
			pm.getPeriod().movePeriod(dt);
		}
		
		TreatmentHealthUnit hu = new TreatmentHealthUnit();
		Period p = new Period(treatPeriod);
		hu.setPeriod(p);
		hu.setTbCase(tbcase);
		hu.setTbunit(getTbunitselection().getTbunit());
		hu.setTransferring(false);
		tbcase.getHealthUnits().add(hu);

		tbcase.setTreatmentPeriod(new Period(treatPeriod));
		tbcase.setState(CaseState.ONTREATMENT);
*/
	}


	public void initMedicineSelection(RegimenPhase phase) {
		List<PrescribedMedicine> meds;
		if (RegimenPhase.CONTINUOUS.equals(phase))
			meds = medicinesContPhase;
		else meds = medicinesIntPhase;

		medicineSelection.clear();
		medicineSelection.applyFilter(meds, "medicine");
		this.phase = phase;
	}
	
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
