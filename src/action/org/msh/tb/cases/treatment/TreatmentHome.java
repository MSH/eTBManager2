package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.tb.SourcesQuery;
import org.msh.tb.cases.CaseHome;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


@Name("treatmentHome")
@Scope(ScopeType.CONVERSATION)
public class TreatmentHome {
	// forms displayed by the page
	public enum FormEditing {	NONE, TREATMENT, HEALTHUNIT, REGIMEN, MEDICINE; 	}

	@In(required=true) CaseHome caseHome;
	@In(create=true) CaseRegimenHome caseRegimenHome;
	@In(create=true) PrescribedMedicineHome prescribedMedicineHome;
	@In(create=true) FacesMessages facesMessages;
	@In EntityManager entityManager;
	
	private Date iniDate;
	private Date endDate;
	private Date iniContinuousPhase;
	
	private List<SelectItem> months;
	private List<SelectItem> iniMonths;
	private List<SelectItem> doses;
	
	/**
	 * Return to the UI if there is any validation problem during ajax requests
	 */
	private boolean validated;

	private TreatmentHealthUnit healthUnit;
	private FormEditing formEditing;
	

	/**
	 * Initialize editing of the treatment
	 */
	public void initializeEditing() {
		PrescriptionTable tbl = getPrescriptionTable();
		tbl.setEditing(true);
		tbl.refresh();
		TbCase tbcase = caseHome.getInstance();
		if (tbcase.getIniContinuousPhase() != null)
			prescribedMedicineHome.splitPeriod(tbcase.getIniContinuousPhase());
	}


	/**
	 * Save changes made to the treatment and finish editing
	 * @return
	 */
	public String saveChanges() {
		getPrescriptionTable().refresh();

		caseHome.getInstance().updateDaysTreatPlanned();
		return caseHome.persist();
	}
	

	/**
	 * Start editing in the treatment period
	 */
	public void startTreatmentPeriodEditing() {
		TbCase tbcase = caseHome.getInstance();

		healthUnit = tbcase.getHealthUnits().get( tbcase.getHealthUnits().size() - 1 );

		iniDate = tbcase.getTreatmentPeriod().getIniDate();
		endDate = healthUnit.getPeriod().getEndDate();
		iniContinuousPhase = tbcase.getIniContinuousPhase();
		
		formEditing = FormEditing.TREATMENT;
	}
	


	/**
	 * Confirm editing of the treatment period
	 */
	public void endTreatmentPeriodEditing() {
		validated = checkDateBasicRules(iniDate, endDate);
		
		if (!validated)
			return;
		
		TbCase tbcase = caseHome.getInstance();
		
		Period intPeriod = new Period(iniDate, DateUtils.incDays(iniContinuousPhase, -1));
		Period conPeriod = new Period(iniContinuousPhase, endDate);

		prescribedMedicineHome.adjustPhasePeriod(RegimenPhase.INTENSIVE, intPeriod);
		prescribedMedicineHome.adjustPhasePeriod(RegimenPhase.CONTINUOUS, conPeriod);
		
		Period p = new Period(iniDate, endDate);
		healthUnit.setPeriod(p);
		
		tbcase.setTreatmentPeriod(p);
		tbcase.setIniContinuousPhase(iniContinuousPhase);

		formEditing = FormEditing.NONE;
		getPrescriptionTable().refresh();
	}

	
	/**
	 * Initialize regimen change 
	 */
	public void startRegimenChange() {
		formEditing = FormEditing.REGIMEN;
		caseRegimenHome.setRegimen(null);
	}


	/**
	 * Change the treatment regimen
	 */
	public void endRegimenChange() {
		TbCase tbcase = caseHome.getInstance();
		Date dt = tbcase.getTreatmentPeriod().getIniDate();
		caseRegimenHome.setUseDefaultDoseUnit(false);
		caseRegimenHome.applyRegimen(dt, null);
		tbcase.setRegimen(caseRegimenHome.getRegimen());
		//caseHome.persist();
		
		formEditing = FormEditing.NONE;
		validated = true;

		getPrescriptionTable().refresh();
	}


	/**
	 * Start entering a new prescription period
	 */
	public void startAddMedicine() {
		PrescribedMedicine pm = new PrescribedMedicine();
		Contexts.getConversationContext().set("prescribedMedicine", pm);

		// check the initial treatment date for individualized regimen
		TbCase tbcase = caseHome.getInstance();
		Period p = tbcase.getTreatmentPeriod();
		pm.setTbcase(tbcase);
		if (p == null) {
			p = new Period();
			caseHome.getInstance().setTreatmentPeriod(p);
		}

		if (p.getIniDate() == null) 
			p.setIniDate(new Date());
		if (p.getEndDate() == null)
			p.setEndDate(new Date());

		formEditing = FormEditing.MEDICINE;
		validated = false;
	}

	/**
	 * Start editing an existing prescription period
	 * @param pm
	 */
	public void startEditingMedicine(PrescribedMedicine pm) {
		Contexts.getConversationContext().set("prescribedMedicine", pm);
		formEditing = FormEditing.MEDICINE;
		validated = false;
	}


	/**
	 * Add a medicine to the prescription of the case
	 */
	public void endMedicineEditing() {
		validated = false;
		
		TbCase tbcase = caseHome.getInstance();
		
		PrescribedMedicine pm = (PrescribedMedicine)Contexts.getConversationContext().get("prescribedMedicine");
		
		if (pm.getSource() == null) {
			SourcesQuery sources = (SourcesQuery)Component.getInstance("sources");
			if (sources.isSingleResult())
				pm.setSource(sources.getResultList().get(0));
			else return;
		}

		if (!checkDateBasicRules(pm.getPeriod().getIniDate(), pm.getPeriod().getEndDate()))
			return;
		
		if (pm.getPeriod().getIniDate().before(tbcase.getTreatmentPeriod().getIniDate())) {
			facesMessages.addToControlFromResourceBundle("edtIniDate", "javax.faces.component.UIInput.REQUIRED");
			return;
		}

		prescribedMedicineHome.removePeriod(pm.getPeriod(), pm.getMedicine());
		if (!tbcase.getPrescribedMedicines().contains(pm)) {
			pm.setTbcase(tbcase);
			tbcase.getPrescribedMedicines().add(pm);
		}
		
		validated = true;
		formEditing = FormEditing.NONE;

		getPrescriptionTable().refresh();
	}


	
	
	/**
	 * Create list of dates indicating the changes in the medicine prescription
	 */
/*	private void createListMedicineChanges() {
		prescriptionChanges = new ArrayList<Date>();
		
		TbCase tbcase = caseHome.getInstance();
		
		// create period list
		prescriptionChanges.add(  tbcase.getTreatmentPeriod().getIniDate());
		prescriptionChanges.add(tbcase.getTreatmentPeriod().getEndDate());
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			Date dt = pm.getPeriod().getIniDate();
			if (!prescriptionChanges.contains(dt))
				prescriptionChanges.add(dt);
			dt = DateUtils.incDays( pm.getPeriod().getEndDate(), 1 );
			if (!prescriptionChanges.contains(dt))
				prescriptionChanges.add(dt);
		}
		Collections.sort(prescriptionChanges);
	}
*/	

	/**
	 * Initialize the period been edited
	 */
/*	public void initializePeriod() {
		TbCase tbcase = caseHome.getInstance();
		
		if (tbcase.getHealthUnits().size() == 0)
			return;
		
		if (tbcase.getHealthUnits().size() > 1)
			 period = tbcase.getHealthUnits().get(tbcase.getHealthUnits().size() - 1).getPeriod();
		else period = tbcase.getTreatmentPeriod();
	}
*/

	/** 
	 * Crop the treatment period according to the new period
	 * @param iniDate
	 * @param endDate
	 */
	public void cropTreatmentPeriod(Period p) {
		TbCase tbcase = caseHome.getInstance();
		int index = 0;
	
		// crop treatment health units
		while (index < tbcase.getHealthUnits().size()) {
			TreatmentHealthUnit hu = tbcase.getHealthUnits().get(index);
			// if doesn't intersect, so it's out of the period
			if (!hu.getPeriod().intersect(p)) {
				tbcase.getHealthUnits().remove(hu);
				if (entityManager.contains(hu))
					entityManager.remove(hu);
			}
			else index++;
		}
		
		// crop prescribed medicines
		index = 0;
		while (index < tbcase.getPrescribedMedicines().size()) {
			PrescribedMedicine pm = tbcase.getPrescribedMedicines().get(index);
			if (!pm.getPeriod().intersect(p)) {
				tbcase.getPrescribedMedicines().remove(pm);
				if (entityManager.contains(pm))
					entityManager.remove(pm);
			}
			else index++;
		}
		
		// set the new treatment period
		tbcase.setTreatmentPeriod(p);
	}
	
	
	/**
	 * Check basic constraints for date fields
	 * @return
	 */
	protected boolean checkDateBasicRules(Date iniDate, Date endDate) {
		if ((iniDate == null) || (endDate == null)) {
			facesMessages.add("Start and finish dates are required");
			return false;
		}

		// check if end date is before the initial date
		if (endDate.before(iniDate)) {
			facesMessages.addToControlFromResourceBundle("edtIniDate", "form.inienddate");
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Undo the treatment, turning the case to the 'waiting for treatment' state again
	 */
	public String undoTreatment() {
		TbCase tbcase = caseHome.getInstance();
		tbcase.setState(CaseState.WAITING_TREATMENT);
		tbcase.setTreatmentPeriod(null);
		tbcase.setIniContinuousPhase(null);
		tbcase.setRegimen(null);

		Integer id = caseHome.getInstance().getId();
		entityManager.createQuery("delete from PrescribedMedicine where tbcase.id = " + id.toString()).executeUpdate();
		entityManager.createQuery("delete from TreatmentHealthUnit where tbcase.id = " + id.toString()).executeUpdate();
		
//		tbcase.getTreatmentPeriod().set(null, null);
		tbcase.getHealthUnits().clear();
		tbcase.getPrescribedMedicines().clear();

		caseHome.setDisplayMessage(false);
		caseHome.persist();

		facesMessages.addFromResourceBundle("cases.treat.undo.executed");
		
		return "treatment-undone";
	}

	public Date getIniDate() {
		return iniDate;
	}

	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}


	public boolean isValidated() {
		return validated;
	}


	public FormEditing getFormEditing() {
		return formEditing;
	}


	public void setFormEditing(FormEditing formEditing) {
		this.formEditing = formEditing;
	}
	


	/**
	 * Return list of changes in the medicine prescription 
	 * @return
	 */
/*	public List<Date> getPrescriptionChanges() {
		if (prescriptionChanges == null)
			createListMedicineChanges();
		return prescriptionChanges;
	}
*/

/*	public Period getPeriod() {
		if (period == null)
			initializePeriod();
		return period;
	}
*/

	/**
	 * @return the iniContinuousPhase
	 */
	public Date getIniContinuousPhase() {
		return iniContinuousPhase;
	}


	/**
	 * @param iniContinuousPhase the iniContinuousPhase to set
	 */
	public void setIniContinuousPhase(Date iniContinuousPhase) {
		this.iniContinuousPhase = iniContinuousPhase;
	}

	/**
	 * Return the list of months as options for the user
	 * @return
	 */
	public List<SelectItem> getMonths() {
		if (months == null) {
			months = new ArrayList<SelectItem>();
			for (int i = 1; i <= 24; i++) {
				months.add(new SelectItem(i, Integer.toString(i)));
			}
		}
		return months;
	}


	/**
	 * Return the list of months as options for the user
	 * @return
	 */
	public List<SelectItem> getIniMonths() {
		if (iniMonths == null) {
			iniMonths = new ArrayList<SelectItem>();
			for (int i = 0; i < 24; i++) {
				iniMonths.add(new SelectItem(i, Integer.toString(i + 1)));
			}
		}
		return iniMonths;
	}


	/**
	 * Return the options for the dose unit during prescription
	 * @return
	 */
	public List<SelectItem> getDoses() {
		if (doses == null) {
			doses = new ArrayList<SelectItem>();
			doses.add(new SelectItem(null, "-"));
			for (int i = 1; i <= 10; i++) {
				doses.add(new SelectItem(i, Integer.toString(i)));
			}
		}

		return doses;
	}

	
	/**
	 * Return a reference to a prescription table component
	 * @return
	 */
	protected PrescriptionTable getPrescriptionTable() {
		return (PrescriptionTable)Component.getInstance("prescriptionTable", true);
	}
}
